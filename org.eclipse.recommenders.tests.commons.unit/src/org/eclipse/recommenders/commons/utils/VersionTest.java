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
package org.eclipse.recommenders.commons.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.recommenders.commons.utils.Version;
import org.junit.Test;

public class VersionTest {

    @Test
    public void testValueOf_valid() {
        String version = "1.2.3.4";
        Version sut = Version.valueOf(version);
        assertEquals(1, sut.major);
        assertEquals(2, sut.minor);
        assertEquals(3, sut.micro);
        assertEquals(String.valueOf(4), sut.qualifier);
        assertEquals(version, sut.toString());
    }

    @Test
    public void testToString_withQualifier() {
        String version = "1.2.3.4";
        Version sut = Version.valueOf(version);
        assertEquals(version, sut.toString());
    }

    @Test
    public void testToString_withoutQualifier() {
        String version = "1.2.3";
        Version sut = Version.valueOf(version);
        assertEquals(version, sut.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValueOf_InvalidToManyDots() {
        Version.valueOf("1.2.3.4.5");
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
        Version v1 = Version.create(1, 2, 3, "4");
        Version v2 = Version.create(1, 2, 3, "5");
        assertTrue(v1.compareTo(v2) < 0);
        assertTrue(v2.compareTo(v1) > 0);
        assertTrue(v1.compareTo(v1) == 0);
    }

    @Test
    public void testComapreTo_DifferentMajor() {
        Version v1 = Version.create(1, 2);
        Version v2 = Version.create(2, 2);
        assertTrue(v1.compareTo(v2) < 0);
        assertTrue(v2.compareTo(v1) > 0);
    }

    @Test
    public void testComapreTo_DifferentMinor() {
        Version v1 = Version.create(1, 1, 0);
        Version v2 = Version.create(1, 2, 0);
        assertTrue(v1.compareTo(v2) < 0);
        assertTrue(v2.compareTo(v1) > 0);
    }

    @Test
    public void testComapreTo_DifferentMicro() {
        Version v1 = Version.create(1, 1, 1);
        Version v2 = Version.create(1, 1, 2);
        assertTrue(v1.compareTo(v2) < 0);
        assertTrue(v2.compareTo(v1) > 0);
    }
}
