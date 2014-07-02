/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.testing.jdt;

import static org.eclipse.recommenders.testing.jdt.AstUtils.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.util.Set;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.recommenders.utils.Pair;
import org.junit.Test;

public class AstUtilsTest {

    @Test
    public void test001() {
        String code = "$public class X extends Y {}";

        Pair<CompilationUnit, Set<Integer>> markers = createAstWithMarkers(code.toString());

        assertThat(markers.getSecond(), hasItem(0));
    }

    @Test
    public void test002() {
        String code = "class $X$ {}";

        Pair<CompilationUnit, Set<Integer>> markers = createAstWithMarkers(code.toString());

        assertThat(markers.getFirst().toString(), not(hasToString(containsString(MARKER))));

        assertThat(markers.getSecond(), hasItem(6));
        assertThat(markers.getSecond(), hasItem(7));
    }
}
