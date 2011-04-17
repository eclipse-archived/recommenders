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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.internal.rcp.codecompletion.templates.code.CodeBuilder;
import org.junit.Test;

import junit.framework.Assert;

/**
 * Unit tests for covering the {@link CodeBuilder} class.
 */
public final class CodeBuilderTest {

    private final CodeBuilder codeBuilder = new CodeBuilder(MethodCallFormatterTest.getMethodCallFormatterMock());

    /**
     * Ensures the correct behavior of many of the {@link CodeBuilder}'s
     * features by giving several different methods to the builder.
     */
    @Test
    public void testBuildCode() {
        final List<IMethodName> methods = new LinkedList<IMethodName>();
        methods.add(UnitTestSuite.getDefaultConstructorCall().getInvokedMethod());
        methods.add(UnitTestSuite.getDefaultMethodCall().getInvokedMethod());
        methods.add(UnitTestSuite.getDefaultReturningMethodCall().getInvokedMethod());
        methods.add(UnitTestSuite
                .createMethod("Lorg/eclipse/swt/widgets/Text.someMethod()Lorg/eclipse/swt/widgets/SomeWidget;"));

        final String expected = "${constructedType:newType(org.eclipse.swt.widgets.Button)} someWidget = new ${constructedType}(${intTest:link(0)}, ${arg:link(false, true)}, ${arg2}, ${arg3:var(org.eclipse.swt.widgets.Button)}); someWidget.setText(${intTest2:link(0)}, ${arg4:link(false, true)}, ${arg5}, ${arg6:var(org.eclipse.swt.widgets.Button)}); ${returnedType:newType(String)} text = someWidget.getText(${intTest3:link(0)}, ${arg7:link(false, true)}, ${arg8}, ${arg9:var(org.eclipse.swt.widgets.Button)}); ${returnedType:newType(SomeWidget)} ${returned:newName(org.eclipse.swt.widgets.SomeWidget)} = someWidget.someMethod(${intTest4:link(0)}, ${arg10:link(false, true)}, ${arg11}, ${arg12:var(org.eclipse.swt.widgets.Button)}); ${cursor}";
        Assert.assertEquals(expected, codeBuilder.buildCode(methods, "someWidget").replaceAll("[\\s]+", " "));
    }

    /**
     * When no methods are given the {@link CodeBuilder} should throw an
     * exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testBuildCodeNoMethods() {
        codeBuilder.buildCode(new ArrayList<IMethodName>(), "");
    }
}
