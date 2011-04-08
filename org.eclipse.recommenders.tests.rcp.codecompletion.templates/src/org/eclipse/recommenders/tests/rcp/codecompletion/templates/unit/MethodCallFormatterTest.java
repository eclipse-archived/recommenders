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
package org.eclipse.recommenders.tests.rcp.codecompletion.templates.unit;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.recommenders.internal.rcp.codecompletion.templates.code.MethodCallFormatter;
import org.eclipse.recommenders.internal.rcp.codecompletion.templates.types.MethodCall;
import org.junit.Assert;
import org.junit.Test;

public final class MethodCallFormatterTest {

    private static MethodCallFormatter methodCallFormatterMock = new MethodCallFormatter(
            MethodFormatterTest.getMethodFormatterMock());

    @Test
    public void testMethodCallFormatter() throws JavaModelException {
        // ...
        check(UnitTestSuite.getDefaultMethodCall(),
                "button123.setText(${intTest:link(0)}, ${arg0:link(false, true)}, ${arg1}, ${arg2:var(org/eclipse/swt/widgets/Button)});");

        // ...
        check(UnitTestSuite.getDefaultConstructorCall(),
                "${constructedType:newType(org.eclipse.swt.widgets.Button)} ${unconstructed:newName(org.eclipse.swt.widgets.Button)} = new ${constructedType}(${intTest:link(0)}, ${arg3:link(false, true)}, ${arg4}, ${arg5:var(org/eclipse/swt/widgets/Button)});");

        // Invoke getText() on an "unconstructed" variable.
        check(new MethodCall("", UnitTestSuite.getDefaultReturningMethodCall().getInvokedMethod()),
                "${returnedType:newType(String)} text = ${unconstructed}.getText(${intTest:link(0)}, ${arg6:link(false, true)}, ${arg7}, ${arg8:var(org/eclipse/swt/widgets/Button)});");
    }

    private void check(final MethodCall methodCall, final String expected) {
        Assert.assertEquals(expected, methodCallFormatterMock.format(methodCall));
    }

    public static MethodCallFormatter getMethodCallFormatterMock() {
        return methodCallFormatterMock;
    }
}
