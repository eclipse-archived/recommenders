/**
 * Copyright (c) 2010, 2012 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 *    Patrick Gottschaemmer, Olav Lenz - Introduced ProxySelector
 */
package org.eclipse.recommenders.internal.rcp.repo;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.io.File;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.repository.internal.DefaultServiceLocator;
import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.recommenders.internal.rcp.wiring.RecommendersModule.LocalModelRepositoryLocation;
import org.eclipse.recommenders.internal.rcp.wiring.RecommendersModule.RemoteModelRepositoryLocation;
import org.eclipse.recommenders.rcp.repo.IModelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.aether.AbstractRepositoryListener;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.collection.DependencyCollectionContext;
import org.sonatype.aether.collection.DependencySelector;
import org.sonatype.aether.connector.file.FileRepositoryConnectorFactory;
import org.sonatype.aether.connector.wagon.WagonProvider;
import org.sonatype.aether.connector.wagon.WagonRepositoryConnectorFactory;
import org.sonatype.aether.deployment.DeployRequest;
import org.sonatype.aether.deployment.DeploymentException;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.installation.InstallRequest;
import org.sonatype.aether.installation.InstallationException;
import org.sonatype.aether.repository.Authentication;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.repository.ProxySelector;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.DependencyRequest;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.resolution.VersionRangeRequest;
import org.sonatype.aether.resolution.VersionRangeResult;
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory;
import org.sonatype.aether.util.DefaultRepositorySystemSession;
import org.sonatype.aether.version.Version;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;

@Singleton
public class ModelRepository implements IModelRepository {

    private Logger log = LoggerFactory.getLogger(getClass());
    private AsyncHttpClient http = new AsyncHttpClient();

    private final File location;
    private RepositorySystem system;

    private RemoteRepository remote;
    private ProxySelector proxySelector;

    @Inject
    public ModelRepository(@LocalModelRepositoryLocation File localLocation,
            @RemoteModelRepositoryLocation String remoteLocation, ProxySelector proxySelector) throws Exception {
        this.location = localLocation;
        this.proxySelector = proxySelector;
        this.system = createRepositorySystem();
        setRemote(remoteLocation);
    }

    public ModelRepository(@LocalModelRepositoryLocation File localLocation,
            @RemoteModelRepositoryLocation String remoteLocation) throws Exception {
        this(localLocation, remoteLocation, null);
    }

    protected RepositorySystem createRepositorySystem() throws Exception {
        DefaultServiceLocator locator = new DefaultServiceLocator();
        locator.setServices(WagonProvider.class, new ManualWagonProvider());
        locator.addService(RepositoryConnectorFactory.class, WagonRepositoryConnectorFactory.class);
        // import org.sonatype.aether.connector.file.FileRepositoryConnectorFactory;
        locator.addService(RepositoryConnectorFactory.class, FileRepositoryConnectorFactory.class);
        return locator.getService(RepositorySystem.class);
    }

