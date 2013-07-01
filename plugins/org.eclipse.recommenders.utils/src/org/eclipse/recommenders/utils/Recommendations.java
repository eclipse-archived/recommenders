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

import java.util.Comparator;
import java.util.List;

import org.eclipse.recommenders.utils.names.IMethodName;

import com.google.common.annotations.Beta;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;

@Beta
public class Recommendations {

    public static final Comparator<Recommendation<?>> BY_RELEVANCE = new Comparator<Recommendation<?>>() {

        @Override
        public int compare(Recommendation<?> o1, Recommendation<?> o2) {
            return start().compare(o1.getRelevance(), o2.getRelevance()).compare(o1.toString(), o2.toString()).result();
        }
    };

    public static final Comparator<Recommendation<?>> BY_NAME = new Comparator<Recommendation<?>>() {

        @Override
        public int compare(Recommendation<?> o1, Recommendation<?> o2) {
            return start().compare(o1.getProposal().toString(), o2.getProposal().toString()).result();
        }
    };

    private static final Predicate<Recommendation<IMethodName>> VOID = new Predicate<Recommendation<IMethodName>>() {

        @Override
        public boolean apply(Recommendation<IMethodName> input) {
            return !input.getProposal().isVoid();
        }
    };

    /**
     * Returns the top k elements of the given iterable, sorted by proposal relevance in descending order.
     */
    public static <T> List<Recommendation<T>> top(Iterable<Recommendation<T>> recommendations, int numberOfTopElements) {
        return Ordering.from(BY_RELEVANCE).greatestOf(recommendations, numberOfTopElements);
    }

    /**
     * Filters all void methods from the list of method recommendations.
     */
    public static Iterable<Recommendation<IMethodName>> filterVoid(Iterable<Recommendation<IMethodName>> recommendations) {
        return Iterables.filter(recommendations, VOID);
    };

    /**
     * Sorts the given list of proposals lexicographically in ascending order by the outcome of the proposal's toString
     * output.
     */
    public static <T> List<Recommendation<T>> sortByName(Iterable<Recommendation<T>> recommendations) {
        return Ordering.from(BY_NAME).sortedCopy(recommendations);
    }
}
