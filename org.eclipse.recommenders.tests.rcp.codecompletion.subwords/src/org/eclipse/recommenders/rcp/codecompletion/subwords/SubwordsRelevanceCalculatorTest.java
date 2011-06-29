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

import static org.eclipse.recommenders.rcp.codecompletion.subwords.SubwordsRelevanceCalculator.calculateRelevance;
import static org.eclipse.recommenders.rcp.codecompletion.subwords.SubwordsRelevanceCalculator.commonPrefixLength;
import junit.framework.Assert;

import org.junit.Test;

public class SubwordsRelevanceCalculatorTest {

    @Test
    public void testNullInput() {
        Assert.assertEquals(0, calculateRelevance(null, "xyz"));
        Assert.assertEquals(0, calculateRelevance("xyz", null));
    }

    @Test
    public void testEmptyInput() {
        Assert.assertEquals(0, calculateRelevance("", "xyz"));
        Assert.assertEquals(0, calculateRelevance("xyz", ""));
    }

    @Test
    public void testCommonPrefixLength() {
        Assert.assertEquals(0, commonPrefixLength("abcde", "xyz"));
        Assert.assertEquals(3, commonPrefixLength("abcde", "abc"));
        Assert.assertEquals(3, commonPrefixLength("abcde", "abcxy"));
    }
}
