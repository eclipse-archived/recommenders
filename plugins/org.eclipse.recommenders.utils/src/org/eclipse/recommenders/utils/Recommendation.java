/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.utils;

import static com.google.common.base.Objects.toStringHelper;
import static org.eclipse.recommenders.utils.Checks.*;

import com.google.common.annotations.Beta;

@Beta
public class Recommendation<T> {

    /*
     * TODO: This generic return type seems to cause compile errors with javac.
     */
    public static <S, T extends S> Recommendation<S> newRecommendation(T proposal, double relevance) {
        return new Recommendation<S>(ensureIsNotNull(proposal, "proposal cannot be null"), ensureIsInRange(relevance,
                0, 1, "relevance '%f' must be in range [0..1]", relevance));
    }

    private final T proposal;
    private final double relevance;

    protected Recommendation(T proposal, double relevance) {
        this.proposal = proposal;
        this.relevance = relevance;
    }

    public T getProposal() {
        return proposal;
    };

    public double getRelevance() {
        return relevance;
    };

    @Override
    public String toString() {
        return toStringHelper(this).add("proposal", proposal).add("probability", String.format("%.4f", relevance))
                .toString();
    }
}
