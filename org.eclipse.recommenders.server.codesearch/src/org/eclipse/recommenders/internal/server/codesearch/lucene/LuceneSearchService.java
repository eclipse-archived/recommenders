/**
 * Copyright (c) 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.server.codesearch.lucene;

import static org.eclipse.recommenders.commons.utils.Throws.throwUnhandledException;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipException;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.SimpleFSDirectory;
import org.eclipse.recommenders.commons.codesearch.FeatureWeights;
import org.eclipse.recommenders.commons.codesearch.Request;
import org.eclipse.recommenders.commons.utils.Bag;
import org.eclipse.recommenders.commons.utils.TreeBag;
import org.eclipse.recommenders.commons.utils.gson.GsonUtil;

import com.google.common.collect.Lists;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class LuceneSearchService {

    private static Logger log = Logger.getLogger(LuceneSearchService.class);
    private FeatureWeights weights;
    private IndexReader luceneIndexReader;
    private File baseDir;
    private File indexDir;

    @Inject
    public LuceneSearchService(@Named("codesearch.basedir") final File basedir) throws CorruptIndexException,
            IOException {
        setFolders(basedir);
        createLuceneIndexReader();
        createTermsFrequencyIndex();
        loadDefaultWeights();
    }

    private void setFolders(final File basedir) throws ZipException, IOException {
        this.baseDir = basedir;
        this.indexDir = new File(basedir, "index");
        if (!indexDir.exists()) {
            indexDir.mkdirs();
        }
    }

    private void createLuceneIndexReader() throws IOException, CorruptIndexException {
        final SimpleFSDirectory index = new SimpleFSDirectory(indexDir);
        luceneIndexReader = IndexReader.open(index);
    }

    private void createTermsFrequencyIndex() throws IOException {
        final TermEnum termEnum = luceneIndexReader.terms();
        final Bag<Term> termsBag = TreeBag.newTreeBag();
        while (termEnum.next()) {
            final Term term = termEnum.term();
            final int docFreq = termEnum.docFreq();
            termsBag.add(term, docFreq);
        }
    }

    private void loadDefaultWeights() {
        try {
            this.weights = new FeatureWeights();
            this.weights.weights = GsonUtil.deserialize(new File(baseDir, "weights.json"),
                    new TypeToken<Map<String, Float>>() {
                    }.getType());
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<LuceneSearchResult> search(final Request request) {
        try {
            final LinkedList<LuceneSearchResult> proposals = Lists.newLinkedList();
            final FeatureWeights weights = request.featureWeights == null ? this.weights : request.featureWeights;
            final CodesearchQuery query = LuceneQueryUtil.toCodeSearchQuery(request, weights);
            final IndexSearcher searcher = new IndexSearcher(luceneIndexReader);
            final TopDocs search = searcher.search(query, 15);
            for (final ScoreDoc scoreDoc : search.scoreDocs) {
                final Document doc = searcher.doc(scoreDoc.doc);
                final LuceneSearchResult searchResult = LuceneSearchResult.create(scoreDoc.score, doc.get("id"),
                        scoreDoc.doc, null);
                proposals.add(searchResult);
            }
            return proposals;

        } catch (final Exception e) {
            throw throwUnhandledException(e);
        }
    }

    public ScoringExplanation explainScore(final Request request, final int luceneDocumentId) {
        try {
            final CodesearchQuery query = LuceneQueryUtil.toCodeSearchQuery(request, weights);
            final IndexSearcher searcher = new IndexSearcher(luceneIndexReader);
            final ScoringExplanation explain = (ScoringExplanation) searcher.explain(query, luceneDocumentId);
            return explain;
        } catch (final Exception e) {
            throw throwUnhandledException(e);
        }
    }
}
