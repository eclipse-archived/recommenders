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
        LinkedList<Object[]> scenarios = new LinkedList<>();
        scenarios.add(new Object[] { "2.3.0", "2.3.0" });
        scenarios.add(new Object[] { "2.3.0", "2.3" });
        scenarios.add(new Object[] { "2.0.0", "2" });
        scenarios.add(new Object[] { "2.3.0", "2.3-SNAPSHOT" });
        scenarios.add(new Object[] { "2.3.0", "2.3.Beta" });
        scenarios.add(new Object[] { "Beta", "Beta" });
        scenarios.add(new Object[] { "2.3.0", "2.3.0" });
        scenarios.add(new Object[] { "1.2.3", "1.2.3.4" });
        scenarios.add(new Object[] { "15.0.0", "15.0" });
        scenarios.add(new Object[] { "1.1.3333", "1.1.3333" });
        scenarios.add(new Object[] { "1.2222.0", "1.2222" });
        scenarios.add(new Object[] { "1111.2222.0", "1111.2222" });
        scenarios.add(new Object[] { "1111.2222.3333", "1111.2222.3333" });
        scenarios.add(new Object[] { "1234567.1234567.0", "1234567.1234567" });
        scenarios.add(new Object[] { "1234567.898765.432101", "1234567.898765.432101" });
        return scenarios;
    }

    @Test
    public void testCanonicalVersionWorksCorrect() {
        assertEquals(expected, Versions.canonicalizeVersion(input));
    }
}
