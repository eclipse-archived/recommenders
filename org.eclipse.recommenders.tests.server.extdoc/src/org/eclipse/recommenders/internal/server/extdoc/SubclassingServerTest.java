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
import org.eclipse.recommenders.server.extdoc.types.ClassSelfcallDirectives;
import org.eclipse.recommenders.server.extdoc.types.MethodSelfcallDirectives;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public final class SubclassingServerTest {

    private final SubclassingServer server = new SubclassingServer();

    @Test
    @Ignore
    public void testGetClassOverrideDirective() {
        final ClassOverrideDirectives directive = server.getClassOverrideDirective(null);
        Assert.assertNotNull(directive);
    }

    @Test
    @Ignore
    public void testGetClassSelfcallDirective() {
        final ClassSelfcallDirectives directive = server.getClassSelfcallDirective(null);
        Assert.assertNotNull(directive);
    }

    @Test
    @Ignore
    public void testGetMethodSelfcallDirective() {
        final MethodSelfcallDirectives directive = server.getMethodSelfcallDirective(null);
        Assert.assertNotNull(directive);
    }

}
