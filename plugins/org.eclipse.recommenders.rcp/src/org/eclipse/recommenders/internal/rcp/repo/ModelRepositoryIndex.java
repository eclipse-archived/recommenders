/**
 * Copyright (c) 2010, 2012 Darmstadt University of Technology.
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
import static org.eclipse.recommenders.utils.Constants.*;
import static com.google.common.base.Optional.of;
import static com.google.common.io.Closeables.closeQuietly;
import static org.eclipse.recommenders.internal.rcp.repo.RepositoryUtils.newArtifact;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.eclipse.recommenders.internal.rcp.wiring.RecommendersModule.ModelRepositoryIndexLocation;
import org.eclipse.recommenders.rcp.repo.IModelRepositoryIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.aether.artifact.Artifact;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

public class ModelRepositoryIndex implements Closeable, IModelRepositoryIndex {

    public static final Artifact INDEX_ARTIFACT = newArtifact(R_COORD_INDEX);

    private Logger log = LoggerFactory.getLogger(getClass());
    private Directory directory;
    private IndexReader reader;
    private final File location;

    @Inject
    public ModelRepositoryIndex(@ModelRepositoryIndexLocation File location) throws IOException {
        this.location = location;
    }

    @Override
    public void open() {
        try {
            directory = FSDirectory.open(location);
            reader = IndexReader.open(directory);
        } catch (Exception e) {
            log.error("Failed to open search index.", e); //$NON-NLS-1$
        }
    }

    @Override
    public void close() {
        closeQuietly(reader);
        closeQuietly(directory);
    }

    public boolean exists() {
        return location.exists();
    }

    @Override
    public File getLocation() {
        return location;
    }

    @Override
    public Optional<Artifact> searchByFingerprint(String fingerprint, String classifier) {
        Term t1 = new Term(F_FINGERPRINTS, fingerprint);
        Term t2 = new Term(F_CLASSIFIER, classifier);
        return findByTerm(classifier, t1, t2);

    }

    @Override
    public Optional<Artifact> searchByArtifactId(String artifactId, String classifier) {
        Term t1 = new Term(F_SYMBOLIC_NAMES, artifactId);
        Term t2 = new Term(F_CLASSIFIER, classifier);

        return findByTerm(classifier, t1, t2);
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
                if (value != null)
                    res.add(newArtifact(value));
            }
        } catch (Exception e) {
            log.error("Searching index failed with exception", e); //$NON-NLS-1$
        }
        return res;
    }

    private Optional<Artifact> findByTerm(String classifier, Term... terms) {
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
            if (matches.totalHits == 0) {
                return absent();
            }
            if (matches.totalHits > 1) {
                log.warn("More than one potential match for query {} found. Inconsistency in model store?", query); //$NON-NLS-1$
            }
            Document doc = reader.document(matches.scoreDocs[0].doc);
            String modelCoordinate = doc.get(classifier);
            if (modelCoordinate == null) {
                return absent();
            }
            return of(newArtifact(modelCoordinate));
        } catch (Exception e) {
            log.error("Searching index failed with exception", e); //$NON-NLS-1$
            return absent();
        }
    }
}
