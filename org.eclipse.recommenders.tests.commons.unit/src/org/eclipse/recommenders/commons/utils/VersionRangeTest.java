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
package org.eclipse.recommenders.commons.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.recommenders.commons.utils.VersionRange.VersionRangeBuilder;
import org.junit.Test;

public class VersionRangeTest {

    Version v36 = Version.create(3, 6);
    Version v37 = Version.create(3, 7);
    Version v38 = Version.create(3, 8);
    Version v39 = Version.create(3, 9);

    @Test
    public void testInRange() {

        final VersionRange sut = new VersionRangeBuilder().minExclusive(v36).maxExclusive(v38).build();
        assertTrue(sut.isIncluded(v37));
    }

    @Test
    public void testMaxInRange01() {
        final VersionRange r1 = new VersionRangeBuilder().maxExclusive(v38).build();
        final VersionRange r2 = new VersionRangeBuilder().maxExclusive(v38).build();
        assertTrue(r1.hasGreaterEqualUpperBoundThan(r2));
        assertTrue(r2.hasGreaterEqualUpperBoundThan(r1));
    }

    @Test
    public void testMaxInRange03() {
        final VersionRange r1 = new VersionRangeBuilder().maxInclusive(v38).build();
        final VersionRange r2 = new VersionRangeBuilder().maxExclusive(v38).build();
        assertTrue(r1.hasGreaterEqualUpperBoundThan(r2));
        assertFalse(r2.hasGreaterEqualUpperBoundThan(r1));
    }

    @Test
    public void testMaxInRange02() {
        final VersionRange r1 = new VersionRangeBuilder().maxExclusive(v38).build();
        final VersionRange r2 = new VersionRangeBuilder().maxInclusive(v37).build();
        assertTrue(r1.hasGreaterEqualUpperBoundThan(r2));
        assertFalse(r2.hasGreaterEqualUpperBoundThan(r1));
    }

    @Test
    public void testInRangeMinIncluded() {
        final VersionRange sut = new VersionRangeBuilder().minInclusive(v36).maxExclusive(v38).build();
        assertTrue(sut.isIncluded(v36));
    }

    @Test
    public void testInRangeMaxIncluded() {
        final VersionRange sut = new VersionRangeBuilder().minInclusive(v36).maxInclusive(v38).build();
        assertTrue(sut.isIncluded(v38));
    }

    @Test
    public void testUnknownInAll() {
        final VersionRange sut = VersionRange.ALL;
        assertTrue(sut.isIncluded(Version.UNKNOWN));
    }

    @Test
    public void testNotInRange() {
        final VersionRange sut = new VersionRangeBuilder().minInclusive(v37).maxInclusive(v38).build();
        assertFalse(sut.isIncluded(v36));
    }

    @Test
    public void testNotInRangeMinExcluded() {
        final VersionRange sut = new VersionRangeBuilder().minExclusive(v36).maxInclusive(v38).build();
        assertFalse(sut.isIncluded(v36));
    }

    @Test
    public void testNotInRangeMaxExcluded() {
        final VersionRange sut = new VersionRangeBuilder().minExclusive(v36).maxExclusive(v38).build();
        assertFalse(sut.isIncluded(v38));
    }

    @Test
    public void testEmptyRange() {
        final VersionRange sut = new VersionRangeBuilder().minExclusive(v36).maxExclusive(v36).build();
        assertFalse(sut.isIncluded(v36));
        assertFalse(sut.isIncluded(v37));
        assertFalse(sut.isIncluded(v38));
    }

    @Test
    public void testStringRepresentationInclusiveBounds() {
        final VersionRange sut = new VersionRangeBuilder().minInclusive(v36).maxInclusive(v38).build();
        assertEquals("[3.6.0,3.8.0]", sut.toString());
    }

    @Test
    public void testStringRepresentationExclusiveBounds() {
        final VersionRange sut = new VersionRangeBuilder().minExclusive(v36).maxExclusive(v38).build();
        assertEquals("(3.6.0,3.8.0)", sut.toString());
    }

    @Test
    public void testVersionBelowInclusive() {
        final VersionRange sut = new VersionRangeBuilder().minInclusive(v37).maxExclusive(v38).build();
        assertTrue(sut.isVersionBelow(v36));
        assertFalse(sut.isVersionBelow(v37));
        assertFalse(sut.isVersionBelow(v38));
    }

    @Test
    public void testVersionBelowExclusive() {
        final VersionRange sut = new VersionRangeBuilder().minExclusive(v37).maxExclusive(v38).build();
        assertTrue(sut.isVersionBelow(v36));
        assertTrue(sut.isVersionBelow(v37));
        assertFalse(sut.isVersionBelow(v38));
    }

    @Test
    public void testVersionAboveInclusive() {
        final VersionRange sut = new VersionRangeBuilder().minInclusive(v36).maxInclusive(v37).build();
        assertFalse(sut.isVersionAbove(v36));
        assertFalse(sut.isVersionAbove(v37));
        assertTrue(sut.isVersionAbove(v38));
    }

