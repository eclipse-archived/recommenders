/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Sewe - initial API and implementation.
 */
package org.eclipse.recommenders.testing;

import static java.lang.String.format;

import org.eclipse.recommenders.utils.Recommendation;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

public class RecommendationMatchers {

    private static final double DELTA = 0.01;

    public static <T> Matcher<Recommendation<T>> recommendation(T method, double relevance) {
        return new RecommendationMatcher<T>(method, relevance);
    }

    private static class RecommendationMatcher<T> extends BaseMatcher<Recommendation<T>> {

        private final T expectedProposal;
        private final double expectedRelevance;

        public RecommendationMatcher(T expectedProposal, double expectedRelevance) {
            this.expectedProposal = expectedProposal;
            this.expectedRelevance = expectedRelevance;
        }

        @Override
        public boolean matches(Object object) {
            if (object instanceof Recommendation) {
                @SuppressWarnings("unchecked")
                Recommendation<T> recommendation = (Recommendation<T>) object;
                return recommendation.getProposal().equals(expectedProposal)
                        && inRange(recommendation.getRelevance(), expectedRelevance);
            } else {
                return false;
            }
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("a recommendation of ").appendValue(expectedProposal)
                    .appendText(format(" with relevance %1.2f +/- %1.2f", expectedRelevance, DELTA));
        }

        public boolean inRange(double actual, double expected) {
            return actual == expected || Math.abs(expected - actual) <= DELTA;
        }
    };
}
