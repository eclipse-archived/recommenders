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

import java.util.ArrayList;
import java.util.List;

public class SubwordsNGramsDistance {
    private static boolean forTest = false;

    public static int calculateRelevanceDistance(String s1, String s2, final int windowLength) {
        if (s1 == null || "".equals(s1) || s2 == null || "".equals(s2)) {
            return 0;
        }

        s1 = s1.trim();
        s2 = s2.trim();

        final List<String> qgramsS1 = new ArrayList<String>();
        final List<String> qgramsS2 = new ArrayList<String>();

        if (s1.length() >= windowLength && s2.length() >= windowLength) {
            int start = 0;
            while (start + windowLength <= s1.length()) {
                qgramsS1.add(s1.substring(start, start + windowLength).toLowerCase());
                ++start;
            }
            start = 0;
            while (start + windowLength <= s2.length()) {
                qgramsS2.add(s2.substring(start, start + windowLength).toLowerCase());
                ++start;
            }
        }

        if (forTest) {
            System.out.println(s1 + " = " + qgramsS1);
            System.out.println(s2 + " = " + qgramsS2);
        }

        qgramsS1.retainAll(qgramsS2);

        if (forTest) {
            System.out.println("Common " + windowLength + "grams : " + qgramsS1);
        }

        int qgrams = qgramsS1.size();

        if (forTest) {
            System.out.println(windowLength + "-grams : " + qgrams);
        }

        /*
         * Common prefix weight
         */
        final int minStr = Math.min(s1.length(), s2.length());
        int commonPrefixLength = 0;
        int i = 0;
        while (i < minStr && s1.charAt(i) == s2.charAt(i)) {
            ++commonPrefixLength;
            ++i;
        }
        qgrams = Math.round((commonPrefixLength + qgrams) * 20 - (Math.abs(s1.length() - s2.length())));

        if (forTest) {
            System.out.println(windowLength + "-grams pounded : " + qgrams);
        }

        return qgrams;
    }

    public static int calculateRelevanceDistance(final String s1, final String s2) {
        return calculateRelevanceDistance(s1, s2, 3);
    }
}