    @Test
    public void testVersionAboveExclusive() {
        final VersionRange sut = new VersionRangeBuilder().minExclusive(v36).maxExclusive(v37).build();
        assertFalse(sut.isVersionAbove(v36));
        assertTrue(sut.isVersionAbove(v37));
        assertTrue(sut.isVersionAbove(v38));
    }

    @Test
    public void testUpperBoundHigherThanHappyPath() {
        final VersionRange range1 = new VersionRangeBuilder().maxExclusive(v36).build();
        final VersionRange range2 = new VersionRangeBuilder().maxExclusive(v37).build();

        assertFalse(range1.isUpperBoundHigherThan(range2));
        assertTrue(range2.isUpperBoundHigherThan(range1));
    }

    @Test
    public void testUpperBoundHigherThanOnEqualVersion() {
        final VersionRange range1 = new VersionRangeBuilder().maxExclusive(v36).build();
        final VersionRange range2 = new VersionRangeBuilder().maxInclusive(v36).build();

        assertFalse(range1.isUpperBoundHigherThan(range2));
        assertTrue(range2.isUpperBoundHigherThan(range1));
        assertFalse(range1.isUpperBoundHigherThan(range1));
        assertFalse(range2.isUpperBoundHigherThan(range2));
    }

    @Test
    public void testLowerBoundLowerThanHappyPath() {
        final VersionRange range1 = new VersionRangeBuilder().minExclusive(v37).build();
        final VersionRange range2 = new VersionRangeBuilder().minExclusive(v36).build();

        assertFalse(range1.isLowerBoundLowerThan(range2));
        assertTrue(range2.isLowerBoundLowerThan(range1));
    }

    @Test
    public void testLowerBoundLowerThanOnEqualVersion() {
        final VersionRange range1 = new VersionRangeBuilder().minExclusive(v36).build();
        final VersionRange range2 = new VersionRangeBuilder().minInclusive(v36).build();

        assertFalse(range1.isLowerBoundLowerThan(range2));
        assertTrue(range2.isLowerBoundLowerThan(range1));
        assertFalse(range1.isLowerBoundLowerThan(range1));
        assertFalse(range2.isLowerBoundLowerThan(range2));
    }

    @Test
    public void testIsUpperBoundEquals() {
        final VersionRange range1 = new VersionRangeBuilder().maxExclusive(v36).build();
        final VersionRange range2 = new VersionRangeBuilder().maxInclusive(v36).build();

        assertTrue(range1.isUpperBoundEquals(range1));
        assertFalse(range1.isUpperBoundEquals(range2));
    }

    @Test
    public void testIsLowerBoundEquals() {
        final VersionRange range1 = new VersionRangeBuilder().minExclusive(v36).build();
        final VersionRange range2 = new VersionRangeBuilder().minInclusive(v36).build();

        assertTrue(range1.isLowerBoundEquals(range1));
        assertFalse(range1.isLowerBoundEquals(range2));
    }

    @Test
    public void testResiduesInnerRange() {
        final VersionRange outerRange = new VersionRangeBuilder().minInclusive(v36).maxExclusive(v39).build();
        final VersionRange innerRange = new VersionRangeBuilder().minInclusive(v37).maxExclusive(v38).build();

        final List<VersionRange> residues = outerRange.getResidues(innerRange);

        assertEquals(2, residues.size());
        final VersionRange lowerResidue = residues.get(0);
        assertEquals(v36, lowerResidue.getMinVersion());
        assertTrue(lowerResidue.isMinVersionInclusive());
        assertEquals(v37, lowerResidue.getMaxVersion());
        assertFalse(lowerResidue.isMaxVersionInclusive());

        final VersionRange upperResidue = residues.get(1);
        assertEquals(v38, upperResidue.getMinVersion());
        assertTrue(upperResidue.isMinVersionInclusive());
        assertEquals(v39, upperResidue.getMaxVersion());
        assertFalse(upperResidue.isMaxVersionInclusive());
    }

    @Test
    public void testResiduesNoIntersection() {
        final VersionRange range1 = new VersionRangeBuilder().minInclusive(v36).maxExclusive(v38).build();
        final VersionRange range2 = new VersionRangeBuilder().minInclusive(v38).maxExclusive(v39).build();

        final List<VersionRange> residues = range1.getResidues(range2);

        assertEquals(1, residues.size());
        assertEquals(range1, residues.get(0));
    }

    @Test
    public void testEmpty() {
        final VersionRange range = new VersionRangeBuilder().minInclusive(v37).maxInclusive(v36).build();
        assertTrue(range.isEmpty());
    }

    @Test
    public void testNotEmpty() {
        final VersionRange range = new VersionRangeBuilder().minInclusive(v36).maxInclusive(v36).build();
        assertFalse(range.isEmpty());
    }
}
