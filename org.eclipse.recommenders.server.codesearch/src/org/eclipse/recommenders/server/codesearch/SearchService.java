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
package org.eclipse.recommenders.server.codesearch;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipException;

import org.apache.commons.lang3.time.StopWatch;
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
import org.eclipse.recommenders.server.codesearch.lucene.CodesearchQuery;
import org.eclipse.recommenders.server.codesearch.lucene.LuceneQueryUtil;

import com.google.common.collect.Lists;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class SearchService {

    private static Logger log = Logger.getLogger(SearchService.class);
    private FeatureWeights weights;
    private IndexReader luceneIndexReader;
    private File indexFolder;

    @Inject
    public SearchService(@Named("codesearch.index") final File indexFolder) throws CorruptIndexException, IOException {
        setFolders(indexFolder);
        createLuceneIndexReader();
        createTermsFrequencyIndex();
        loadDefaultWeights();
        log.info("CodeSearch server started");
    }

    private void setFolders(final File indexFolder) throws ZipException, IOException {
        this.indexFolder = indexFolder;
        if (!indexFolder.exists()) {
            indexFolder.mkdirs();
        }
    }

    private void createLuceneIndexReader() throws IOException, CorruptIndexException {
        final SimpleFSDirectory index = new SimpleFSDirectory(indexFolder);
        luceneIndexReader = IndexReader.open(index);
        System.out.printf("# docs in index: %d\n", luceneIndexReader.maxDoc());
    }

    private void createTermsFrequencyIndex() throws IOException {
        final TermEnum termEnum = luceneIndexReader.terms();
        final Bag<Term> termsBag = TreeBag.newTreeBag();
        while (termEnum.next()) {
            final Term term = termEnum.term();
            final int docFreq = termEnum.docFreq();
            termsBag.add(term, docFreq);
        }
        log.debug(termsBag.totalElementsCount());
    }

    private void loadDefaultWeights() {
        try {
            this.weights = new FeatureWeights();
            this.weights.weights = GsonUtil.deserialize(new File(indexFolder, "weights.json"),
                    new TypeToken<Map<String, Float>>() {
                    }.getType());
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<SearchResult> search(final Request request) {
        try {
            final LinkedList<SearchResult> proposals = Lists.newLinkedList();
            final FeatureWeights weights = (request.featureWeights == null) ? this.weights : request.featureWeights;
            final CodesearchQuery query = LuceneQueryUtil.toCodeSearchQuery(request, weights);
            final StopWatch w = new StopWatch();
            w.start();
            final IndexSearcher searcher = new IndexSearcher(luceneIndexReader);
            final TopDocs search = searcher.search(query, 15);
            w.stop();
            log.info("total scoring time:" + w);
            for (final ScoreDoc scoreDoc : search.scoreDocs) {
                final Document doc = searcher.doc(scoreDoc.doc);
                proposals.add(new SearchResult(scoreDoc.score, doc.get("id")));
            }
            return proposals;

        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
