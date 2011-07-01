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
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.eclipse.recommenders.rcp.codecompletion.subwords.SubwordsRelevanceCalculator.PREFIX_BONUS;

import org.junit.Test;

public class SubwordsRelevanceCalculatorTest {

    private static SubwordsRelevanceCalculator createSut(final String token, final String completion) {
        final SubwordsRelevanceCalculator sut = new SubwordsRelevanceCalculator(token);
        sut.setCompletion(completion);
        sut.setJdtRelevance(0);
        return sut;
    }

    @Test
    public void testEmptyToken() {
        final SubwordsRelevanceCalculator sut = createSut("", "someMethod");
        assertTrue(sut.matchesRegex());
        assertEquals(PREFIX_BONUS, sut.getRelevance());
    }

    @Test
    public void testPrefixToken() {
        final SubwordsRelevanceCalculator sut = createSut("set", "setText");
        assertTrue(sut.matchesRegex());
        assertEquals(PREFIX_BONUS + 2, sut.getRelevance());
    }

    @Test
    public void testSubword() {
        final SubwordsRelevanceCalculator sut = createSut("text", "setText");
        assertTrue(sut.matchesRegex());
        assertEquals(3, sut.getRelevance());
    }

    @Test
    public void testIrrelevantCompletion() {
        final SubwordsRelevanceCalculator sut = createSut("get", "setText");
        assertFalse(sut.matchesRegex());
    }
}
