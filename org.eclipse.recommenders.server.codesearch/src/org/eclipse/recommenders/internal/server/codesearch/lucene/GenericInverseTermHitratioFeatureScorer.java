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

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.search.Explanation;
import org.eclipse.recommenders.commons.utils.Bag;
import org.eclipse.recommenders.commons.utils.HashBag;

public class GenericInverseTermHitratioFeatureScorer implements SingleFeatureScorer {

    private final IndexReader reader;
    private final Bag<Integer> relevantDocs = HashBag.newHashBag();
    private final List<Term> terms;
    private final String fieldName;

    public GenericInverseTermHitratioFeatureScorer(final IndexReader reader, final List<Term> terms) throws IOException {
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
        if (terms.isEmpty()) {
            return 0;
        }
        final int numberOfTermsInExampleAndRequest = relevantDocs.count(doc);
        final int numberOfTermsInRequest = terms.size();
        final float hitratio = numberOfTermsInExampleAndRequest / (float) numberOfTermsInRequest;
        final float score = 1 - hitratio;
        return (float) Math.pow(score, 2);
    }

    @Override
    public String getIdentifier() {
        return this.getClass().getSimpleName();
    }
}
