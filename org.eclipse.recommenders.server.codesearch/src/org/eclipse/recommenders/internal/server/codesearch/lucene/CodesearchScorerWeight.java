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
import java.util.Map;

import org.apache.commons.collections.map.LRUMap;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.eclipse.recommenders.commons.codesearch.FeatureWeights;

@SuppressWarnings("unchecked")
public class CodesearchScorerWeight extends org.apache.lucene.search.Weight {
    /*
     * XXX Having this cache is odd because it is only needed to quickly
     * recompute the scores of each code snippet to push these values to the
     * client... Should we think about refactoring this somehow?
     */
    private static Map<CodesearchQuery, CodesearchScorer> cache = new LRUMap(10);
    private static final long serialVersionUID = 1041182637020279041L;
    private float queryWeight;
    private final CodesearchQuery query;
    private final FeatureWeights weights;

    public CodesearchScorerWeight(final CodesearchQuery query, final FeatureWeights weights) {
        this.query = query;
        this.weights = weights;
    }

    @Override
    public Scorer scorer(final IndexReader reader, final boolean scoreDocsInOrder, final boolean topScorer)
            throws IOException {

        return findOrCreateScorer(reader);
    }

    private CodesearchScorer findOrCreateScorer(final IndexReader reader) throws IOException {
        final CodesearchScorer scorer = cache.get(query);

        return scorer != null ? scorer : new CodesearchScorer(reader, query, weights);
    }

    @Override
    public Explanation explain(final IndexReader reader, final int doc) throws IOException {
        return findOrCreateScorer(reader).explain(reader, doc);
    }

    @Override
    public Query getQuery() {
        return query;
    }

    @Override
    public float getValue() {
        return queryWeight;
    }

    @Override
    public void normalize(final float norm) {
        queryWeight *= norm;
    }

    @Override
    public float sumOfSquaredWeights() throws IOException {
        return 1;
    }
}
