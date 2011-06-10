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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.recommenders.commons.utils.VersionRange.VersionRangeBuilder;
import org.junit.Test;

public class VersionRangeTest {

    Version v36 = Version.create(3, 6);
    Version v37 = Version.create(3, 7);
    Version v38 = Version.create(3, 8);

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
}
