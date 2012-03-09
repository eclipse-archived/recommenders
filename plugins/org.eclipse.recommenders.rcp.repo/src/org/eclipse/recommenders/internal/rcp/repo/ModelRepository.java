/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.repo;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.repository.internal.DefaultServiceLocator;
import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.recommenders.internal.rcp.repo.aether.ManualWagonProvider;
import org.eclipse.recommenders.internal.rcp.repo.aether.TransferListener;
import org.eclipse.recommenders.internal.rcp.repo.wiring.GuiceModule.LocalModelRepositoryLocation;
import org.eclipse.recommenders.internal.rcp.repo.wiring.GuiceModule.RemoteModelRepositoryLocation;
import org.eclipse.recommenders.rcp.repo.IModelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.aether.AbstractRepositoryListener;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.connector.wagon.WagonProvider;
import org.sonatype.aether.connector.wagon.WagonRepositoryConnectorFactory;
import org.sonatype.aether.deployment.DeployRequest;
import org.sonatype.aether.deployment.DeploymentException;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.installation.InstallRequest;
import org.sonatype.aether.installation.InstallationException;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.DependencyRequest;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.resolution.VersionRangeRequest;
import org.sonatype.aether.resolution.VersionRangeResult;
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory;
import org.sonatype.aether.util.DefaultRepositorySystemSession;
import org.sonatype.aether.version.Version;

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

    @Inject
    public ModelRepository(@LocalModelRepositoryLocation File localLocation,
            @RemoteModelRepositoryLocation String remoteLocation) throws Exception {
        this.location = localLocation;
        createRepositorySystem();
        setRemote(remoteLocation);
    }

    private void createRepositorySystem() throws Exception {
        DefaultServiceLocator locator = new DefaultServiceLocator();
        locator.setServices(WagonProvider.class, new ManualWagonProvider());
        locator.addService(RepositoryConnectorFactory.class, WagonRepositoryConnectorFactory.class);
        // import org.sonatype.aether.connector.file.FileRepositoryConnectorFactory;
        // locator.addService(RepositoryConnectorFactory.class, FileRepositoryConnectorFactory.class);
        system = locator.getService(RepositorySystem.class);
    }

    private DefaultRepositorySystemSession newSession() {
        MavenRepositorySystemSession session = new MavenRepositorySystemSession();
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

    private Optional<String> remoteEtag(Artifact artifact) {

        try {
            String remoteBaseurl = StringUtils.removeEnd(remote.getUrl(), "/");
            String url = String.format("%1$s/%2$s", remoteBaseurl, computePath(artifact));
            Response r = http.prepareHead(url).execute().get();

            String header = r.getHeader("ETag");
            if (header != null && !header.isEmpty()) {
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
    public synchronized void resolve(Artifact artifact, final IProgressMonitor monitor)
            throws DependencyResolutionException {
        monitor.subTask("Resolving...");
        DefaultRepositorySystemSession session = newSession();
        session.setTransferListener(new TransferListener(monitor));
        session.setRepositoryListener(new AbstractRepositoryListener() {

            @Override
            public void artifactDownloaded(org.sonatype.aether.RepositoryEvent event) {
                saveEtag(event.getArtifact());
            };
        });

        Dependency dependency = new Dependency(artifact, null);

        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRoot(dependency);
        collectRequest.addRepository(remote);

        DependencyRequest dependencyRequest = new DependencyRequest();
        dependencyRequest.setCollectRequest(collectRequest);

        system.resolveDependencies(session, dependencyRequest).getRoot();
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
    public void install(Artifact artifact, Artifact pom) throws InstallationException {
        RepositorySystemSession session = newSession();
        InstallRequest r = new InstallRequest();
        r.addArtifact(artifact).addArtifact(pom);
        system.install(session, r);
    }

    @Override
    public void deploy(Artifact artifact, Artifact pom, IProgressMonitor monitor) throws DeploymentException {
        DefaultRepositorySystemSession session = newSession();
        session.setTransferListener(new TransferListener(monitor));

        // Authentication authentication = new Authentication("admin", "admin123");
        // nexus.setAuthentication(authentication);

        DeployRequest r = new DeployRequest();
        r.addArtifact(artifact).addArtifact(pom);
        r.setRepository(remote);
        system.deploy(session, r);
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
        VersionRangeRequest rangeRequest = new VersionRangeRequest(a, Collections.singletonList(remote), "cr-calls");
        try {
            return of(system.resolveVersionRange(newSession(), rangeRequest));
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

    @Override
    public void setRemote(String url) {
        remote = new RemoteRepository("remote-models", "default", url);
        // "http://vandyk.st.informatik.tu-darmstadt.de/maven/"
        // recommenders repository
        // "http://213.133.100.41/recommenders/models/"
        // local repository
        // File("target/dist-repo").toURI().toString());
    }
}
