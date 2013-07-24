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
 *    Olav Lenz - externalize Strings.
 */
package org.eclipse.recommenders.models;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.eclipse.recommenders.utils.Constants.F_CLASSIFIER;
import static org.eclipse.recommenders.utils.Constants.F_SYMBOLIC_NAMES;
import static org.eclipse.recommenders.utils.Throws.throwUnsupportedOperation;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.maven.repository.internal.DefaultServiceLocator;
import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.eclipse.recommenders.utils.Artifacts;
import org.eclipse.recommenders.utils.Checks;
import org.eclipse.recommenders.utils.Executors;
import org.eclipse.recommenders.utils.Zips;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.collection.DependencyCollectionContext;
import org.sonatype.aether.collection.DependencySelector;
import org.sonatype.aether.connector.file.FileRepositoryConnectorFactory;
import org.sonatype.aether.connector.wagon.WagonProvider;
import org.sonatype.aether.connector.wagon.WagonRepositoryConnectorFactory;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.repository.Authentication;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.repository.ProxySelector;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.DependencyRequest;
import org.sonatype.aether.resolution.DependencyResult;
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory;
import org.sonatype.aether.transfer.TransferListener;
import org.sonatype.aether.util.DefaultRepositorySystemSession;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.AbstractIdleService;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

public class AetherModelRepository extends AbstractIdleService implements IModelRepository {

    private ListeningExecutorService executor = MoreExecutors.listeningDecorator(Executors.coreThreadsTimoutExecutor(1,
            Thread.MIN_PRIORITY, "model-downloader"));

    private Artifact INDEX = new DefaultArtifact("org.eclipse.recommenders", "index", "zip", "0.0.0");

    private Logger log = LoggerFactory.getLogger(getClass());

    private File basedir;
    private File indexdir;
    private File repodir;

    private RepositorySystem system;

    private RemoteRepository remote;
    private ProxySelector proxySelector;
    private FSDirectory index;
    private IndexReader reader;

    public AetherModelRepository(File local, String remote, ProxySelector proxySelector) throws Exception {
        basedir = local;
        indexdir = new File(local, "index");
        repodir = new File(local, "repository");
        this.proxySelector = proxySelector;
        system = createRepositorySystem();
        this.remote = new RemoteRepository("remote-models", "default", remote);
        if (proxySelector != null) {
            this.remote.setProxy(proxySelector.getProxy(this.remote));
        }
    }

    @Override
    protected void shutDown() throws Exception {

    }

    @Override
    protected void startUp() throws Exception {
        open();
    }

    public void open() throws Exception {
        if (doesNotExistOrIsAlmostEmptyFolder(indexdir)) {
            File zippedIndex = executor.submit(new DownloadArtifactTask(INDEX, null)).get();
            Zips.unzip(zippedIndex, indexdir);
        }
        index = FSDirectory.open(indexdir);
        reader = IndexReader.open(index);
    }

    private boolean doesNotExistOrIsAlmostEmptyFolder(File location) {
        return !location.exists() || location.listFiles().length < 2;
        // 2 = if this folder contains an index, there must be more than one file...
        // on mac, we often have hidden files in the folder. This is just a simple heuristic.
    }

    public AetherModelRepository(File local, String remote) throws Exception {
        this(local, remote, null);
    }

    public ListenableFuture<File> schedule(final ModelArchiveCoordinate model) {
        return schedule(model, null);
    }

    public ListenableFuture<File> schedule(final ModelArchiveCoordinate model, final TransferListener callback) {
        final Artifact coord = new DefaultArtifact(model.getGroupId(), model.getArtifactId(), model.getClassifier(),
                model.getExtension(), model.getVersion());
        return executor.submit(new DownloadArtifactTask(coord, callback));
    }

    @Override
    public void resolve(ModelArchiveCoordinate model) throws Exception {
        schedule(model, null).get();
    }

    public void delete(ModelArchiveCoordinate model) throws IOException {
        throwUnsupportedOperation();
    }

    @Override
    public Optional<File> getLocation(ModelArchiveCoordinate coord) {
        File result = new File(repodir, computePath(coord));
        if (!result.exists()) {
            // TODO no logic implemented that prevents multiple resolutions of the same type
            schedule(coord, null);
            return absent();
        }
        return of(result);
    }

