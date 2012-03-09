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
package org.eclipse.recommenders.utils.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.recommenders.utils.Version;
import org.junit.Test;

public class MavenVersionParserTest {

    @Test
    public void testWithQualifier() {
        final String version = "1.2.3-4";
        final MavenVersionParser parser = new MavenVersionParser();
        assertTrue(parser.canParse(version));
        final Version sut = parser.parse(version);
        assertEquals("1.2.3.4", sut.toString());
    }

    @Test
    public void testWithoutQualifier() {
        final String version = "1.2.3";
        final MavenVersionParser parser = new MavenVersionParser();
        assertTrue(parser.canParse(version));
        final Version sut = parser.parse(version);
        assertEquals(version, sut.toString());
    }

    @Test
    public void testWithoutMicro() {
        final String version = "1.2";
        final MavenVersionParser parser = new MavenVersionParser();
        assertTrue(parser.canParse(version));
        final Version sut = parser.parse(version);
        assertEquals("1.2.0", sut.toString());
    }

    @Test
    public void testWithoutMinor() {
        final String version = "1";
        final MavenVersionParser parser = new MavenVersionParser();
        assertTrue(parser.canParse(version));
        final Version sut = parser.parse(version);
        assertEquals("1.0.0", sut.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidNoDotsAtAll() {
        final String version = "1_2_3_4_5";
        final MavenVersionParser parser = new MavenVersionParser();
        assertFalse(parser.canParse(version));
        parser.parse(version);
    }

    @Test
    public void testStrangeQualifier() {
        final String version = "1.2.3.4-5";
        final MavenVersionParser parser = new MavenVersionParser();
        Version actual = parser.parse(version);
        assertEquals(Version.create(1, 2, 3, "4-5"), actual);
    }

    @Test
    public void testValueOf_InvalidToManyDots() {
        final String version = "1.2.3-4.5";
        final MavenVersionParser parser = new MavenVersionParser();
        Version actual = parser.parse(version);
        assertEquals(Version.create(1, 2, 3, "4.5"), actual);
    }

    @Test
    public void testAlternativeDelimiter() {
        final String version = "1.2.3.5";
        final MavenVersionParser parser = new MavenVersionParser();
        assertTrue(parser.canParse(version));
        final Version sut = parser.parse(version);
        assertEquals("1.2.3.5", sut.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidDelimiter() {
        final String version = "1-2-3-5";
        final MavenVersionParser parser = new MavenVersionParser();
        assertFalse(parser.canParse(version));
        parser.parse(version);
    }
}
