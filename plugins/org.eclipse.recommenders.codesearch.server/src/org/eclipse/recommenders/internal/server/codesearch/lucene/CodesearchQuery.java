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

import static java.lang.String.format;

import java.io.IOException;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.eclipse.recommenders.codesearch.FeatureWeights;
import org.eclipse.recommenders.codesearch.Request;

public class CodesearchQuery extends Query {

    private static final long serialVersionUID = -549181541659460559L;
    private final Request request;
    private final FeatureWeights weights;

    public CodesearchQuery(final Request request, final FeatureWeights weights) {
        this.request = request;
        this.weights = weights;
    }

    @Override
    public CodesearchScorerWeight createWeight(final Searcher searcher) throws IOException {
        return new CodesearchScorerWeight(this, weights);
    }

    @Override
    public String toString(final String field) {
        return format("tostring not implemented properly: field %s", field);
    }

    public Request getRequest() {
        return request;
    }
}
