/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 *    Patrick Gottschaemmer, Olav Lenz - Introduced ProxySelector
 *    Olav Lenz - externalize Strings.
 *    Andreas Sewe - modernized use of Aether
 */
package org.eclipse.recommenders.models;

import static com.google.common.base.Objects.firstNonNull;
import static com.google.common.base.Optional.*;
import static org.sonatype.aether.repository.RepositoryPolicy.UPDATE_POLICY_INTERVAL;

import java.io.File;
import java.util.concurrent.Callable;

import org.apache.maven.repository.internal.DefaultArtifactDescriptorReader;
import org.apache.maven.repository.internal.DefaultVersionRangeResolver;
import org.apache.maven.repository.internal.DefaultVersionResolver;
import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.apache.maven.repository.internal.SnapshotMetadataGeneratorFactory;
import org.apache.maven.repository.internal.VersionsMetadataGeneratorFactory;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.providers.file.FileWagon;
import org.eclipse.recommenders.utils.Executors;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.connector.wagon.WagonProvider;
import org.sonatype.aether.connector.wagon.WagonRepositoryConnectorFactory;
import org.sonatype.aether.impl.ArtifactDescriptorReader;
import org.sonatype.aether.impl.MetadataGeneratorFactory;
import org.sonatype.aether.impl.VersionRangeResolver;
import org.sonatype.aether.impl.VersionResolver;
import org.sonatype.aether.impl.internal.DefaultServiceLocator;
import org.sonatype.aether.repository.Authentication;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.repository.Proxy;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.repository.RepositoryPolicy;
import org.sonatype.aether.resolution.ArtifactRequest;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.resolution.ArtifactResult;
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory;
import org.sonatype.aether.transfer.TransferCancelledException;
import org.sonatype.aether.transfer.TransferEvent;
import org.sonatype.aether.transfer.TransferListener;
import org.sonatype.aether.util.DefaultRepositorySystemSession;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.maven.wagon.AhcWagon;

import com.google.common.annotations.Beta;
import com.google.common.base.Optional;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

public class ModelRepository implements IModelRepository {

    private ListeningExecutorService executor = MoreExecutors.listeningDecorator(Executors.coreThreadsTimoutExecutor(1,
            Thread.MIN_PRIORITY, "model-downloader"));

    private final File basedir;
    private final RemoteRepository remoteRepo;
    private final RepositorySystem system;
    private final RepositorySystemSession onlineSession;
    private final RepositorySystemSession offlineSession;

    public ModelRepository(File basedir, String remoteUrl) throws Exception {
        this.basedir = basedir;
        remoteRepo = newRemoteRepository(remoteUrl);
        system = createRepositorySystem();
        onlineSession = createRepositorySystemSession(false);
        offlineSession = createRepositorySystemSession(true);
    }

    private RepositorySystem createRepositorySystem() throws Exception {
        DefaultServiceLocator locator = new DefaultServiceLocator();
        locator.addService(VersionResolver.class, DefaultVersionResolver.class);
        locator.addService(VersionRangeResolver.class, DefaultVersionRangeResolver.class);
        locator.addService(MetadataGeneratorFactory.class, SnapshotMetadataGeneratorFactory.class);
        locator.addService(MetadataGeneratorFactory.class, VersionsMetadataGeneratorFactory.class);
        locator.addService(ArtifactDescriptorReader.class, DefaultArtifactDescriptorReader.class);
        locator.setServices(WagonProvider.class, new ManualWagonProvider());
        locator.addService(RepositoryConnectorFactory.class, WagonRepositoryConnectorFactory.class);
        return locator.getService(RepositorySystem.class);
    }

    private RepositorySystemSession createRepositorySystemSession(boolean offline) {
        MavenRepositorySystemSession session = new MavenRepositorySystemSession();

        LocalRepository localRepo = new LocalRepository(basedir);
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(localRepo));
        session.setOffline(offline);

        // Do not expect POMs in a model repository
        session.setIgnoreMissingArtifactDescriptor(true);
        // TODO Do expect checksums. Should be switched to CHECKSUM_POLICY_FAIL as the new model repos provide
        // checksums. :-)
        session.setChecksumPolicy(RepositoryPolicy.CHECKSUM_POLICY_IGNORE);

        // Try to update any models older than 60 minutes.
        session.setUpdatePolicy(UPDATE_POLICY_INTERVAL + ":" + 60);
        // Do not retry downloading missing models for 60 minutes.
        session.setNotFoundCachingEnabled(true);
        // Do retry downloading models after a failed download.
        session.setTransferErrorCachingEnabled(false);

        // Use timestamps in snapshot artifacts' names; do not keep (duplicate) artifacts named "SNAPSHOT".
        session.setConfigProperty("aether.artifactResolver.snapshotNormalization", false);

