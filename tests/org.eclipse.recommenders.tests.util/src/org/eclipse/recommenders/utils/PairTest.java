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
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class PairTest {

    private Object o1;

    private Object o2;

    private Pair<?, ?> sut;

    @Before
    public void setup() {
        o1 = new Object();
        o2 = new Object();
        sut = Pair.newPair(o1, o2);
    }

    @Test
    public void testEquals_Same() {
        assertTrue(sut.equals(sut));
    }

    @Test
    public void testEquals_Different() {
        Pair<?, ?> other = Pair.newPair(new Object(), new Object());
        assertFalse(sut.equals(other));
    }

    @Test
    public void testEquals_NullElements() {
        Pair<?, ?> nullnull = create(null, null);
        Pair<?, ?> nullobj = create(null, new Object());
        Pair<?, ?> objnull = create(null, new Object());
        Pair<?, ?> objobj = create(new Object(), new Object());
        assertEquals(nullnull, nullnull);
        assertEquals(nullobj, nullobj);
        assertEquals(objnull, objnull);
        assertEquals(objobj, objobj);
    }

    private Pair<?, ?> create(Object arg0, Object arg1) {
        return Pair.newPair(arg0, arg1);
    }

    @Test
    public void testEquals_Null() {
        assertFalse(sut.equals(null));
    }

    @Test
    public void testGet() {
        assertEquals(o1, sut.get(0));
        assertEquals(o2, sut.get(1));
    }

    @Test
    public void testGetFirst() {
        assertEquals(o1, sut.getFirst());
    }

    @Test
    public void testGetSecond() {
        assertEquals(o2, sut.getSecond());
    }

    @Test
    public void testHashCode() {
        // execute
        int actual = sut.hashCode();
        // verify
        assertEquals(o1.hashCode() + o2.hashCode(), actual);
    }
}
