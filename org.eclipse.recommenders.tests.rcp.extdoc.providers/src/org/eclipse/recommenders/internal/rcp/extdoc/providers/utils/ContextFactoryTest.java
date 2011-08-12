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
package org.eclipse.recommenders.internal.rcp.extdoc.providers.utils;

import org.eclipse.recommenders.tests.commons.extdoc.ExtDocUtils;
import org.eclipse.recommenders.tests.commons.extdoc.TestJavaElementSelection;
import org.eclipse.recommenders.tests.commons.extdoc.TestTypeUtils;

import org.junit.Assert;
import org.junit.Test;

public final class ContextFactoryTest {

    private final TestJavaElementSelection selection = ExtDocUtils.getSelection();

    @Test
    public void testcreateNullVariableContext() {
        final MockedIntelligentCompletionContext context = ContextFactory.createNullVariableContext(selection);
        Assert.assertEquals(null, context.getVariable());
    }

    @Test
    public void testcreateThisVariableContext() {
        final MockedIntelligentCompletionContext context = ContextFactory.createThisVariableContext(selection,
                TestTypeUtils.getDefaultJavaMethod());
        Assert.assertNotNull(context.getEnclosingMethod());
        Assert.assertNotNull(context.getEnclosingMethodsFirstDeclaration());
        Assert.assertTrue(context.getVariable().isThis());
    }

    @Test
    public void testcreateFieldVariableContext() {

    }

    @Test
    public void testcreateLocalVariableContext() {

    }

}
