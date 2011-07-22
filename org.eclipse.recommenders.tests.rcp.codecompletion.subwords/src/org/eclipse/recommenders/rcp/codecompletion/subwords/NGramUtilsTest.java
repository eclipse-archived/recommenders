/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 */
package org.eclipse.recommenders.rcp.codecompletion.subwords;

import static junit.framework.Assert.assertEquals;
import static org.eclipse.recommenders.rcp.codecompletion.subwords.SubwordsUtils.calculateMatchingNGrams;
import static org.eclipse.recommenders.rcp.codecompletion.subwords.SubwordsUtils.createLowerCaseNGrams;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

public class NGramUtilsTest {

    @Test
    public void testIdentity() {
        assertEquals(1, calculateMatchingNGrams("ab", "ab", 2));
    }

    @Test
    public void testCompletlyDifferent() {
        assertEquals(0, calculateMatchingNGrams("ab", "de", 2));
    }

    @Test
    public void testForShortStrings() {
        assertEquals(0, calculateMatchingNGrams("ab", "de", 3));
    }

    @Test
    public void testPartialMatches() {
        assertEquals(1, calculateMatchingNGrams("abcd", "ab", 2));
        assertEquals(1, calculateMatchingNGrams("abcd", "bc", 2));
        assertEquals(1, calculateMatchingNGrams("abcd", "cd", 2));

        assertEquals(1, calculateMatchingNGrams("ab", "abcd", 2));
        assertEquals(1, calculateMatchingNGrams("bc", "abcd", 2));
        assertEquals(1, calculateMatchingNGrams("cd", "abcd", 2));
    }

    @Test
    public void testTrigrams() {
        assertEquals(0, calculateMatchingNGrams("abcd", "ab", 3));
        assertEquals(1, calculateMatchingNGrams("abcd", "abc", 3));
        assertEquals(1, calculateMatchingNGrams("abcd", "bcd", 3));
    }

    @Test
    public void testMultipleOccurences() {
        assertEquals(1, calculateMatchingNGrams("xyxy", "xy", 2));
        assertEquals(1, calculateMatchingNGrams("xy", "xyxy", 2));
    }

    @Test
    public void test2GramCreation() {
        final List<String> nGrams = createLowerCaseNGrams("abcde", 2);
        assertEquals(Lists.newArrayList("ab", "bc", "cd", "de"), nGrams);
    }

    @Test
    public void test3GramCreation() {
        final List<String> nGrams = createLowerCaseNGrams("abcde", 3);
        assertEquals(Lists.newArrayList("abc", "bcd", "cde"), nGrams);
    }

    @Test
    public void testNGramCreationOnShortString() {
        assertEquals(0, createLowerCaseNGrams("ab", 3).size());
    }

    @Test
    public void testSimpleMatch() {
        assertEquals(1, calculateMatchingNGrams("xyz", "xy", 2));
    }

    @Test
    public void testMultipleMatch() {
        assertEquals(1, calculateMatchingNGrams("xyzabxy", "xy", 2));
    }

}
