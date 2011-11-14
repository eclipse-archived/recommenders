/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;

import java.util.List;
import java.util.Set;

import org.eclipse.recommenders.utils.Bag;
import org.eclipse.recommenders.utils.HashBag;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class HashBagTest {

    HashBag<String> sut = HashBag.newHashBag();

    @Test
    public void testAdd() {
        sut.add("a", 3);
        sut.add("a");
        assertEquals(4, sut.count("a"));
    }

    @Test
    public void testAddAll_Array() {
        final String[] expected = new String[] { "a", "b", "c" };
        sut.addAll(expected);
        for (final String e : expected) {
            assertEquals(1, sut.count(e));
        }
    }

    @Test
    public void testAddAll_Collection() {
        final Set<String> expected = Sets.newHashSet("a", "b", "c");
        sut.addAll(expected);
        final Set<String> actual = sut.elements();
        assertEquals(expected, actual);
    }

    @Test
    public void testCount_NotExistantKey() {
        assertEquals(0, sut.count(null));
    }

    @Test
    public void testCreate_Collection() {
        sut.addAll("a", "b");
        final Bag<String> copy = HashBag.newHashBag(sut.elements());
        assertNotSame(sut, copy);
        assertEquals(sut, copy);
    }

    @Test
    public void testCreate_Copy() {
        sut.addAll("a", "b");
        final Bag<String> copy = HashBag.create(sut);
        assertNotSame(sut, copy);
        assertEquals(sut, copy);
    }

    @Test
    public void testEquals_New() {
        sut.add("a");
        assertFalse(sut.equals(HashBag.newHashBag()));
    }

    @Test
    public void testEquals_Null() {
        assertFalse(sut.equals(null));
    }

    @Test
    public void testEquals_Same() {
        sut.addAll("a", "b");
        assertEquals(sut, sut);
    }

    @Test
    public void testKeySetSize() {
        sut.addAll("a", "b", "c", "a");
        assertEquals(3, sut.elementsCount());
    }

    @Test
    public void testRemove() {
        sut.addAll("a", "a");
        assertEquals(2, sut.totalElementsCount());
        sut.remove("a", 1);
        assertEquals(1, sut.totalElementsCount());
    }

    @Test
    public void testRemoveAll() {
        sut.addAll("a", "a");
        assertEquals(2, sut.totalElementsCount());
        sut.removeAll("a");
        assertEquals(0, sut.totalElementsCount());
    }

    @Test
    public void testTopElements() {
        final String[] someElements = new String[] { "a", "b", "c", "c", "a", "a" };
        final List<String> expected = Lists.newArrayList("a", "c");
        sut.addAll(someElements);
        final List<String> actual = sut.topElements(2);
        assertEquals(expected, actual);
    }

    @Test
    public void testTotalSize_Collection() {
        sut.addAll("a", "b", "c", "a");
        assertEquals(4, sut.totalElementsCount());
    }
}
