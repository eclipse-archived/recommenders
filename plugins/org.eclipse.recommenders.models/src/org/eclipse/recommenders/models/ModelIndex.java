/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.models;

import static com.google.common.base.Optional.absent;
import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.Iterables.find;
import static java.lang.Math.min;
import static java.lang.String.format;
import static org.eclipse.recommenders.models.Coordinates.tryNewProjectCoordinate;
import static org.eclipse.recommenders.utils.Constants.*;
import static org.eclipse.recommenders.utils.IOUtils.closeQuietly;
import static org.eclipse.recommenders.utils.Versions.canonicalizeVersion;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.recommenders.utils.Artifacts;
import org.eclipse.recommenders.utils.Checks;
import org.eclipse.recommenders.utils.Version;
import org.eclipse.recommenders.utils.Versions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * The RecommendersModelIndex index is the default implementation for of an {@link IModelArchiveCoordinateAdvisor}. It
 * internally uses an Apache Lucene index. Clients should rather refer to the interface instead of referencing this
 * class directly.
 */
public class ModelIndex implements IModelArchiveCoordinateAdvisor, IModelIndex {

    private static final int MAX_DOCUMENTS_SEARCHED = 100;

    private File indexdir;
    private Directory index;
    private IndexReader reader;
    private Logger log = LoggerFactory.getLogger(getClass());

    public ModelIndex(File indexdir) {
        this.indexdir = indexdir;
    }

    @VisibleForTesting
    ModelIndex(Directory index) {
        this.index = index;
    }

    public boolean isAccessible() {
        return index != null && reader != null;
    }

    @Override
    public void open() throws IOException {
        // Normally, indexdir is set; if not, the VisibleForTesting constructor has been used.
        if (indexdir != null) {
            index = FSDirectory.open(indexdir);
        }
        reader = IndexReader.open(index);
    }

    @Override
    public void close() throws IOException {
        closeQuietly(reader);
        closeQuietly(index);
    }

    @Override
    public Optional<ModelCoordinate> suggest(ProjectCoordinate pc, String modelType) {
        ImmutableSet<ModelCoordinate> results = suggestCandidates(pc, modelType);

        if (results.isEmpty()) {
            return absent();
        }

        final Version closestVersion = Versions.findClosest(Version.valueOf(pc.getVersion()),
                transform(results, new Function<ModelCoordinate, Version>() {

                    @Override
                    public Version apply(ModelCoordinate mc) {
                        return Version.valueOf(mc.getVersion());
                    }
                }));
        return Optional.of(find(results, new Predicate<ModelCoordinate>() {

            @Override
            public boolean apply(ModelCoordinate mc) {
                return Version.valueOf(mc.getVersion()).equals(closestVersion);
            }
        }));
    }

    @Override
    public ImmutableSet<ModelCoordinate> suggestCandidates(ProjectCoordinate pc, String modelType) {
        Checks.ensureIsNotNull(modelType);
        if (!isAccessible()) {
            return ImmutableSet.of();
        }
        Builder<ModelCoordinate> res = ImmutableSet.builder();
        for (String model : queryLuceneIndexForModelCandidates(pc, modelType)) {
            Artifact tmp = Artifacts.asArtifact(model);
            ModelCoordinate mc = toModelCoordinate(tmp);
            res.add(mc);
        }
        return res.build();
    }

    private Set<String> queryLuceneIndexForModelCandidates(ProjectCoordinate pc, String modelClassifier) {
        BooleanQuery query = new BooleanQuery();
        Term coordTerm = new Term(F_COORDINATE, format("%s:%s:*", pc.getGroupId(), pc.getArtifactId()));
        WildcardQuery coordQuery = new WildcardQuery(coordTerm);
        query.add(coordQuery, Occur.MUST);
        query.add(new TermQuery(new Term(F_CLASSIFIER, modelClassifier)), Occur.MUST);

        try {
            IndexSearcher searcher = new IndexSearcher(reader);
            TopDocs matches = searcher.search(query, 100);
            searcher.close();

            Set<String> results = Sets.newHashSet();
            for (ScoreDoc scoreDoc : matches.scoreDocs) {
                Document doc = reader.document(scoreDoc.doc);
                String modelcoord = doc.get(modelClassifier);
                results.add(modelcoord);
            }
            return results;
        } catch (Exception e) {
            return Collections.emptySet();
        }
    }

    @Override
    public ImmutableSet<ModelCoordinate> getKnownModels(String modelType) {
        List<Artifact> artifacts = findModelArchiveCoordinatesByClassifier(modelType);
        Collection<ModelCoordinate> transform = Collections2.transform(artifacts,
                new Artifact2ModelArchiveTransformer());
        return ImmutableSet.copyOf(transform);
    }

    private List<Artifact> findModelArchiveCoordinatesByClassifier(String classifier) {
        if (!isAccessible()) {
            return Collections.emptyList();
        }
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

    @Override
    public Optional<ProjectCoordinate> suggestProjectCoordinateByArtifactId(String artifactId) {
        Term t1 = new Term(F_SYMBOLIC_NAMES, artifactId);
        return findProjectCoordinateByTerm(t1);
    }

    @Override
    public Optional<ProjectCoordinate> suggestProjectCoordinateByFingerprint(String fingerprint) {
        Term t1 = new Term(F_FINGERPRINTS, fingerprint);
        return findProjectCoordinateByTerm(t1);
    }

    private Optional<ProjectCoordinate> findProjectCoordinateByTerm(Term... terms) {
        if (reader == null) {
            return absent();
        }
        BooleanQuery query = new BooleanQuery();
        for (Term t : terms) {
            TermQuery q = new TermQuery(t);
            query.add(q, Occur.MUST);
        }

        final TopDocs matches;
        try {
            IndexSearcher searcher = new IndexSearcher(reader);
            matches = searcher.search(query, MAX_DOCUMENTS_SEARCHED);
            searcher.close();
        } catch (IOException e) {
            return absent();
        }

        for (int i = 0; i < min(matches.scoreDocs.length, MAX_DOCUMENTS_SEARCHED); i++) {
            final Document doc;
            try {
                doc = reader.document(matches.scoreDocs[i].doc);
            } catch (IOException e) {
                continue;
            }

            String string = doc.get(F_COORDINATE);
            if (string == null) {
                continue;
            }

            DefaultArtifact tmp = new DefaultArtifact(string);
            Optional<ProjectCoordinate> pc = tryNewProjectCoordinate(tmp.getGroupId(), tmp.getArtifactId(),
                    canonicalizeVersion(tmp.getVersion()));
            if (pc.isPresent()) {
                return pc;
            }
        }
        // Nothing found in first MAX_DOCUMENTS_SEARCHED documents; giving up.
        return absent();
    }

    private static final class Artifact2ModelArchiveTransformer implements Function<Artifact, ModelCoordinate> {
        @Override
        public ModelCoordinate apply(Artifact a) {
            return toModelCoordinate(a);
        }

    }

    private static ModelCoordinate toModelCoordinate(Artifact a) {
        return new ModelCoordinate(a.getGroupId(), a.getArtifactId(), a.getClassifier(), a.getExtension(),
                a.getVersion());
    }
}
