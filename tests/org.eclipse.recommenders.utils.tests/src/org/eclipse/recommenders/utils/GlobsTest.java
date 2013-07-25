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
package org.eclipse.recommenders.utils;

import static org.eclipse.recommenders.utils.Globs.matches;
import static org.junit.Assert.*;

import org.junit.Test;

public class GlobsTest {

    private static final String SOME = "some";

    @Test
    public void testSame() {
        assertTrue(matches("", ""));
        assertTrue(matches("s", "s"));
        assertTrue(matches(SOME, SOME));
        assertTrue(matches("s\\s", "s\\s"));
    }

    @Test
    public void testStar() {
        assertTrue(matches("s", "*"));
        assertTrue(matches("s", "s*"));
        assertTrue(matches("s.t.u", "s*"));
        assertTrue(matches("s.t.u", "s.t*"));
        assertTrue(matches(SOME, "s*"));
        assertTrue(matches(SOME, "s****"));
        assertTrue(matches(SOME, "so*"));
    }

    @Test
    public void testNoMatches() {
        assertFalse(matches(SOME, "sap*"));
        assertFalse(matches(SOME, "s?"));
        assertFalse(matches(SOME, "?some"));
        assertFalse(matches(SOME, "som??"));
    }

    @Test
    public void testQuestion() {
        assertTrue(matches("s", "?"));
        assertTrue(matches(SOME, "s???"));
        assertTrue(matches(SOME, "?ome"));
    }
}
