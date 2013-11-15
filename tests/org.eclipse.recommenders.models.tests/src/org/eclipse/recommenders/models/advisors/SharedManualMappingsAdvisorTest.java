/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andreas Sewe - initial API and implementation
 */
package org.eclipse.recommenders.models.advisors;

import static org.eclipse.recommenders.models.advisors.SharedManualMappingsAdvisor.matchesSuffixPattern;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.LinkedList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.Lists;

@RunWith(Parameterized.class)
public class SharedManualMappingsAdvisorTest {

    private final String path;
    private final String suffixPattern;
    private final boolean expected;

    public SharedManualMappingsAdvisorTest(String path, String suffixPattern, boolean matches) {
        this.path = path;
        this.suffixPattern = suffixPattern;
        expected = matches;
    }

    @Parameters(name = "\"{1}\" matches \"{0}\"? {2}!")
    public static Collection<Object[]> scenarios() {
        LinkedList<Object[]> scenarios = Lists.newLinkedList();

        final String path = "/home/user/workspace/project/lib/example.jar";

        scenarios.add(match(path, "example.jar"));
        scenarios.add(match(path, "*.jar"));
        scenarios.add(match(path, "???????.jar"));
        scenarios.add(mismatch(path, "?.jar"));

        scenarios.add(match(path, "lib/example.jar"));
        scenarios.add(match(path, "*/*.jar"));
        scenarios.add(mismatch(path, "libs/*.jar"));

        scenarios.add(match(path, "project/lib/example.jar"));
        scenarios.add(mismatch(path, "workspace/lib/example.jar"));

        scenarios.add(match(path, "home/user/workspace/project/lib/example.jar"));
        scenarios.add(mismatch(path, "home/user/workspace/*/example.jar"));

        scenarios.add(match(path, "/home/user/workspace/project/lib/example.jar"));

        return scenarios;
    }

    private static Object[] match(String absolutePath, String suffixPattern) {
        return new Object[] { absolutePath, suffixPattern, true };
    }

    private static Object[] mismatch(String absolutePath, String suffixPattern) {
        return new Object[] { absolutePath, suffixPattern, false };
    }

    @Test
    public void testMatchesSuffixPattern() {
        assertThat(matchesSuffixPattern(path, suffixPattern), is(expected));
    }
}
