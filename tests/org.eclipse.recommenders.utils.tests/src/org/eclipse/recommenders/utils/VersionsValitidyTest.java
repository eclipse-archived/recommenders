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
public class VersionsValitidyTest {

    private Boolean expected;
    private String input;

    public VersionsValitidyTest(Boolean expected, String input) {
        this.expected = expected;
        this.input = input;
    }

    @Parameters
    public static Collection<Object[]> scenarios() {
        LinkedList<Object[]> scenarios = Lists.newLinkedList();
        scenarios.add(new Object[] { true, "0.100.1" });
        scenarios.add(new Object[] { false, "2.3" });
        scenarios.add(new Object[] { false, "2" });
        scenarios.add(new Object[] { false, "2.3-SNAPSHOT" });
        scenarios.add(new Object[] { false, "2.3.Beta" });
        scenarios.add(new Object[] { false, "Beta" });
        scenarios.add(new Object[] { true, "2.3.0" });
        scenarios.add(new Object[] { false, "1.2.3.4" });
        scenarios.add(new Object[] { true, "2013.9.4" });
        scenarios.add(new Object[] { false, "2013.9" });
        return scenarios;
    }

    @Test
    public void testCanonicalVersionWorksCorrect() {
        assertEquals(expected, Versions.isValidVersion(input));
    }

}
