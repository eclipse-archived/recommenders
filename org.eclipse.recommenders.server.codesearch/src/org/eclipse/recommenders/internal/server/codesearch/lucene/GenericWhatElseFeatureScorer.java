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

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.search.Explanation;
import org.eclipse.recommenders.commons.utils.Bag;
import org.eclipse.recommenders.commons.utils.HashBag;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

public class GenericWhatElseFeatureScorer implements SingleFeatureScorer {

    private final IndexReader reader;
    private final Bag<Integer> relevantDocs = HashBag.newHashBag();
    private final List<Term> terms;
    private final String fieldName;
    private HashSet<String> topElements;

    public GenericWhatElseFeatureScorer(final IndexReader reader, final List<Term> terms) throws IOException {
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
        findDocsContainingTerms();
        createRlatedTermsCounter();
        return relevantDocs.elements();
    }

    private void createRlatedTermsCounter() {

        if (fieldName == null) {
            return;
        }
        final Bag<String> otherTerms = HashBag.newHashBag();
        final Multimap<String, Integer> terms2docs = HashMultimap.create();
        for (final int docId : relevantDocs) {
            try {
                final TermFreqVector frequencyVector = reader.getTermFreqVector(docId, fieldName);
                if (frequencyVector == null) {
                    continue;
                }
                for (final String term : frequencyVector.getTerms()) {
                    otherTerms.add(term);
                    terms2docs.put(term, docId);
                }

            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
        for (final Term term : terms) {
            otherTerms.removeAll(term.text());
        }
        final List<String> top = otherTerms.topElements(5);
        topElements = Sets.newHashSet(top);

    }

    private void findDocsContainingTerms() throws IOException {
        for (final Term term : terms) {
            final TermDocs termDocs = reader.termDocs(term);
            while (termDocs.next()) {
                final int docId = termDocs.doc();
                relevantDocs.add(docId);
            }
        }
    }

    @Override
    public Explanation explainScore(final int doc) throws IOException {
        return new Explanation(scoreDoc(doc), getIdentifier());
    }

    @Override
    public float scoreDoc(final int doc) throws IOException {
        if (topElements == null || topElements.isEmpty()) {
            return 0f;
        }

        final TermFreqVector frequencyVector = reader.getTermFreqVector(doc, fieldName);
        if (frequencyVector == null) {
            return 0f;
        }
        float contains = 0;
        for (final String term : frequencyVector.getTerms()) {
            if (topElements.contains(term)) {
                contains++;
            }
        }
        final float score = contains / topElements.size();
        return score;
    }

    @Override
    public String getIdentifier() {
        return this.getClass().getSimpleName();
    }
}