    private synchronized DefaultRepositorySystemSession newSession() {
        MavenRepositorySystemSession session = new MavenRepositorySystemSession();
        session.setProxySelector(proxySelector);
        remote.setProxy(proxySelector.getProxy(remote));
        LocalRepository localRepo = new LocalRepository(location);
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(localRepo));
        return session;
    }

    @Override
    public boolean isLatest(Artifact artifact) {
        Optional<String> remoteEtag = remoteEtag(artifact);
        Optional<String> localEtag = localEtag(artifact);
        return remoteEtag.equals(localEtag);
    }

    @VisibleForTesting
    public Optional<String> remoteEtag(Artifact artifact) {

        try {
            String remoteBaseurl = StringUtils.removeEnd(remote.getUrl(), "/");
            String url = String.format("%1$s/%2$s", remoteBaseurl, computePath(artifact));
            if (url.startsWith("file:")) {
                // try file:
                File file = new File(new URI(url));
                if (file.exists())
                    return of(file.lastModified() + "");
                return absent();
            }
            Response r = http.prepareHead(url).execute().get();

            String header = r.getHeader("ETag");
            if (isNotEmpty(header)) {
                header = StringUtils.remove(header, "\"");
                return of(header);
            }
        } catch (Exception e) {
            log.debug(e.getMessage());
        }
        log.warn("'{}' did not send ETAG header for '{}'.", remote, artifact);
        return absent();
    }

    private Optional<String> localEtag(Artifact artifact) {
        try {
            File local = etagFile(artifact);
            if (!local.exists()) {
                return absent();
            }
            String line = Files.readFirstLine(local, Charset.defaultCharset());
            if (line != null && !line.isEmpty()) {
                return of(line);
            }
        } catch (Exception e) {
        }
        return absent();
    }

    private File etagFile(Artifact artifact) {
        return new File(location(artifact).getAbsolutePath() + ".etag");
    }

    @Override
    public File location(Artifact artifact) {
        return new File(location, computePath(artifact));
    }

    private String computePath(Artifact artifact) {
        String groupId = artifact.getGroupId().replace('.', '/');
        String artifactId = artifact.getArtifactId();
        String version = artifact.getVersion();
        String classifier = artifact.getClassifier();
        String extension = artifact.getExtension();

        StringBuilder sb = new StringBuilder();
        sb.append(groupId).append('/').append(artifactId).append('/').append(version).append('/').append(artifactId)
                .append('-').append(version);
        if (!isEmpty(classifier)) {
            sb.append('-').append(classifier);
        }
        sb.append('.').append(extension);
        return sb.toString();
    }

    @Override
    public void delete(Artifact artifact) {
        File file = location(artifact);
        File etag = etagFile(artifact);
        file.delete();
        etag.delete();
    }

    @Override
    public synchronized File resolve(Artifact artifact, final IProgressMonitor monitor)
            throws DependencyResolutionException {
        monitor.subTask("Resolving...");
        DefaultRepositorySystemSession session = newSession();
        session.setDependencySelector(new TheArtifactOnlyDependencySelector());
        session.setTransferListener(new TransferListener(monitor));
        session.setRepositoryListener(new AbstractRepositoryListener() {

            @Override
            public void artifactDownloaded(org.sonatype.aether.RepositoryEvent event) {
                monitor.subTask("downloaded " + event.getArtifact());
                saveEtag(event.getArtifact());
            };
        });

        Dependency dependency = new Dependency(artifact, "model");

        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRoot(dependency);
        collectRequest.addRepository(remote);

        DependencyRequest dependencyRequest = new DependencyRequest();
        dependencyRequest.setCollectRequest(collectRequest);

        DependencyNode root = system.resolveDependencies(session, dependencyRequest).getRoot();
        File file = root.getDependency().getArtifact().getFile();
        return file;
    }

    private void saveEtag(Artifact artifact) {
        Optional<String> remoteEtag = remoteEtag(artifact);
        if (remoteEtag.isPresent()) {
            File etagFile = etagFile(artifact);
            try {
                Files.write(remoteEtag.get(), etagFile, Charset.defaultCharset());
            } catch (Exception e) {
                log.error("Failed to write etag to file " + etagFile, e);
            }
        }
    }

    @Override
    public void install(Artifact artifact) throws InstallationException {
        RepositorySystemSession session = newSession();
        InstallRequest r = new InstallRequest();
        r.addArtifact(artifact);// .addArtifact(pom);
        system.install(session, r);
        log.info("installed '{}' to {}", artifact, location);
    }

    public void deploy(Artifact artifact) throws DeploymentException {
        RepositorySystemSession session = newSession();
        DeployRequest r = new DeployRequest();
        r.addArtifact(artifact);
        r.setRepository(remote);
        system.deploy(session, r);
        log.info("deployed '{}' to {}", artifact, remote.getUrl());
    }

    @Override
    public String toString() {
        return location.getAbsolutePath();
    }

    @Override
    public Optional<Artifact> findHigestVersion(Artifact artifact) {
        Optional<VersionRangeResult> opt = resolveVersionRange(artifact);
        if (!opt.isPresent()) {
            return absent();
        }
        ArrayList<Version> versions = Lists.newArrayList(opt.get().getVersions());
        Collections.reverse(versions);
        for (Version v : versions) {
            Artifact query = artifact.setVersion(v.toString());
            if (remoteEtag(query).isPresent()) {
                return of(query);
            }
        }
        return absent();
    }

    private Optional<VersionRangeResult> resolveVersionRange(Artifact a) {
        VersionRangeRequest rangeRequest = new VersionRangeRequest(a, Collections.singletonList(remote),
                a.getClassifier());
        try {
            VersionRangeResult range = system.resolveVersionRange(newSession(), rangeRequest);
            return of(range);
        } catch (Exception e) {
            log.error("Failed to resolve version range for artifact " + a + ".", e);
            return absent();
        }
    }

    @Override
    public Optional<Artifact> findLowestVersion(Artifact artifact) {
        Optional<VersionRangeResult> opt = resolveVersionRange(artifact);
        if (!opt.isPresent()) {
            return absent();
        }
        for (Version v : opt.get().getVersions()) {
            Artifact query = artifact.setVersion(v.toString());
            if (remoteEtag(query).isPresent()) {
                return of(query);
            }
        }
        return absent();
    }

    /**
     * setRemote(String url) is kept for backwards compatibility with the interface, will be changed soon with
     * refactoring of the new models api.
     */
    @Override
    public synchronized void setRemote(String url) {
        remote = new RemoteRepository("remote-models", "default", url);
        remote.setProxy(proxySelector.getProxy(remote));
    }

    public synchronized void setRemoteRepository(RemoteRepository remote) {
        this.remote = remote;
        remote.setProxy(proxySelector.getProxy(remote));
    }

    public synchronized RemoteRepository getRemoteRepository() {
        return remote;
    }

    public static class TheArtifactOnlyDependencySelector implements DependencySelector {
        @Override
        public boolean selectDependency(Dependency d) {
            // we don't want any dependencies to be returned. Just the artifact itself.
            return false;
        }

        @Override
        public DependencySelector deriveChildSelector(DependencyCollectionContext c) {
            return this;
        }
    }

    public String getRemoteUrl() {
        return remote.getUrl();
    }

    @Override
    public File getLocation() {
        return location;
    }

    public void setAuthentication(String user, String pass) {
        remote.setAuthentication(new Authentication(user, pass));
    }
}
