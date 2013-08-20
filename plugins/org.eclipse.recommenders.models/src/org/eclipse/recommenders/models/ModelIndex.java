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

import static com.google.common.base.Optional.*;
import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.Iterables.find;
import static java.lang.String.format;
import static org.eclipse.recommenders.utils.Constants.*;
import static org.eclipse.recommenders.utils.IOUtils.closeQuietly;

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
import org.apache.lucene.store.FSDirectory;
import org.eclipse.recommenders.utils.Artifacts;
import org.eclipse.recommenders.utils.Checks;
import org.eclipse.recommenders.utils.Version;
import org.eclipse.recommenders.utils.Versions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.util.artifact.DefaultArtifact;

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
    private File indexdir;
    private FSDirectory index;
    private IndexReader reader;
    private Logger log = LoggerFactory.getLogger(getClass());

    public ModelIndex(File indexdir) {
        this.indexdir = indexdir;
    }

    public boolean isAccessible() {
        return index != null && reader != null;
    }

    @Override
    public void open() throws IOException {
        index = FSDirectory.open(indexdir);
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

        try {
            IndexSearcher searcher = new IndexSearcher(reader);
            TopDocs matches = searcher.search(query, 5);
            searcher.close();
            if (matches.totalHits <= 0) {
                return absent();
            }
            Document doc = reader.document(matches.scoreDocs[0].doc);
            String string = doc.get(F_COORDINATE);
            if (string == null) {
                return absent();
            }
            DefaultArtifact tmp = new DefaultArtifact(string);
            ProjectCoordinate pc = new ProjectCoordinate(tmp.getGroupId(), tmp.getArtifactId(), tmp.getVersion());
            return fromNullable(pc);
        } catch (Exception e) {
            return absent();
        }
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
