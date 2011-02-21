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
import java.util.UUID;
import java.util.zip.ZipException;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.SimpleFSDirectory;
import org.eclipse.recommenders.commons.codesearch.CodeSearchResource;
import org.eclipse.recommenders.commons.codesearch.FeatureWeights;
import org.eclipse.recommenders.commons.codesearch.Feedback;
import org.eclipse.recommenders.commons.codesearch.Proposal;
import org.eclipse.recommenders.commons.codesearch.Request;
import org.eclipse.recommenders.commons.codesearch.Response;
import org.eclipse.recommenders.commons.utils.Bag;
import org.eclipse.recommenders.commons.utils.TreeBag;
import org.eclipse.recommenders.server.codesearch.lucene.CodesearchQuery;
import org.eclipse.recommenders.server.codesearch.lucene.LuceneQueryUtil;

public class LocalCodesearchService implements CodeSearchResource {

    private static Logger log = Logger.getLogger(LocalCodesearchService.class);
    private FeatureWeights weights;
    private IndexReader luceneIndexReader;
    private File indexFolder;

    public LocalCodesearchService(final File dataFolder) throws CorruptIndexException, IOException {
        setFolders(dataFolder);
        createLuceneIndexReader();
        createTermsFrequencyIndex();
        log.info("CodeSearch server started");
    }

    private void setFolders(final File dataFolder) throws ZipException, IOException {
        indexFolder = new File(dataFolder, "index");
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

    @Override
    public Response search(final Request request) {
        final String requestId = UUID.randomUUID().toString();
        final Response response = performSearch(request);
        return response;
    }

    private Response performSearch(final Request request) {
        try {
            // TODO feature weights are null here:)
            final CodesearchQuery query = LuceneQueryUtil.toCodeSearchQuery(request, request.featureWeights);
            final StopWatch w = new StopWatch();
            w.start();
            final IndexSearcher searcher = new IndexSearcher(luceneIndexReader);
            final TopDocs search = searcher.search(query, 15);
            w.stop();
            log.info("total scoring time:" + w);
            final Response response = Response.newResponse();
            for (final ScoreDoc scoreDoc : search.scoreDocs) {
                final Proposal proposal = Proposal.newProposal();
                final Document doc = searcher.doc(scoreDoc.doc);
                final String className = doc.get("class");
                final String methodName = doc.get("method");
                response.proposals.add(proposal);
                final Explanation explain = searcher.explain(query, scoreDoc.doc);
            }
            return response;

        } catch (final Exception e) {
            return Response.newErrorResponse(e);
        }
    }

    @Override
    public void addFeedback(final String requestId, final Feedback feedback) {
        // TODO Auto-generated method stub

    }

}
