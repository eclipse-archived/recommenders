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

import com.google.common.collect.Lists;

public class GenericTFIDFFeatureScorer implements SingleFeatureScorer {

    private final IndexReader reader;
    private final List<Term> terms;
    private final List<Float> inverseDocumentFrequencies;
    private final String fieldName;

    public GenericTFIDFFeatureScorer(final IndexReader reader, final List<Term> terms) throws IOException {
        this.reader = reader;
        this.terms = terms;
        this.fieldName = getFieldNameFromTerms();
        this.inverseDocumentFrequencies = getInverseDocumentFrequencies();
    }

    private String getFieldNameFromTerms() {
        if (!terms.isEmpty()) {
            return terms.get(0).field();
        }
        return null;
    }

    private List<Float> getInverseDocumentFrequencies() {
        final List<Float> inverseDocumentFrequencies = Lists.newArrayList();
        final float documentsCount = reader.numDocs();
        for (final Term term : terms) {
            try {
                float documentsWithTermCount = reader.docFreq(term) + 1;
                if (documentsWithTermCount < 10) {
                    documentsWithTermCount = setExtremlyRareUsedPenalty(documentsCount);
                }
                inverseDocumentFrequencies.add((float) Math.log10(documentsCount / documentsWithTermCount));
            } catch (final IOException e) {
                // TODO: Exception
                e.printStackTrace();
                inverseDocumentFrequencies.add(0f);
            }
        }
        return inverseDocumentFrequencies;
    }

    private float setExtremlyRareUsedPenalty(final float numberOfDocuments) {
        return numberOfDocuments / 2;
    }

    @Override
    public Set<Integer> findRelevantDocs() throws IOException {
        final Bag<Integer> docIds = HashBag.newHashBag();
        for (final Term term : terms) {
            final TermDocs termDocs = reader.termDocs(term);
            while (termDocs.next()) {
                final int docId = termDocs.doc();
                docIds.add(docId);
            }
        }
        return docIds.elements();
    }

    @Override
    public float scoreDoc(final int doc) throws IOException {
        float score = 0.0f;
        if (!terms.isEmpty()) {
            final TermFreqVector frequencyVector = reader.getTermFreqVector(doc, fieldName);
            if (frequencyVector == null) {
                // XXX workaround for NPE: at
                // org.eclipselabs.cr.examples.server.scorer.GenericTFIDFFeatureScorer.scoreDoc(GenericTFIDFFeatureScorer.java:78)
                return 0;
            }
            for (int i = 0; i < terms.size(); i++) {
                final Term term = terms.get(i);
                final int termIndex = frequencyVector.indexOf(term.text());
                if (termIndex > -1) {
                    final float frequency = frequencyVector.getTermFrequencies()[termIndex];
                    score += frequency * inverseDocumentFrequencies.get(i);
                }
            }
            if (score > 0) {
                score = score / terms.size();
                float numberOfTermsInDocument = 0.0f;
                for (final int frequency : frequencyVector.getTermFrequencies()) {
                    numberOfTermsInDocument += frequency;
                }
                score = score / numberOfTermsInDocument;
                if (Double.isNaN(score) || Double.isInfinite(score)) {
                    score = 0;
                }
            }
        }
        return score;
    }

    @Override
    public Explanation explainScore(final int doc) throws IOException {
        return new Explanation(scoreDoc(doc), getIdentifier());
    }

    @Override
    public String getIdentifier() {
        return this.getClass().getSimpleName();
    }
}
