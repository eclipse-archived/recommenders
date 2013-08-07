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

import static com.google.common.collect.ComparisonChain.start;
import static org.eclipse.recommenders.utils.Checks.ensureIsInRange;

import java.util.Comparator;
import java.util.List;

import org.eclipse.recommenders.utils.names.IMethodName;

import com.google.common.annotations.Beta;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;

@Beta
public class Recommendations {

    private static Comparator<Recommendation<?>> C_BY_RELEVANCE = new Comparator<Recommendation<?>>() {

        @Override
        public int compare(final Recommendation<?> o1, final Recommendation<?> o2) {
            return start().compare(o1.getRelevance(), o2.getRelevance()).compare(o1.toString(), o2.toString()).result();
        }
    };

    private static Comparator<Recommendation<?>> C_BY_NAME = new Comparator<Recommendation<?>>() {

        @Override
        public int compare(final Recommendation<?> o1, final Recommendation<?> o2) {
            return start().compare(o1.getProposal().toString(), o2.getProposal().toString()).result();
        }
    };

    private static Predicate<Recommendation<IMethodName>> P_VOID = new Predicate<Recommendation<IMethodName>>() {

        @Override
        public boolean apply(final Recommendation<IMethodName> input) {
            return !input.getProposal().isVoid();
        }
    };

    private static Predicate<Recommendation<?>> newMinimumRelevancePredicate(final double min) {
        return new Predicate<Recommendation<?>>() {

            @Override
            public boolean apply(final Recommendation<?> input) {
                return input.getRelevance() >= min;
            }
        };
    }

    /**
     * Returns the top k elements of the given list of recommendations, sorted by proposal relevance in descending
     * order.
     */
    public static <T> List<Recommendation<T>> top(final Iterable<Recommendation<T>> recommendations,
            final int numberOfTopElements) {
        return Ordering.from(C_BY_RELEVANCE).greatestOf(recommendations, numberOfTopElements);
    }

    /**
     * Returns the top k elements of the given list of recommendations that satisfy the minimum relevance criterion,
     * sorted by proposal relevance in descending order.
     */
    public static <T> List<Recommendation<T>> top(final Iterable<Recommendation<T>> recommendations,
            final int numberOfTopElements, final double minRelevance) {
        return Ordering.from(C_BY_RELEVANCE).greatestOf(filterRelevance(recommendations, minRelevance),
                numberOfTopElements);
    }

    /**
     * Filters all void methods from the list of method recommendations.
     */
    public static Iterable<Recommendation<IMethodName>> filterVoid(
            final Iterable<Recommendation<IMethodName>> recommendations) {
        return Iterables.filter(recommendations, P_VOID);
    };

    /**
     * Filters all proposals whose relevance is below the given threshold.
     */
    public static <T> Iterable<Recommendation<T>> filterRelevance(final Iterable<Recommendation<T>> recommendations,
            final double min) {
        return Iterables.filter(recommendations, newMinimumRelevancePredicate(min));
    };

    /**
     * Sorts the given list of proposals lexicographically in ascending order by the outcome of the proposal's toString
     * output.
     */
    public static <T> List<Recommendation<T>> sortByName(final Iterable<Recommendation<T>> recommendations) {
        return Ordering.from(C_BY_NAME).sortedCopy(recommendations);
    }

    /**
     * Sorts the given list of proposals in ascending order by the recommendation's relevance.
     */
    public static <T> List<Recommendation<T>> sortByRelevance(final Iterable<Recommendation<T>> recommendations) {
        return Ordering.from(C_BY_RELEVANCE).reverse().sortedCopy(recommendations);
    }

    /**
     * Returns the relevance of the give proposal multiplied by 100 and rounded to the next Integer. Note that this
     * method checks that the relevance is in the range of [0, 1].
     */
    public static int asPercentage(Recommendation<?> recommendation) {
        double rel = recommendation.getRelevance();
        ensureIsInRange(rel, 0, 1, "relevance '%f' not in interval [0, 1]", rel);
        return (int) Math.round(rel * 100);
    }
}
