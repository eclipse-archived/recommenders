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
package org.eclipse.recommenders.internal.rcp.codecompletion.templates.unit;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.internal.rcp.codecompletion.templates.code.CodeBuilder;
import org.junit.Test;

import junit.framework.Assert;

public final class CodeBuilderTest {

    @Test
    public void testBuildCode() {
        final CodeBuilder codeBuilder = new CodeBuilder(MethodCallFormatterTest.getMethodCallFormatterMock());

        // No methods
        Assert.assertEquals("${cursor}", codeBuilder.buildCode(new ArrayList<IMethodName>(), ""));

        final List<IMethodName> methods = new LinkedList<IMethodName>();
        methods.add(UnitTestSuite.getDefaultConstructorCall().getInvokedMethod());
        methods.add(UnitTestSuite.getDefaultMethodCall().getInvokedMethod());
        methods.add(UnitTestSuite.getDefaultReturningMethodCall().getInvokedMethod());
        methods.add(UnitTestSuite
                .createMethod("Lorg/eclipse/swt/widgets/Text.someMethod()Lorg/eclipse/swt/widgets/SomeWidget;"));

        final String expected = "${constructedType:newType(org.eclipse.swt.widgets.Button)} someWidget = new ${constructedType}(${intTest:link(0)}, ${arg0:link(false, true)}, ${arg1}, ${arg2:var(org/eclipse/swt/widgets/Button)}); someWidget.setText(${intTest:link(0)}, ${arg3:link(false, true)}, ${arg4}, ${arg5:var(org/eclipse/swt/widgets/Button)}); ${returnedType:newType(String)} text = someWidget.getText(${intTest:link(0)}, ${arg6:link(false, true)}, ${arg7}, ${arg8:var(org/eclipse/swt/widgets/Button)}); ${returnedType:newType(SomeWidget)} ${returned:newName(org.eclipse.swt.widgets.SomeWidget)} = someWidget.someMethod(${intTest:link(0)}, ${arg9:link(false, true)}, ${arg10}, ${arg11:var(org/eclipse/swt/widgets/Button)}); ${cursor}";
        Assert.assertEquals(expected, codeBuilder.buildCode(methods, "someWidget").replaceAll("[\\s]+", " "));
    }
}