    private String computePath(ModelArchiveCoordinate artifact) {
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

    public ModelArchiveCoordinate[] findModelArchives(ProjectCoordinate projectCoord, String modelType) {
        Checks.ensureIsNotNull(projectCoord);
        Checks.ensureIsNotNull(modelType);
        // TODO write a new match strategy gid:aid:*
        // TODO Term t0 = new Term(F_GROUP_ID, projectCoordinate.getGroupId())
        Term t1 = new Term(F_SYMBOLIC_NAMES, projectCoord.getArtifactId());
        Term t2 = new Term(F_CLASSIFIER, modelType);
        Set<ModelArchiveCoordinate> results = Sets.newHashSet();
        for (String model : findByTerm(modelType, t1, t2)) {
            Artifact tmp = Artifacts.asArtifact(model);
            results.add(new ModelArchiveCoordinate(tmp.getGroupId(), tmp.getArtifactId(), tmp.getClassifier(), tmp
                    .getExtension(), tmp.getVersion()));
        }
        return Iterables.toArray(results, ModelArchiveCoordinate.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<ModelArchiveCoordinate> findBestModelArchive(ProjectCoordinate projectCoord, String modelType) {
        // TODO find best model archive needs to be implemented properly
        log.warn("only returning the first match as best model!");
        ModelArchiveCoordinate[] results = findModelArchives(projectCoord, modelType);
        return (Optional<ModelArchiveCoordinate>) (results.length == 0 ? absent() : of(results[0]));
    }

    public List<Artifact> searchByClassifier(String classifier) {
        List<Artifact> res = Lists.newLinkedList();
        try {
            Term t = new Term(F_CLASSIFIER, classifier);
            IndexSearcher searcher = new IndexSearcher(reader);
            TopDocs matches = searcher.search(new TermQuery(t), Integer.MAX_VALUE);
            searcher.close();
            for (ScoreDoc doc : matches.scoreDocs) {
                String value = reader.document(doc.doc).get(classifier);
                if (value != null) {
                    res.add(Artifacts.newArtifact(value));
                }
            }
        } catch (Exception e) {
            log.error("Searching index failed with exception", e); //$NON-NLS-1$
        }
        return res;
    }

    private Set<String> findByTerm(String modelCoordFieldName, Term... terms) {
        if (reader == null) {
            return Collections.emptySet();
        }
        BooleanQuery query = new BooleanQuery();
        for (Term t : terms) {
            TermQuery q = new TermQuery(t);
            query.add(q, Occur.MUST);
        }

        try {
            IndexSearcher searcher = new IndexSearcher(reader);
            TopDocs matches = searcher.search(query, 100);
            searcher.close();

            Set<String> results = Sets.newHashSet();
            for (ScoreDoc scoreDoc : matches.scoreDocs) {
                Document doc = reader.document(scoreDoc.doc);
                // TODO does work with CALL models only!:
                String modelcoord = doc.get(modelCoordFieldName);
                results.add(modelcoord);
            }
            return results;
        } catch (Exception e) {
            return Collections.emptySet();
        }
    }

    protected RepositorySystem createRepositorySystem() throws Exception {
        @SuppressWarnings("deprecation")
        DefaultServiceLocator locator = new DefaultServiceLocator();
        locator.setServices(WagonProvider.class, new ManualWagonProvider());
        locator.addService(RepositoryConnectorFactory.class, WagonRepositoryConnectorFactory.class);
        // import org.sonatype.aether.connector.file.FileRepositoryConnectorFactory;
        locator.addService(RepositoryConnectorFactory.class, FileRepositoryConnectorFactory.class);
        return locator.getService(RepositorySystem.class);
    }

    private synchronized DefaultRepositorySystemSession newSession() {
        MavenRepositorySystemSession session = new MavenRepositorySystemSession();

        if (proxySelector != null) {
            session.setProxySelector(proxySelector);
            remote.setProxy(proxySelector.getProxy(remote));
        }
        LocalRepository localRepo = new LocalRepository(repodir);
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(localRepo));
        return session;
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
            DefaultRepositorySystemSession session = newSession();
            session.setTransferListener(callback);
            session.setDependencySelector(new TheArtifactOnlyDependencySelector());
            Dependency dependency = new Dependency(coord, "model");

            CollectRequest collectRequest = new CollectRequest();
            collectRequest.setRoot(dependency);
            collectRequest.addRepository(remote);

            DependencyRequest dependencyRequest = new DependencyRequest();
            dependencyRequest.setCollectRequest(collectRequest);
            DependencyResult dependencies = system.resolveDependencies(session, dependencyRequest);
            DependencyNode root = dependencies.getRoot();
            File file = root.getDependency().getArtifact().getFile();
            return file;
        }
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

    public void setAuthentication(String user, String pass) {
        remote.setAuthentication(new Authentication(user, pass));
    }

}
