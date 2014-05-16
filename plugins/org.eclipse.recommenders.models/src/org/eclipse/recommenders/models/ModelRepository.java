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

import static org.eclipse.aether.ConfigurationProperties.PERSISTED_CHECKSUMS;
import static org.eclipse.aether.repository.RepositoryPolicy.CHECKSUM_POLICY_FAIL;
import static org.eclipse.aether.resolution.ArtifactDescriptorPolicy.IGNORE_MISSING;
import static org.eclipse.aether.resolution.ResolutionErrorPolicy.*;

import java.io.File;
import java.util.Collections;
import java.util.Map;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.Authentication;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.Proxy;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.repository.RepositoryPolicy;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transfer.TransferCancelledException;
import org.eclipse.aether.transfer.TransferEvent;
import org.eclipse.aether.transfer.TransferListener;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.util.repository.AuthenticationBuilder;
import org.eclipse.aether.util.repository.SimpleArtifactDescriptorPolicy;
import org.eclipse.aether.util.repository.SimpleResolutionErrorPolicy;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;

/**
 * This class is thread-safe.
 */
public class ModelRepository implements IModelRepository {

    private final RepositorySystem system;
    private final RepositorySystemSession defaultSession;
    private final RemoteRepository defaultRemoteRepo;

    private Authentication authentication;
    private Proxy proxy;

    public ModelRepository(File basedir, String remoteUrl) {
        this(createRepositorySystem(), basedir, remoteUrl);
    }

    @VisibleForTesting
    public ModelRepository(RepositorySystem system, File basedir, String remoteUrl) {
        this.system = system;
        this.defaultSession = createDefaultSession(basedir);
        this.defaultRemoteRepo = createRemoteRepository(remoteUrl);
    }

    private static RepositorySystem createRepositorySystem() {
        DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();

        locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
        locator.addService(TransporterFactory.class, FileTransporterFactory.class);
        locator.addService(TransporterFactory.class, HttpTransporterFactory.class);

        return locator.getService(RepositorySystem.class);
    }

    /**
     * Provides a default session that can be further customized.
     */
    private RepositorySystemSession createDefaultSession(File basedir) {
        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();

        LocalRepository localRepo = new LocalRepository(basedir);
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));

        // Do not expect POMs in a model repository.
        session.setArtifactDescriptorPolicy(new SimpleArtifactDescriptorPolicy(IGNORE_MISSING));
        // Do expect checksums...
        session.setChecksumPolicy(CHECKSUM_POLICY_FAIL);
        // ...but do not store them.
        session.setConfigProperty(PERSISTED_CHECKSUMS, false);

        // Use timestamps in snapshot artifacts' names; do not keep (duplicate) artifacts named "SNAPSHOT".
        session.setConfigProperty("aether.artifactResolver.snapshotNormalization", false);

        // Ensure that the update policy set is honored.
        session.setConfigProperty("aether.versionResolver.noCache", true);
        session.setConfigProperty("aether.updateCheckManager.sessionState", "bypass");

        return session;
    }

    private RemoteRepository createRemoteRepository(String url) {
        return new RemoteRepository.Builder("models", "default", url).build();
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
            RemoteRepository remoteRepo = new RemoteRepository.Builder(defaultRemoteRepo)
            .setAuthentication(authentication).setProxy(proxy).build();
            ArtifactRequest request = new ArtifactRequest(coord, Collections.singletonList(remoteRepo), null);
            ArtifactResult result = system.resolveArtifact(session, request);
            return Optional.of(result.getArtifact().getFile());
        } catch (ArtifactResolutionException e) {
            return Optional.absent();
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
            onlineSession.setResolutionErrorPolicy(new SimpleResolutionErrorPolicy(CACHE_DISABLED));
        } else {
            // Try to update any models older than 60 minutes.
            onlineSession.setUpdatePolicy(RepositoryPolicy.UPDATE_POLICY_INTERVAL + ":" + 60);
            // Do not retry downloading missing models until the update interval has elapsed.
            // Do not retry downloading models after a failed download.
            onlineSession.setResolutionErrorPolicy(new SimpleResolutionErrorPolicy(CACHE_ALL));
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
        Authentication proxyAuthentication = new AuthenticationBuilder().addUsername(user).addPassword(pass).build();
        proxy = type == null ? null : new Proxy(type, host, port, proxyAuthentication);
    }

    @Beta
    public void unsetProxy() {
        proxy = null;
    }

    @Beta
    public void setAuthentication(String user, String pass) {
        authentication = new AuthenticationBuilder().addUsername(user).addPassword(pass).build();
    }

    @Override
    public String toString() {
        return defaultSession.getLocalRepository().getBasedir().toString();
    }

    private Artifact toSnapshotArtifact(ModelCoordinate mc) {
        return new DefaultArtifact(mc.getGroupId(), mc.getArtifactId(), mc.getClassifier(), mc.getExtension(),
                mc.getVersion() + "-SNAPSHOT");
    }
}
