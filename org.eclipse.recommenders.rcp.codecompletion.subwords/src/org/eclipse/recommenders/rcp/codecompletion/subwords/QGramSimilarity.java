/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Paul-Emmanuel Faidherbe - Completion proposals relevance benchmark
 */
package org.eclipse.recommenders.rcp.codecompletion.subwords;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class QGramSimilarity {

    public static float calculateQGramSimilarity(final String s1, final String s2, final int n) {
        final List<String> nGrams1 = createNGrams(s1, n);
        final List<String> nGrams2 = createNGrams(s2, n);
        final int maxDifferences = nGrams1.size() + nGrams2.size();
        if (maxDifferences == 0) {
            return 0f;
        }
        final int differences = calculateDifferences(nGrams1, nGrams2);
        return (maxDifferences - differences) / (float) maxDifferences;
    }

    private static int calculateDifferences(final List<String> nGrams1, final List<String> nGrams2) {
        final Set<String> allNGrams = Sets.newHashSet();
        allNGrams.addAll(nGrams1);
        allNGrams.addAll(nGrams2);
        int differences = 0;
        for (final String nGram : allNGrams) {
            differences += Math.abs(Collections.frequency(nGrams1, nGram) - Collections.frequency(nGrams2, nGram));
        }
        return differences;
    }

    public static int calculateMatchingNGrams(final String s1, final String s2, final int n) {
        final Set<String> nGrams1 = Sets.newHashSet(createNGrams(s1, n));
        final Set<String> nGrams2 = Sets.newHashSet(createNGrams(s2, n));
        nGrams1.retainAll(nGrams2);
        return nGrams1.size();
    }

    public static List<String> createNGrams(final String candidate, final int n) {
        final List<String> nGrams = Lists.newLinkedList();
        int start = 0;
        while (start + n <= candidate.length()) {
            nGrams.add(candidate.substring(start, start + n));
            ++start;
        }
        return nGrams;
    }
}
