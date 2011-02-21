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
package org.eclipse.recommenders.server.codesearch.lucene;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.search.Explanation;
import org.eclipse.recommenders.commons.utils.Bag;
import org.eclipse.recommenders.commons.utils.HashBag;

public class GenericInverseTermRatioFeatureScorer implements SingleFeatureScorer {

    private final IndexReader reader;
    private final Bag<Integer> relevantDocs = HashBag.newHashBag();
    private final List<Term> terms;
    private final String fieldName;

    public GenericInverseTermRatioFeatureScorer(final IndexReader reader, final List<Term> terms) throws IOException {
        this.reader = reader;
        this.terms = terms;
        this.fieldName = getFieldNameFromTerms();
    }

    private String getFieldNameFromTerms() {
        if (!terms.isEmpty()) {
            return terms.get(0).field();
        }
        return null;
    }

    @Override
    public Set<Integer> findRelevantDocs() throws IOException {
        for (final Term term : terms) {
            final TermDocs termDocs = reader.termDocs(term);
            while (termDocs.next()) {
                final int docId = termDocs.doc();
                relevantDocs.add(docId);
            }
        }
        return relevantDocs.elements();
    }

    @Override
    public Explanation explainScore(final int doc) throws IOException {
        return new Explanation(scoreDoc(doc), getIdentifier());
    }

    @Override
    public float scoreDoc(final int doc) throws IOException {
        float score = 0.0f;
        if (!terms.isEmpty()) {
            final TermFreqVector v = reader.getTermFreqVector(doc, fieldName);
            if (v != null) {
                final String[] docTerms = v.getTerms();
                final int numberOfTermsInRequestAndExample = relevantDocs.count(doc);
                final int numberOfTermsInExample = docTerms.length;
                score = 1 - numberOfTermsInRequestAndExample / (float) numberOfTermsInExample;
            }
        }
        if (Double.isNaN(score) || Double.isInfinite(score)) {
            score = 0.0f;
        }

        return (float) Math.pow(score, 2);
    }

    @Override
    public String getIdentifier() {
        return this.getClass().getSimpleName();
    }
}
