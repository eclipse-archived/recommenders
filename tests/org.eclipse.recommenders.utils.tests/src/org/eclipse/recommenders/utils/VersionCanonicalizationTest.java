/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Olav Lenz - Initial implementation.
 */
package org.eclipse.recommenders.utils;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.LinkedList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.Lists;

@RunWith(Parameterized.class)
public class VersionCanonicalizationTest {

    private String expected;
    private String input;

    public VersionCanonicalizationTest(String expected, String input) {
        this.expected = expected;
        this.input = input;
    }

    @Parameters
    public static Collection<Object[]> scenarios() {
        LinkedList<Object[]> scenarios = Lists.newLinkedList();
        scenarios.add(new Object[] { "2.3.0", "2.3.0" });
        scenarios.add(new Object[] { "2.3.0", "2.3" });
        scenarios.add(new Object[] { "2.0.0", "2" });
        scenarios.add(new Object[] { "2.3.0", "2.3-SNAPSHOT" });
        scenarios.add(new Object[] { "2.3.0", "2.3.Beta" });
        scenarios.add(new Object[] { "Beta", "Beta" });
        scenarios.add(new Object[] { "2.3.0", "2.3.0" });
        scenarios.add(new Object[] { "1.2.3", "1.2.3.4" });
        return scenarios;
    }

    @Test
    public void testCanonicalVersionWorksCorrect() {
        assertEquals(expected, Versions.canonicalizeVersion(input));
    }

}