        return session;
    }

    private RemoteRepository newRemoteRepository(String url) {
        return new RemoteRepository("remote-models", "default", url);
    }

    @Override
    public Optional<File> getLocation(ModelCoordinate mc) {
        return resolveLocally(mc);
    }

    // Warning: Only thread-safe as long as offlineSession is not mutated.
    private Optional<File> resolveLocally(ModelCoordinate mc) {
        final Artifact coord = new DefaultArtifact(mc.getGroupId(), mc.getArtifactId(), mc.getClassifier(),
                mc.getExtension(), mc.getVersion());

        ArtifactRequest request = new ArtifactRequest();
        request.setArtifact(coord);
        request.addRepository(remoteRepo);

        try {
            ArtifactResult result = system.resolveArtifact(offlineSession, request);
            return Optional.of(result.getArtifact().getFile());
        } catch (ArtifactResolutionException e) {
            return absent();
        }
    }

    @Override
    public Optional<File> resolve(ModelCoordinate mc) throws Exception {
        return fromNullable(schedule(mc, null).get());
    }

    @Override
    public ListenableFuture<File> resolve(ModelCoordinate mc, DownloadCallback callback) {
        return schedule(mc, callback);
    }

    private ListenableFuture<File> schedule(final ModelCoordinate mc, DownloadCallback callback) {
        final DownloadCallback cb = firstNonNull(callback, DownloadCallback.NULL);
        final Artifact coord = new DefaultArtifact(mc.getGroupId(), mc.getArtifactId(), mc.getClassifier(),
                mc.getExtension(), mc.getVersion());
        return executor.submit(new DownloadArtifactTask(coord, new TransferListener() {

            @Override
            public void transferSucceeded(TransferEvent e) {
                cb.downloadSucceeded();
            }

            @Override
            public void transferStarted(TransferEvent e) throws TransferCancelledException {
                cb.downloadStarted();
            }

            @Override
            public void transferProgressed(TransferEvent e) throws TransferCancelledException {
                cb.downloadProgressed(e.getTransferredBytes(), e.getResource().getContentLength());
            }

            @Override
            public void transferInitiated(TransferEvent e) throws TransferCancelledException {
                cb.downloadInitiated();
            }

            @Override
            public void transferFailed(TransferEvent e) {
                cb.downloadFailed();
            }

            @Override
            public void transferCorrupted(TransferEvent e) throws TransferCancelledException {
                cb.downloadCorrupted();
            }
        }));
    }

    @Beta
    public void setProxy(String type, String host, int port, String user, String pass) {
        Authentication auth = user == null ? null : new Authentication(user, pass);
        Proxy proxy = type == null ? null : new Proxy(type, host, port, auth);
        remoteRepo.setProxy(proxy);
    }

    @Beta
    public void unsetProxy() {
        // TODO: need an API to reset proxy settings.
        remoteRepo.setProxy(null);
    }

    @Beta
    public void setAuthentication(String user, String pass) {
        remoteRepo.setAuthentication(new Authentication(user, pass));
    }

    @Override
    public String toString() {
        return basedir.getAbsolutePath();
    }

    private final class DownloadArtifactTask implements Callable<File> {

        private final Artifact coord;
        private final TransferListener callback;

        private DownloadArtifactTask(Artifact coord, TransferListener callback) {
            this.coord = coord;
            this.callback = callback;
        }

        @Override
        public File call() throws Exception {
            DefaultRepositorySystemSession session = new DefaultRepositorySystemSession(onlineSession);
            session.setTransferListener(callback);

            ArtifactRequest request = new ArtifactRequest();
            request.setArtifact(coord);
            request.addRepository(remoteRepo);

            ArtifactResult result = system.resolveArtifact(onlineSession, request);
            return result.getArtifact().getFile();
        }
    }

    /**
     * A simplistic provider for wagon instances when no Plexus-compatible IoC container is used.
     */
    private static class ManualWagonProvider implements org.sonatype.aether.connector.wagon.WagonProvider {

        @Override
        public Wagon lookup(String roleHint) throws Exception {
            if ("http".equals(roleHint) || "https".equals(roleHint)) { //$NON-NLS-1$ //$NON-NLS-2$
                AhcWagon ahcWagon = new AhcWagon();
                // TODO set timeout to 300s instead of 60s to solve timeouts.
                // experimental.
                ahcWagon.setTimeout(300 * 1000);
                return ahcWagon;
                // return new WebDavWagon();
            } else if ("file".equals(roleHint)) {
                return new FileWagon();
            } else {
                return null;
            }
        }

        @Override
        public void release(Wagon wagon) {
        }
    }
}
