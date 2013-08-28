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

import static com.google.common.base.Optional.*;
import static org.sonatype.aether.repository.RepositoryPolicy.*;

import java.io.File;
import java.util.Collections;
import java.util.Map;

import org.apache.maven.repository.internal.DefaultArtifactDescriptorReader;
import org.apache.maven.repository.internal.DefaultVersionRangeResolver;
import org.apache.maven.repository.internal.DefaultVersionResolver;
import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.apache.maven.repository.internal.SnapshotMetadataGeneratorFactory;
import org.apache.maven.repository.internal.VersionsMetadataGeneratorFactory;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.providers.file.FileWagon;
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
import com.google.common.collect.Maps;

/**
 * This class is thread-safe.
 */
public class ModelRepository implements IModelRepository {

    private final File basedir;
    private final RemoteRepository remoteRepo;
    private final RepositorySystem system;
    private final RepositorySystemSession defaultSession;

    public ModelRepository(File basedir, String remoteUrl) throws Exception {
        this.basedir = basedir;
        remoteRepo = createRemoteRepository(remoteUrl);
        system = createRepositorySystem();
        defaultSession = createDefaultSession();
    }

    private RemoteRepository createRemoteRepository(String url) {
        return new RemoteRepository("models", "default", url);
    }

    private RepositorySystem createRepositorySystem() throws Exception {
        DefaultServiceLocator locator = new DefaultServiceLocator();

        locator.addService(VersionResolver.class, DefaultVersionResolver.class);
        locator.addService(VersionRangeResolver.class, DefaultVersionRangeResolver.class);

        locator.addService(ArtifactDescriptorReader.class, DefaultArtifactDescriptorReader.class);

        locator.addService(MetadataGeneratorFactory.class, SnapshotMetadataGeneratorFactory.class);
        locator.addService(MetadataGeneratorFactory.class, VersionsMetadataGeneratorFactory.class);

        locator.addService(RepositoryConnectorFactory.class, WagonRepositoryConnectorFactory.class);
        locator.setServices(WagonProvider.class, new ManualWagonProvider());

        return locator.getService(RepositorySystem.class);
    }

    /**
     * Provides a default session that can be further customized.
     */
    private RepositorySystemSession createDefaultSession() {
        MavenRepositorySystemSession session = new MavenRepositorySystemSession();

        LocalRepository localRepo = new LocalRepository(basedir);
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(localRepo));

        // Do not expect POMs in a model repository.
        session.setIgnoreMissingArtifactDescriptor(true);
        // Do expect checksums.
        session.setChecksumPolicy(CHECKSUM_POLICY_FAIL);

        // Use timestamps in snapshot artifacts' names; do not keep (duplicate) artifacts named "SNAPSHOT".
        session.setConfigProperty("aether.artifactResolver.snapshotNormalization", false);

        // Ensure that the update policy set above is honoured.
        session.setConfigProperty("aether.versionResolver.noCache", true);

