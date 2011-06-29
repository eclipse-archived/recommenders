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

public class SubwordsRelevanceCalculator {

    public static int calculateRelevance(String s1, String s2) {
        s1 = prepareString(s1);
        s2 = prepareString(s2);

        final float qGram = QGramSimilarity.calculateQGramSimilarity(s1, s2, 2);
        final int commonPrefixLength = commonPrefixLength(s1, s2);
        final int weightedResult = Math.round((commonPrefixLength + qGram * 100f) * 20f - lengthDifference(s1, s2));

        return Math.max(0, weightedResult);
    }

    private static int lengthDifference(final String s1, final String s2) {
        return Math.abs(s1.length() - s2.length());
    }

    private static String prepareString(final String s1) {
        if (s1 == null) {
            return "";
        } else {
            return s1.trim().toLowerCase();
        }
    }

    public static int commonPrefixLength(final String s1, final String s2) {
        final int minStr = Math.min(s1.length(), s2.length());
        int commonPrefixLength = 0;
        for (int i = 0; i < minStr && s1.charAt(i) == s2.charAt(i); i++) {
            ++commonPrefixLength;
        }
        return commonPrefixLength;
    }

}
