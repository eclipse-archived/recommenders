/**
 * Copyright (c) 2010, 2012 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 *    Olav Lenz - modifications for project coordinates mapping
 */
package org.eclipse.recommenders.models.dependencies.impl;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.fromNullable;
import static com.google.common.io.Closeables.closeQuietly;
import static org.eclipse.recommenders.utils.Constants.F_COORDINATE;
import static org.eclipse.recommenders.utils.Constants.F_FINGERPRINTS;
import static org.eclipse.recommenders.utils.Constants.F_SYMBOLIC_NAMES;

import java.io.Closeable;
import java.io.File;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;

/**
 * Lighter version of ModelRepositoryIndex in *.recommendrs.rcp
 */
public class SimpleIndexSearcher implements Closeable {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private Directory directory;
    private IndexReader reader;
    private final File location;

    public SimpleIndexSearcher(File location) {
        this.location = location;
    }

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

    public Optional<String> searchByArtifactId(String artifactId) {
        Term t1 = new Term(F_SYMBOLIC_NAMES, artifactId);
        return findByTerm(t1);
    }

    public Optional<String> searchByFingerprint(String fingerprint) {
        Term t1 = new Term(F_FINGERPRINTS, fingerprint);
        return findByTerm(t1);
    }

    private Optional<String> findByTerm(Term... terms) {
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
            String modelCoordinate = doc.get(F_COORDINATE);
            return fromNullable(modelCoordinate);
        } catch (Exception e) {
            return absent();
        }
    }

}
