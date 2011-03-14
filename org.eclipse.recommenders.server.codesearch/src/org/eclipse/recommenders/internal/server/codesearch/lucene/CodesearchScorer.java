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

import static org.eclipse.recommenders.commons.utils.Throws.throwNotImplemented;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.DefaultSimilarity;
import org.eclipse.recommenders.commons.codesearch.FeatureWeights;
import org.eclipse.recommenders.internal.server.codesearch.lucene.FeatureScorers.CallsHitratioFeatureScorer;
import org.eclipse.recommenders.internal.server.codesearch.lucene.FeatureScorers.CallsInverseHitratioFeatureScorer;
import org.eclipse.recommenders.internal.server.codesearch.lucene.FeatureScorers.CallsInverseRatioFeatureScorer;
import org.eclipse.recommenders.internal.server.codesearch.lucene.FeatureScorers.CallsRatioFeatureScorer;
import org.eclipse.recommenders.internal.server.codesearch.lucene.FeatureScorers.CallsTFIDFFeatureScorer;
import org.eclipse.recommenders.internal.server.codesearch.lucene.FeatureScorers.CallsWhatElseFeatureScorer;
import org.eclipse.recommenders.internal.server.codesearch.lucene.FeatureScorers.FieldsHitratioFeatureScorer;
import org.eclipse.recommenders.internal.server.codesearch.lucene.FeatureScorers.FieldsInverseRatioFeatureScorer;
import org.eclipse.recommenders.internal.server.codesearch.lucene.FeatureScorers.FieldsRatioFeatureScorer;
import org.eclipse.recommenders.internal.server.codesearch.lucene.FeatureScorers.FieldsTFIDFFeatureScorer;
import org.eclipse.recommenders.internal.server.codesearch.lucene.FeatureScorers.InterfacesHitratioFeatureScorer;
import org.eclipse.recommenders.internal.server.codesearch.lucene.FeatureScorers.InterfacesInverseRatioFeatureScorer;
import org.eclipse.recommenders.internal.server.codesearch.lucene.FeatureScorers.InterfacesRatioFeatureScorer;
import org.eclipse.recommenders.internal.server.codesearch.lucene.FeatureScorers.InterfacesTFIDFFeatureScorer;
import org.eclipse.recommenders.internal.server.codesearch.lucene.FeatureScorers.OverridesHitratioFeatureScorer;
import org.eclipse.recommenders.internal.server.codesearch.lucene.FeatureScorers.OverridesInverseRatioFeatureScorer;
import org.eclipse.recommenders.internal.server.codesearch.lucene.FeatureScorers.OverridesRatioFeatureScorer;
import org.eclipse.recommenders.internal.server.codesearch.lucene.FeatureScorers.OverridesTFIDFFeatureScorer;
import org.eclipse.recommenders.internal.server.codesearch.lucene.FeatureScorers.SuperclassHitratioFeatureScorer;
import org.eclipse.recommenders.internal.server.codesearch.lucene.FeatureScorers.SuperclassInverseRatioFeatureScorer;
import org.eclipse.recommenders.internal.server.codesearch.lucene.FeatureScorers.SuperclassRatioFeatureScorer;
import org.eclipse.recommenders.internal.server.codesearch.lucene.FeatureScorers.SuperclassTFIDFFeatureScorer;
import org.eclipse.recommenders.internal.server.codesearch.lucene.FeatureScorers.UsesHitratioFeatureScorer;
import org.eclipse.recommenders.internal.server.codesearch.lucene.FeatureScorers.UsesInverseHitratioFeatureScorer;
import org.eclipse.recommenders.internal.server.codesearch.lucene.FeatureScorers.UsesInverseRatioFeatureScorer;
import org.eclipse.recommenders.internal.server.codesearch.lucene.FeatureScorers.UsesRatioFeatureScorer;
import org.eclipse.recommenders.internal.server.codesearch.lucene.FeatureScorers.UsesTFIDFFeatureScorer;
import org.eclipse.recommenders.internal.server.codesearch.lucene.FeatureScorers.UsesWhatElseFeatureScorer;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class CodesearchScorer extends org.apache.lucene.search.Scorer {

    private final LinkedHashMap<SingleFeatureScorer, Float> subScorers;
    private final Set<Integer> relevantDocs = Sets.newTreeSet();
    private Iterator<Integer> docsIterator;
    private int currentDoc;
    private final FeatureWeights weights;

    public CodesearchScorer(final IndexReader reader, final CodesearchQuery query, final FeatureWeights weights)
            throws IOException {
        super(new DefaultSimilarity());
        this.weights = weights;

        subScorers = Maps.newLinkedHashMap();
        {
            // uses scorer
            addSubScorer(new UsesRatioFeatureScorer(reader, query));
            addSubScorer(new UsesHitratioFeatureScorer(reader, query));
            addSubScorer(new UsesInverseHitratioFeatureScorer(reader, query));
            addSubScorer(new UsesInverseRatioFeatureScorer(reader, query));
            addSubScorer(new UsesWhatElseFeatureScorer(reader, query));
            addSubScorer(new UsesTFIDFFeatureScorer(reader, query));
        }
        {
            // calls scorer
            addSubScorer(new CallsRatioFeatureScorer(reader, query));
            addSubScorer(new CallsHitratioFeatureScorer(reader, query));
            addSubScorer(new CallsInverseHitratioFeatureScorer(reader, query));
            addSubScorer(new CallsInverseRatioFeatureScorer(reader, query));
            addSubScorer(new CallsTFIDFFeatureScorer(reader, query));
            addSubScorer(new CallsWhatElseFeatureScorer(reader, query));
        }
        {
            // extends scorer
            addSubScorer(new SuperclassRatioFeatureScorer(reader, query));
            addSubScorer(new SuperclassHitratioFeatureScorer(reader, query));
            addSubScorer(new SuperclassInverseRatioFeatureScorer(reader, query));
            addSubScorer(new SuperclassTFIDFFeatureScorer(reader, query));
        }
        {
            // implements scorer
            addSubScorer(new InterfacesHitratioFeatureScorer(reader, query));
            addSubScorer(new InterfacesRatioFeatureScorer(reader, query));
            addSubScorer(new InterfacesInverseRatioFeatureScorer(reader, query));
            addSubScorer(new InterfacesTFIDFFeatureScorer(reader, query));
        }
        {
            // method overrides scorer
            addSubScorer(new OverridesRatioFeatureScorer(reader, query));
            addSubScorer(new OverridesHitratioFeatureScorer(reader, query));
            addSubScorer(new OverridesInverseRatioFeatureScorer(reader, query));
            addSubScorer(new OverridesTFIDFFeatureScorer(reader, query));
        }
        {
            addSubScorer(new FieldsRatioFeatureScorer(reader, query));
            addSubScorer(new FieldsHitratioFeatureScorer(reader, query));
            addSubScorer(new FieldsInverseRatioFeatureScorer(reader, query));
            addSubScorer(new FieldsTFIDFFeatureScorer(reader, query));
        }
        findRelevantDocuments();
    }

    private void addSubScorer(final SingleFeatureScorer subscorer) {

        subScorers.put(subscorer, weights.getWeight(subscorer.getIdentifier()));

    }

    private void findRelevantDocuments() throws IOException {
        for (final SingleFeatureScorer scorer : subScorers.keySet()) {
            // StopWatch w = new StopWatch();
            // w.start();

            final Set<Integer> docs = scorer.findRelevantDocs();
            relevantDocs.addAll(docs);
            // w.stop();
            // System.out.printf("find relevant docs for %s took %s\n",
            // scorer.getClass(), w);
        }
        docsIterator = relevantDocs.iterator();
    }

    @Override
    public float score() throws IOException {
        float score = 0.0f;
        try {
            for (final SingleFeatureScorer scorer : subScorers.keySet()) {
                final float featureScore = scorer.scoreDoc(currentDoc);
                final float featureWeight = subScorers.get(scorer);
                final float subscore = featureWeight * featureScore;
                score += subscore;
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return score;
    }

    @Override
    public int advance(final int target) throws IOException {
        throw throwNotImplemented();
    }

    @Override
    public int docID() {
        return currentDoc;
    }

    @Override
    public int nextDoc() throws IOException {
        if (!docsIterator.hasNext()) {
            return NO_MORE_DOCS;
        }
        currentDoc = docsIterator.next();
        return docID();
    }

    public ScoringExplanation explain(final IndexReader reader, final int doc) {
        final ScoringExplanation e = new ScoringExplanation();
        e.luceneDocumentId = doc;
        float score = 0.0f;
        try {
            for (final SingleFeatureScorer subScorer : subScorers.keySet()) {
                final float featureWeight = subScorers.get(subScorer);
                final float featureScore = subScorer.scoreDoc(doc);
                final float subScore = featureWeight * featureScore;
                score += subScore;
                //
                e.addSubScore(subScorer.getIdentifier(), featureScore, featureWeight);
            }
        } catch (final Exception x) {
            x.printStackTrace();
        }
        e.score = score;
        return e;
    }
}
