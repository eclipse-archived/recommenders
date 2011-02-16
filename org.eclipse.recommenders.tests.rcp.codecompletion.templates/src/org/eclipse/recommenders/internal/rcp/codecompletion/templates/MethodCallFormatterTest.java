/**
 * Copyright (c) 2010 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.codecompletion.templates;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.recommenders.internal.rcp.codecompletion.templates.types.MethodCall;
import org.junit.Assert;
import org.junit.Test;

public final class MethodCallFormatterTest {

    @Test
    public void testPatternNamer() throws JavaModelException {
        final MethodCallFormatter formatter = getMethodCallFormatterMock();
        final MethodCall methodCall = AllTests.getDefaultMethodCall();
        Assert.assertEquals("someVariable = new Button(${intTest:link(0)});", formatter.format(methodCall));
    }

    public static MethodCallFormatter getMethodCallFormatterMock() throws JavaModelException {
        return new MethodCallFormatter(MethodFormatterTest.getMethodFormatterMock());
    }
}
