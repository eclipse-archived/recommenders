package org.eclipse.recommenders.internal.rcp.codecompletion.templates.unit;

import java.util.ArrayList;

import com.google.common.collect.Lists;

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

        Assert.assertEquals(
                "Button bu = new Button(${intTest:link(0)}); bu.setText(${intTest:link(0)}); ${cursor}",
                codeBuilder.buildCode(
                        Lists.newArrayList(UnitTestSuite.getDefaultConstructorCall().getInvokedMethod(), UnitTestSuite
                                .getDefaultMethodCall().getInvokedMethod()), "bu").replaceAll("[\\s]+", " "));
    }

}
