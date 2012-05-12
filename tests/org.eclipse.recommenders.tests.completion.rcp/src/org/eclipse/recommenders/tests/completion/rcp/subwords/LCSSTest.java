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
package org.eclipse.recommenders.tests.completion.rcp.subwords;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.eclipse.recommenders.internal.completion.rcp.subwords.LCSS;
import org.junit.Test;

public class LCSSTest {

    @Test
    public void testOneWord() {
        assertEquals(1, LCSS.findSequences("", "").size());
        assertEquals(0, LCSS.findSequences("", "a").size());
        assertEquals(1, LCSS.findSequences("a", "").size());
        assertEquals(1, LCSS.findSequences("a", "a").size());
        assertEquals(0, LCSS.findSequences("a", "b").size());

        assertEquals(2, LCSS.findSequences("aa", "a").size());
        assertEquals(0, LCSS.findSequences("aa", "b").size());

        assertEquals(1, LCSS.findSequences("aa", "aa").size());
        assertEquals(0, LCSS.findSequences("aa", "ba").size());

        assertEquals(1, LCSS.findSequences("aaa", "aaa").size());
        assertEquals(4, LCSS.findSequences("aaaa", "aaa").size());

        assertEquals(1, LCSS.findSequences("ab", "ab").size());
        assertEquals(0, LCSS.findSequences("ab", "de").size());

        assertEquals(1, LCSS.findSequences("abcd", "ab").size());
        assertEquals(1, LCSS.findSequences("abcd", "bc").size());
        assertEquals(1, LCSS.findSequences("abcd", "cd").size());

        assertEquals(0, LCSS.findSequences("ab", "abcd").size());
        assertEquals(0, LCSS.findSequences("bc", "abcd").size());
        assertEquals(0, LCSS.findSequences("cd", "abcd").size());

        assertEquals(1, LCSS.findSequences("abcd", "ab").size());
        assertEquals(1, LCSS.findSequences("abcd", "abc").size());
        assertEquals(1, LCSS.findSequences("abcd", "bcd").size());

        assertEquals(1, LCSS.findSequences("xyz", "xy").size());
        assertEquals(0, LCSS.findSequences("xy", "xyxy").size());
        assertEquals(3, LCSS.findSequences("xyxy", "xy").size());
        assertEquals(3, LCSS.findSequences("xyzabxy", "xy").size());
    }

    @Test
    public void testMultipleWords() {
        assertEquals(1, LCSS.findSequences("aaBaaCaaDaa", "caa").size());
        assertEquals(1, LCSS.findSequences("aaBaaCaaDaa", "cdaa").size());
        assertEquals(2, LCSS.findSequences("aaBaaCaaDaa", "badaa").size()); // ba_* & b_a*
        assertEquals(1, LCSS.findSequences("initializeDialogUnits", "dial").size());
        assertEquals(2, LCSS.findSequences("setDateData", "dat").size());
    }

    @Test
    public void testBug001() {
        List<int[]> s = LCSS.findSequences("newLabeledStatement", "le");
        assertEquals(2, s.size());
        assertTrue(Arrays.equals(s.get(0), new int[] { 3, 6 }));
        assertTrue(Arrays.equals(s.get(1), new int[] { 3, 8 }));

    }

    @Test
    public void testPackages() {
        List<int[]> s = LCSS.findSequences("com.apple.coxcurrent", "comcon");
        assertEquals(1, s.size());
    }

    @Test
    public void testConstants() {
        List<int[]> s = LCSS.findSequences("DLM_IMG_HELP", "el");
        assertEquals(0, s.size());
    }

    @Test
    public void testTypeNames() {
        assertEquals(1, LCSS.findSequences("StringBuilder", "sb").size());
        assertEquals(1, LCSS.findSequences("StringBuilder", "sbu").size());
        assertEquals(0, LCSS.findSequences("String", "tri").size());
        assertEquals(0, LCSS.findSequences("LinkedList", "inkedList").size());
        assertEquals(1, LCSS.findSequences("ArrayList", "list").size());
    }

    @Test
    public void testSubsequences() {
        LCSS.findSequences("setdatedata", "dat");
    }
}
