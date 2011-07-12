/**
 * Copyright (c) 2011 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.internal.server.extdoc;

import org.eclipse.recommenders.server.extdoc.SubclassingServer;
import org.eclipse.recommenders.server.extdoc.types.ClassOverrideDirectives;
import org.eclipse.recommenders.server.extdoc.types.ClassOverridePatterns;
import org.eclipse.recommenders.server.extdoc.types.ClassSelfcallDirectives;
import org.eclipse.recommenders.server.extdoc.types.MethodSelfcallDirectives;
import org.eclipse.recommenders.tests.commons.extdoc.ServerUtils;
import org.eclipse.recommenders.tests.commons.extdoc.TestUtils;
import org.junit.Test;

public final class SubclassingServerTest {

    private final SubclassingServer server = new SubclassingServer(ServerUtils.getServer(),
            ServerUtils.getUsernameListener());

    @Test
    public void testGetClassOverrideDirectives() {
        final ClassOverrideDirectives directive = server.getClassOverrideDirectives(TestUtils.getDefaultType());
        // Assert.assertTrue(directive.getNumberOfSubclasses() > 0);
        // Assert.assertFalse(directive.getOverrides().isEmpty());
        // directive.validate();
    }

    @Test
    public void testGetClassSelfcallDirectives() {
        final ClassSelfcallDirectives directive = server.getClassSelfcallDirectives(TestUtils.getDefaultType());
        // Assert.assertTrue(directive.getNumberOfSubclasses() > 0);
        // Assert.assertFalse(directive.getCalls().isEmpty());
        // directive.validate();
    }

    @Test
    public void testGetMethodSelfcallDirectives() {
        final MethodSelfcallDirectives directive = server.getMethodSelfcallDirectives(TestUtils.getDefaultMethod());
        // Assert.assertTrue(directive.getNumberOfDefinitions() > 0);
        // Assert.assertFalse(directive.getCalls().isEmpty());
        // directive.validate();
    }

    @Test
    public void testGetClassOverridePatterns() {
        final ClassOverridePatterns patterns = server.getClassOverridePatterns(TestUtils.getDefaultType());
        // Assert.assertTrue(patterns.getPatterns().length > 0);
    }

}
