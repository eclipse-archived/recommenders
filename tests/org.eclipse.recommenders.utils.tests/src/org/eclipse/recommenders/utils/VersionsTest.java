/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Sewe - initial API and implementation.
 */
package org.eclipse.recommenders.utils;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import org.junit.Test;

public class VersionsTest {

    private static final Version V_1_0_0 = new Version(1, 0, 0);
    private static final Version V_1_9_0 = new Version(1, 9, 0);

    private static final Version V_2_0_0 = new Version(2, 0, 0);
    private static final Version V_2_1_0 = new Version(2, 1, 0);
    private static final Version V_2_2_0 = new Version(2, 2, 0);
    private static final Version V_2_2_1 = new Version(2, 2, 1);
    private static final Version V_2_2_2 = new Version(2, 2, 2);
    private static final Version V_2_2_3 = new Version(2, 2, 3);
    private static final Version V_2_3_0 = new Version(2, 3, 0);
    private static final Version V_2_9_0 = new Version(2, 9, 0);

    private static final Version V_3_0_0 = new Version(3, 0, 0);

    private static final Version V_4_0_0 = new Version(4, 0, 0);

    @Test
    public void testExactMatchIsClosest() {
        Version closest = Versions.findClosest(V_2_0_0, Arrays.asList(V_1_0_0, V_2_0_0, V_3_0_0));

        assertThat(closest, is(equalTo(V_2_0_0)));
    }

    @Test
    public void testSameMajorVersionIsCloserAbove() {
        Version closest = Versions.findClosest(V_2_0_0, Arrays.asList(V_1_9_0, V_2_9_0));

        assertThat(closest, is(equalTo(V_2_9_0)));
    }

    @Test
    public void testSameMajorVersionIsCloserBelow() {
        Version closest = Versions.findClosest(V_2_9_0, Arrays.asList(V_2_0_0, V_3_0_0));
        assertThat(closest, is(equalTo(V_2_0_0)));
    }

    @Test
    public void testPreferLowerMajorVersionOverHigher() {
        Version major = Versions.findClosest(V_2_0_0, Arrays.asList(V_1_0_0, V_3_0_0));
        assertThat(major, is(equalTo(V_1_0_0)));
    }

    @Test
    public void testPreferLowerMinorVersionOverHigher() {
        Version closest = Versions.findClosest(V_2_2_0, Arrays.asList(V_2_1_0, V_2_3_0));

        assertThat(closest, is(equalTo(V_2_1_0)));
    }

    @Test
    public void testPreferLowerPatchVersionOverHigher() {
        Version closest = Versions.findClosest(V_2_2_2, Arrays.asList(V_2_2_1, V_2_2_3));

        assertThat(closest, is(equalTo(V_2_2_1)));
    }

    @Test
    public void testPreferLowerMajorVersionOverHigher2() {
        Version closest = Versions.findClosest(V_2_1_0, Arrays.asList(V_1_0_0, V_3_0_0));

        assertThat(closest, is(equalTo(V_1_0_0)));
    }

    @Test
    public void testPreferNearestMajorVersionAbove() {
        Version closest = Versions.findClosest(V_3_0_0, Arrays.asList(V_1_0_0, V_4_0_0));

        assertThat(closest, is(equalTo(V_4_0_0)));
    }

    @Test
    public void testPreferNearestMajorVersionBelow() {
        Version closest = Versions.findClosest(V_2_0_0, Arrays.asList(V_1_0_0, V_4_0_0));

        assertThat(closest, is(equalTo(V_1_0_0)));
    }

    @Test
    public void testPreferHighestMinorVersionVersionForLowerMajorVersion() {
        Version closest = Versions.findClosest(V_3_0_0, Arrays.asList(V_2_0_0, V_2_1_0));

        assertThat(closest, is(equalTo(V_2_1_0)));
    }

    @Test
    public void testPreferLowestMinorVersionVersionForHigherMajorVersion() {
        Version closest = Versions.findClosest(V_1_0_0, Arrays.asList(V_2_0_0, V_2_1_0));

        assertThat(closest, is(equalTo(V_2_0_0)));
    }
}
