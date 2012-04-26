/**
 * Copyright (c) 2010, 2012 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.completion.rcp.subwords;

import static java.lang.Character.isLowerCase;
import static java.lang.Character.toLowerCase;
import static java.lang.Character.toUpperCase;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;

public class LCSS {

    // public static int compare(String completion, String s2) {
    // int res = 0;
    // for (int[] s1 : findSequences(completion, s2)) {
    // int score = scoreSubsequence(s1);
    // res = Math.max(res, score);
    // }
    // return res;
    // }

    /**
     * Returns the best, i.e, the longest continuous sequence - or the empty sequence if no subsequence could be found.
     */
    public static int[] bestSubsequence(String completion, String token) {
        int bestScore = -1;
        int[] bestSequence = new int[0];
        for (int[] s1 : findSequences(completion, token)) {
            int score = scoreSubsequence(s1);
            if (score > bestScore) {
                bestScore = score;
                bestSequence = s1;
            }
        }
        return bestSequence;
    }

    public static int scoreSubsequence(int[] s1) {
        int score = 0;
        for (int i = 0; i < s1.length - 1; i++) {
            if (s1[i] + 1 == s1[i + 1])
                score++;
        }
        return score;
    }

    public static List<int[]> findSequences(String completion, String token) {

        List<int[]> active = Lists.newLinkedList();
        int[] start = new int[0];
        active.add(start);

        for (int i = 0; i < token.length(); i++) {
            List<int[]> next = Lists.newLinkedList();
            outer: for (int[] s : active) {

                int startIndex = s.length == 0 ? 0 : s[s.length - 1] + 1;
                boolean wordBoundaryFound = false;

                for (int j = startIndex; j < completion.length(); j++) {
                    char c1 = completion.charAt(j);
                    wordBoundaryFound |= Character.isUpperCase(c1);

                    if (isSameIgnoreCase(c1, token.charAt(i))) {
                        if (wordBoundaryFound && !Character.isUpperCase(c1))
                            continue;

                        // wordBoundaryFound = false;
                        int[] copy = Arrays.copyOf(s, s.length + 1);
                        copy[i] = j;
                        next.add(copy);
                    }
                }
            }
            active = next;
            next = Lists.newLinkedList();
        }

        // filter
        for (Iterator<int[]> it = active.iterator(); it.hasNext();) {
            int[] candidate = it.next();
            if (candidate.length < token.length()) {
                it.remove();
                continue;
            }
        }

        return active;
    }

    private static boolean isSameIgnoreCase(char c1, char c2) {
        if (c1 == c2)
            return true;
        c2 = isLowerCase(c2) ? toUpperCase(c2) : toLowerCase(c2);
        return c1 == c2;
    }

    public static boolean containsSubsequence(String completion, String token) {
        boolean res = !findSequences(completion, token).isEmpty();
        return res;
    }

}
