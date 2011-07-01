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

import static org.eclipse.recommenders.rcp.codecompletion.subwords.SubwordsUtils.calculateMatchingNGrams;
import static org.eclipse.recommenders.rcp.codecompletion.subwords.SubwordsUtils.createNGrams;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.google.common.collect.Lists;

public class NGramUtilsTest {

    @Test
    public void testIdentity() {
        Assert.assertEquals(1, calculateMatchingNGrams("ab", "ab", 2));
    }

    @Test
    public void testCompletlyDifferent() {
        Assert.assertEquals(0, calculateMatchingNGrams("ab", "de", 2));
    }

    @Test
    public void testForShortStrings() {
        Assert.assertEquals(0, calculateMatchingNGrams("ab", "de", 3));
    }

    @Test
    public void testPartialMatches() {
        Assert.assertEquals(1, calculateMatchingNGrams("abcd", "ab", 2));
        Assert.assertEquals(1, calculateMatchingNGrams("abcd", "bc", 2));
        Assert.assertEquals(1, calculateMatchingNGrams("abcd", "cd", 2));

        Assert.assertEquals(1, calculateMatchingNGrams("ab", "abcd", 2));
        Assert.assertEquals(1, calculateMatchingNGrams("bc", "abcd", 2));
        Assert.assertEquals(1, calculateMatchingNGrams("cd", "abcd", 2));
    }

    @Test
    public void testTrigrams() {
        Assert.assertEquals(0, calculateMatchingNGrams("abcd", "ab", 3));
        Assert.assertEquals(1, calculateMatchingNGrams("abcd", "abc", 3));
        Assert.assertEquals(1, calculateMatchingNGrams("abcd", "bcd", 3));
    }

    @Test
    public void testMultipleOccurences() {
        Assert.assertEquals(1, calculateMatchingNGrams("xyxy", "xy", 2));
        Assert.assertEquals(1, calculateMatchingNGrams("xy", "xyxy", 2));
    }

    @Test
    public void test2GramCreation() {
        final List<String> nGrams = createNGrams("abcde", 2);
        Assert.assertEquals(Lists.newArrayList("ab", "bc", "cd", "de"), nGrams);
    }

    @Test
    public void test3GramCreation() {
        final List<String> nGrams = createNGrams("abcde", 3);
        Assert.assertEquals(Lists.newArrayList("abc", "bcd", "cde"), nGrams);
    }

    @Test
    public void testNGramCreationOnShortString() {
        Assert.assertEquals(0, createNGrams("ab", 3).size());
    }
}