        return session;
    }

    /**
     * Maps model coordinates where resolution is currently in-progress to the file they previously resolved to, if any.
     * This makes it possible to immediately answer offline resolution requests while an online request is overwriting,
     * e.g., the <code>maven-metadata.xml</code>.
     */
    private final Map<ModelCoordinate, Optional<File>> inProgressResolutions = Maps.newHashMap();

    /**
     * {@inheritDoc}
     * 
     * Note: This implementation ignores the <code>prefetch</code> parameter.
     */
    @Override
    public Optional<File> getLocation(ModelCoordinate mc, boolean prefetch) {
        synchronized (inProgressResolutions) {
            if (inProgressResolutions.containsKey(mc)) {
                return inProgressResolutions.get(mc);
            }
            RepositorySystemSession offlineSession = newOfflineSession();
            return resolveInternal(mc, offlineSession);
        }
    }

    @Override
    public Optional<File> resolve(ModelCoordinate mc, boolean force) {
        return resolve(mc, force, DownloadCallback.NULL);
    }

    @Override
    public Optional<File> resolve(ModelCoordinate mc, boolean force, final DownloadCallback callback) {

        synchronized (inProgressResolutions) {
            RepositorySystemSession offlineSession = newOfflineSession();
            Optional<File> previousFile = resolveInternal(mc, offlineSession);
            inProgressResolutions.put(mc, previousFile);
        }

        try {
            // TODO Synchronization is still rather coarse-grained. We should consider SyncContext backed by
            // ReadWriteLock.
            synchronized (this) {
                RepositorySystemSession onlineSession = newOnlineSession(callback, force);
                return resolveInternal(mc, onlineSession);
            }
        } finally {
            synchronized (inProgressResolutions) {
                inProgressResolutions.remove(mc);
            }
        }
    }

    /**
     * <em>Not</em> thread-safe. Synchronization needs to be done by the caller.
     */
    private Optional<File> resolveInternal(ModelCoordinate mc, RepositorySystemSession session) {
        try {
            final Artifact coord = toSnapshotArtifact(mc);
            ArtifactRequest request = new ArtifactRequest(coord, Collections.singletonList(remoteRepo), null);
            ArtifactResult result = system.resolveArtifact(session, request);
            return of(result.getArtifact().getFile());
        } catch (ArtifactResolutionException e) {
            return absent();
        }
    }

    private RepositorySystemSession newOfflineSession() {
        DefaultRepositorySystemSession offlineSession = new DefaultRepositorySystemSession(defaultSession);
        offlineSession.setOffline(true);
        return offlineSession;
    }

    private DefaultRepositorySystemSession newOnlineSession(final DownloadCallback callback, boolean forceDownloads) {
        final DefaultRepositorySystemSession onlineSession = new DefaultRepositorySystemSession(defaultSession);

        if (forceDownloads) {
            onlineSession.setUpdatePolicy(RepositoryPolicy.UPDATE_POLICY_ALWAYS);
            onlineSession.setNotFoundCachingEnabled(false);
            onlineSession.setTransferErrorCachingEnabled(false);
        } else {
            // Try to update any models older than 60 minutes.
            onlineSession.setUpdatePolicy(UPDATE_POLICY_INTERVAL + ":" + 60);
            // Do not retry downloading missing models until the update interval has elapsed.
            onlineSession.setNotFoundCachingEnabled(true);
            // Do not retry downloading models after a failed download.
            onlineSession.setTransferErrorCachingEnabled(true);
        }

        onlineSession.setTransferListener(new TransferListener() {

            @Override
            public void transferInitiated(TransferEvent e) throws TransferCancelledException {
                callback.downloadInitiated(e.getResource().getResourceName());
            }

            @Override
            public void transferStarted(TransferEvent e) throws TransferCancelledException {
                callback.downloadStarted(e.getResource().getResourceName());
            }

            @Override
            public void transferProgressed(TransferEvent e) throws TransferCancelledException {
                callback.downloadProgressed(e.getResource().getResourceName(), e.getTransferredBytes(), e.getResource()
                        .getContentLength());
            }

            @Override
            public void transferSucceeded(TransferEvent e) {
                callback.downloadSucceeded(e.getResource().getResourceName());
            }

            @Override
            public void transferFailed(TransferEvent e) {
                callback.downloadFailed(e.getResource().getResourceName());
            }

            @Override
            public void transferCorrupted(TransferEvent e) throws TransferCancelledException {
                callback.downloadCorrupted(e.getResource().getResourceName());
            }
        });
        return onlineSession;
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

    private Artifact toSnapshotArtifact(ModelCoordinate mc) {
        return new DefaultArtifact(mc.getGroupId(), mc.getArtifactId(), mc.getClassifier(), mc.getExtension(),
                mc.getVersion() + "-SNAPSHOT");
    }
}
