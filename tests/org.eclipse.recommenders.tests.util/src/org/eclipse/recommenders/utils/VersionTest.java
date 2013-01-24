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
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class VersionTest {

    @Test
    public void testValueOf_valid() {
        final String version = "1.2.3.4";
        final Version sut = Version.valueOf(version);
        assertEquals(1, sut.major);
        assertEquals(2, sut.minor);
        assertEquals(3, sut.micro);
        assertEquals(String.valueOf(4), sut.qualifier);
        assertEquals(version, sut.toString());
    }

    @Test
    public void testToString_withQualifier() {
        final String version = "1.2.3.4";
        final Version sut = Version.valueOf(version);
        assertEquals(version, sut.toString());
    }

    @Test
    public void testToString_withoutQualifier() {
        final String version = "1.2.3";
        final Version sut = Version.valueOf(version);
        assertEquals(version, sut.toString());
    }

    @Test
    // (expected = IllegalArgumentException.class)
    public void testValueOf_InvalidToManyDots() {
        // we accept small version mistakes
        Version version = Version.valueOf("1.2.3.4.5");
        assertEquals("4.5", version.qualifier);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValueOf_InvalidNoDotsAtAll() {
        Version.valueOf("1_2_3_4_5");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValueOf_InvalidMavenStyle() {
        Version.valueOf("1.2.3-5");
    }

    @Test
    public void testComapreTo_differentQualifier() {
        final Version v1 = Version.create(1, 2, 3, "4");
        final Version v2 = Version.create(1, 2, 3, "5");
        assertTrue(v1.compareTo(v2) < 0);
        assertTrue(v2.compareTo(v1) > 0);
        assertTrue(v1.compareTo(v1) == 0);
    }

    @Test
    public void testComapreTo_DifferentMajor() {
        final Version v1 = Version.create(1, 2);
        final Version v2 = Version.create(2, 2);
        assertTrue(v1.compareTo(v2) < 0);
        assertTrue(v2.compareTo(v1) > 0);
    }

    @Test
    public void testComapreTo_DifferentMinor() {
        final Version v1 = Version.create(1, 1, 0);
        final Version v2 = Version.create(1, 2, 0);
        assertTrue(v1.compareTo(v2) < 0);
        assertTrue(v2.compareTo(v1) > 0);
    }

    @Test
    public void testComapreTo_DifferentMicro() {
        final Version v1 = Version.create(1, 1, 1);
        final Version v2 = Version.create(1, 1, 2);
        assertTrue(v1.compareTo(v2) < 0);
        assertTrue(v2.compareTo(v1) > 0);
    }

    @Test
    public void testCompareUnknownLatest() {
        assertTrue(Version.UNKNOWN.compareTo(Version.LATEST) < 0);
        assertTrue(Version.LATEST.compareTo(Version.UNKNOWN) > 0);
    }
}
