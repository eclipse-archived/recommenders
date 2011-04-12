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

import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Region;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.commons.utils.names.VmTypeName;
import org.eclipse.recommenders.internal.rcp.codecompletion.templates.CompletionTargetVariableBuilder;
import org.eclipse.recommenders.internal.rcp.codecompletion.templates.types.CompletionTargetVariable;
import org.eclipse.recommenders.rcp.codecompletion.IIntelligentCompletionContext;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Unit tests for covering the {@link CompletionTargetVariableBuilder} class.
 */
@SuppressWarnings("restriction")
public final class CompletionTargetVariableBuilderTest {

    private static final ITypeName ENCLOSINGTYPE = UnitTestSuite.getDefaultMethodCall().getInvokedMethod()
            .getDeclaringType();

    @Test
    public void testExistentVariable() {
        testCompletionTargetVariableBuilder("Button butto = new Button();\nbutto.", "butto", "Button",
                new Region(29, 6), false);
        testCompletionTargetVariableBuilder("", null, null, new Region(0, 0), false);
    }

    @Test
    public void testConstructor() {
        testCompletionTargetVariableBuilder("Button bu", "bu", "Button", new Region(0, 9), true);
        testCompletionTargetVariableBuilder("Button", null, "Button", new Region(0, 6), true);
        // testCompletionTargetVariableBuilder("Button b", "b", null, new
        // Region(0, 8), true);
    }

    private void testCompletionTargetVariableBuilder(final String code, final String variableName,
            final String typeName, final Region region, final boolean needsConstructor) {
        final IIntelligentCompletionContext context = needsConstructor ? CompletionTargetVariableBuilderTest
                .getConstructorContextMock(code, variableName, typeName) : getMockedContext(code, variableName,
                typeName);
        final CompletionTargetVariable completionTargetVariable = CompletionTargetVariableBuilder
                .createInvokedVariable(context);

        Assert.assertEquals(variableName == null ? (typeName == null ? "this" : "") : variableName,
                completionTargetVariable.getName());
        Assert.assertEquals(typeName == null ? ENCLOSINGTYPE : VmTypeName.get(typeName),
                completionTargetVariable.getType());
        Assert.assertEquals(region, completionTargetVariable.getDocumentRegion());
        Assert.assertEquals(needsConstructor, completionTargetVariable.isNeedsConstructor());
    }

    protected static IIntelligentCompletionContext getMockedContext(final String code, final String variableName,
            final String typeName) {
        final IIntelligentCompletionContext context = Mockito.mock(IIntelligentCompletionContext.class);

        Mockito.when(context.getReceiverName()).thenReturn(variableName);
        if (typeName != null) {
            Mockito.when(context.getReceiverType()).thenReturn(VmTypeName.get(typeName));
        }
        Mockito.when(context.getPrefixToken()).thenReturn("");
        Mockito.when(Integer.valueOf(context.getInvocationOffset())).thenReturn(Integer.valueOf(code.length()));
        Mockito.when(context.getReplacementRegion()).thenReturn(new Region(code.length(), 0));
        Mockito.when(context.getEnclosingMethod()).thenReturn(UnitTestSuite.getDefaultMethodCall().getInvokedMethod());
        Mockito.when(context.getEnclosingType()).thenReturn(ENCLOSINGTYPE);

        final JavaContentAssistInvocationContext originalContext = Mockito
                .mock(JavaContentAssistInvocationContext.class);
        final IDocument doc = Mockito.mock(IDocument.class);
        try {
            Mockito.when(doc.get(Mockito.anyInt(), Mockito.anyInt())).thenReturn("");
        } catch (final BadLocationException e) {
            throw new IllegalStateException(e);
        }
        Mockito.when(originalContext.getDocument()).thenReturn(doc);
        Mockito.when(context.getOriginalContext()).thenReturn(originalContext);

        return context;
    }

    public static IIntelligentCompletionContext getConstructorContextMock(final String code, final String variableName,
            final String typeName) {
        final IIntelligentCompletionContext context = CompletionTargetVariableBuilderTest.getMockedContext(code,
                variableName, typeName);
        if (typeName != null) {
            Mockito.when(context.getExpectedType()).thenReturn(VmTypeName.get(typeName));
        }
        final Statement node = Mockito.mock(Statement.class);
        Mockito.when(node.toString()).thenReturn(
                "<test:" + (typeName == null ? code : VmTypeName.get(typeName).getClassName())
                        + (variableName != null ? " " + variableName : "") + ">");
        Mockito.when(context.getCompletionNode()).thenReturn(node);
        return context;
    }
}
