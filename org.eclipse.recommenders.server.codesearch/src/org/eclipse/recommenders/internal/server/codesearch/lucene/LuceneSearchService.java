/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 */
package org.eclipse.recommenders.internal.server.codesearch.lucene;

import static org.eclipse.recommenders.commons.utils.Checks.ensureIsFalse;
import static org.eclipse.recommenders.commons.utils.Checks.ensureIsNotNull;
import static org.eclipse.recommenders.commons.utils.Throws.throwUnhandledException;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
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
    private File weightsFile;

    @Inject
    public LuceneSearchService(@Named("codesearch.basedir") final File basedir) throws CorruptIndexException,
            IOException {
        initializeFilesAndFolders(basedir);
        initializeLuceneIndexReader();
        initializeTermFrequencyIndex();
        initializeFeatureWeights();
    }

    private void initializeFilesAndFolders(final File basedir) throws ZipException, IOException {
        this.baseDir = basedir;
        this.indexDir = new File(basedir, "index");
        if (!indexDir.exists()) {
            indexDir.mkdirs();
        }
        weightsFile = new File(baseDir, "weights/active-weights.json");

    }

    private void initializeLuceneIndexReader() throws IOException, CorruptIndexException {
        final SimpleFSDirectory index = new SimpleFSDirectory(indexDir);
        luceneIndexReader = IndexReader.open(index);
    }

    private void initializeTermFrequencyIndex() throws IOException {
        final TermEnum termEnum = luceneIndexReader.terms();
        final Bag<Term> termsBag = TreeBag.newTreeBag();
        while (termEnum.next()) {
            final Term term = termEnum.term();
            final int docFreq = termEnum.docFreq();
            termsBag.add(term, docFreq);
        }
    }

    private void initializeFeatureWeights() {
        try {
            this.weights = new FeatureWeights();
            this.weights.weights = GsonUtil.deserialize(weightsFile, new TypeToken<Map<String, Float>>() {
            }.getType());
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<LuceneSearchResult> search(final Request request) {
        try {
            final LinkedList<LuceneSearchResult> proposals = Lists.newLinkedList();
            final CodesearchQuery query = LuceneQueryUtil.toCodeSearchQuery(request, weights);
            final IndexSearcher searcher = new IndexSearcher(luceneIndexReader);
            final TopDocs search = searcher.search(query, 50);
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

    public void updateWeights(final FeatureWeights newWeights) {
        ensureIsNotNull(newWeights);
        ensureIsNotNull(newWeights.weights);
        ensureIsFalse(newWeights.weights.isEmpty(), "empty weights not allowed. At least one feature is required.");
        weights = newWeights;

        final File backupFile = computeNewWeightsBackupFile();
        GsonUtil.serialize(newWeights.weights, backupFile);
        GsonUtil.serialize(newWeights.weights, weightsFile);
    }

    private File computeNewWeightsBackupFile() {
        final DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd__HH.mm.ss");
        final String dateString = formatter.format(new Date());
        return new File(weightsFile.getParentFile(), "weights-" + dateString + ".json");
    }
}
