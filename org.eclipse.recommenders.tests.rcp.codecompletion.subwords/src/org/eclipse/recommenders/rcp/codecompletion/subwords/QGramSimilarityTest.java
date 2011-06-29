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

import static org.eclipse.recommenders.rcp.codecompletion.subwords.QGramSimilarity.calculateQGramSimilarity;
import static org.eclipse.recommenders.rcp.codecompletion.subwords.QGramSimilarity.createNGrams;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.google.common.collect.Lists;

public class QGramSimilarityTest {

    @Test
    public void testQGramSimilarityIdentity() {
        Assert.assertEquals(1f, calculateQGramSimilarity("ab", "ab", 2));
    }

    @Test
    public void testQGramSimilarityCompletlyDifferent() {
        Assert.assertEquals(0f, calculateQGramSimilarity("ab", "de", 2));
    }

    @Test
    public void testQGramSimilarityForNoNGrams() {
        Assert.assertEquals(0f, calculateQGramSimilarity("ab", "de", 3));
    }

    @Test
    public void testQGramSimilarityPartialMatches() {
        Assert.assertEquals(0.5f, calculateQGramSimilarity("abcd", "ab", 2));
        Assert.assertEquals(0.5f, calculateQGramSimilarity("abcd", "bc", 2));
        Assert.assertEquals(0.5f, calculateQGramSimilarity("abcd", "cd", 2));

        Assert.assertEquals(0.5f, calculateQGramSimilarity("ab", "abcd", 2));
        Assert.assertEquals(0.5f, calculateQGramSimilarity("bc", "abcd", 2));
        Assert.assertEquals(0.5f, calculateQGramSimilarity("cd", "abcd", 2));
    }

    @Test
    public void testQGramSimilarityTrigrams() {
        Assert.assertEquals(0f, calculateQGramSimilarity("abcd", "ab", 3));
        Assert.assertEquals(2f / 3f, calculateQGramSimilarity("abcd", "abc", 3));
        Assert.assertEquals(2f / 3f, calculateQGramSimilarity("abcd", "bcd", 3));
    }

    @Test
    public void testMultipleOccurencesInNGrams() {
        Assert.assertEquals(0.5f, calculateQGramSimilarity("xyxy", "xy", 2));
        Assert.assertEquals(0.5f, calculateQGramSimilarity("xy", "xyxy", 2));
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
