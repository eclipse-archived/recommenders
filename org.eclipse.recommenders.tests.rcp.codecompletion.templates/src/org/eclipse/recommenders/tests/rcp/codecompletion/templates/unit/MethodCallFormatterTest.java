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

/**
 * Unit tests for covering the {@link MethodCallFormatter} class.
 */
public final class MethodCallFormatterTest {

    private static MethodCallFormatter methodCallFormatterMock = new MethodCallFormatter(
            MethodFormatterTest.getMethodFormatterMock());

    @Test
    public void testMethodCallFormatter() throws JavaModelException {
        check(UnitTestSuite.getDefaultMethodCall(),
                "button123.setText(${intTest:link(0)}, ${arg:link(false, true)}, ${arg2}, ${arg3:var(org.eclipse.swt.widgets.Button)});");

        check(UnitTestSuite.getDefaultConstructorCall(),
                "${constructedType:newType(org.eclipse.swt.widgets.Button)} ${unconstructed:newName(org.eclipse.swt.widgets.Button)} = new ${constructedType}(${intTest2:link(0)}, ${arg4:link(false, true)}, ${arg5}, ${arg6:var(org.eclipse.swt.widgets.Button)});");

        check(UnitTestSuite.getDefaultReturningMethodCall(),
                "${java_lang_String:newType(java.lang.String)} text = button456.getText(${intTest3:link(0)}, ${arg7:link(false, true)}, ${arg8}, ${arg9:var(org.eclipse.swt.widgets.Button)});");
    }

    /**
     * @param methodCall
     *            The method call which ought to be formatted by the
     *            {@link MethodCallFormatter}.
     * @param expected
     *            The string which is expected to be given by the formatter.
     */
    private static void check(final MethodCall methodCall, final String expected) throws JavaModelException {
        Assert.assertEquals(expected, methodCallFormatterMock.format(methodCall));
    }

    /**
     * @return A {@link MethodCallFormatter} using a mocked
     *         <code>MethodFormatter</code> to simulate Eclipse's behavior in
     *         resolving a method's parameters, which is not available during
     *         tests.
     */
    public static MethodCallFormatter getMethodCallFormatterMock() {
        return methodCallFormatterMock;
    }
}
