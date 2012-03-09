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
import static com.google.common.io.Closeables.closeQuietly;
import static org.eclipse.recommenders.internal.rcp.repo.RepositoryUtils.newArtifact;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.eclipse.recommenders.internal.rcp.repo.wiring.GuiceModule.ModelRepositoryIndexLocation;
import org.eclipse.recommenders.rcp.repo.IModelRepositoryIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.aether.artifact.Artifact;

import com.google.common.base.Optional;

public class ModelRepositoryIndex implements Closeable, IModelRepositoryIndex {

    public static final String F_FINGERPRINTS = "fingerprints";
    public static final String F_COORDINATE = "coordinate";
    public static final String F_CLASSIFIER = "classifier";
    public static final String F_ARTIFACT_ID = "artifactId";

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
            log.error("Failed to open search index.", e);
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
        return findByTerm(t1, t2);

    }

    @Override
    public Optional<Artifact> searchByArtifactId(String artifactId, String classifier) {
        Term t1 = new Term(F_ARTIFACT_ID, artifactId);
        Term t2 = new Term(F_CLASSIFIER, classifier);

        return findByTerm(t1, t2);
    }

    private Optional<Artifact> findByTerm(Term... terms) {
        if (reader == null) {
            return absent();
        }
        IndexSearcher searcher = new IndexSearcher(reader);
        BooleanQuery query = new BooleanQuery();
        for (Term t : terms) {
            TermQuery q = new TermQuery(t);
            query.add(q, Occur.MUST);

        }

        try {
            TopDocs matches = searcher.search(query, 5);
            if (matches.totalHits == 0) {
                return absent();
            }
            if (matches.totalHits > 1) {
                log.warn("More than one potential match for query {} found. Inconsistency in model store?", query);
            }
            Document doc = reader.document(matches.scoreDocs[0].doc);
            String coordinate = doc.get(F_COORDINATE);
            if (coordinate == null) {
                return absent();
            }
            return of(newArtifact(coordinate));
        } catch (Exception e) {
            log.error("Searching index failed with exception", e);
            return absent();
        }
    }
}