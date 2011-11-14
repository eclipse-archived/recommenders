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
package org.eclipse.recommenders.tests.completion.rcp.templates;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Region;
import org.eclipse.recommenders.completion.rcp.IIntelligentCompletionContext;
import org.eclipse.recommenders.internal.analysis.codeelements.Variable;
import org.eclipse.recommenders.internal.completion.rcp.templates.CompletionTargetVariableBuilder;
import org.eclipse.recommenders.internal.completion.rcp.templates.types.CompletionTargetVariable;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.names.VmTypeName;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

/**
 * Unit tests for covering the {@link CompletionTargetVariableBuilder} class.
 */
@SuppressWarnings("restriction")
public final class CompletionTargetVariableBuilderTest {

    @Test
    public void testExistentVariable() {
        testCompletionTargetVariableBuilder("Button butto = new Button();\nbutto.", "butto", "Button",
                new Region(29, 6), false);
        testCompletionTargetVariableBuilder("", null, "Button", new Region(0, 0), false);
        // testCompletionTargetVariableBuilder("b", "b", null, new Region(0, 1),
        // false);
    }

    @Test
    public void testConstructor() {
        testCompletionTargetVariableBuilder("Button bu", "bu", "Button", new Region(0, 9), true);
        // testCompletionTargetVariableBuilder("Button", null, "Button", new
        // Region(0, 6), true);
        // testCompletionTargetVariableBuilder("Text", "Text", null, new
        // Region(0, 4), true);
    }

    private static void testCompletionTargetVariableBuilder(final String code, final String variableName,
            final String typeName, final Region region, final boolean needsConstructor) {
        final IIntelligentCompletionContext context = needsConstructor ? getConstructorContextMock(code, variableName,
                typeName) : getMockedContext(code, variableName, typeName);
        final CompletionTargetVariable completionTargetVariable = CompletionTargetVariableBuilder
                .createInvokedVariable(context);

        final String expectedVariable = variableName == null ? typeName == null ? "this" : "" : typeName == null ? ""
                : variableName;
        final ITypeName expectedType = typeName == null ? "Text".equals(variableName) ? VmTypeName.get("L"
                + variableName) : TestUtils.getDefaultMethodCall().getInvokedMethod().getDeclaringType()
                : VmTypeName.get(typeName);
        Assert.assertEquals(expectedVariable, completionTargetVariable.getName());
        Assert.assertEquals(expectedType, completionTargetVariable.getType());
        Assert.assertEquals(region, completionTargetVariable.getDocumentRegion());
        Assert.assertEquals(needsConstructor, completionTargetVariable.needsConstructor());
    }

    static IIntelligentCompletionContext getMockedContext(final String code, final String variableName,
            final String typeName) {
        final IIntelligentCompletionContext context = Mockito.mock(IIntelligentCompletionContext.class);

        final Variable variable = Mockito.mock(Variable.class);
        Mockito.when(variable.isThis()).thenReturn(variableName == null || variableName.isEmpty());
        Mockito.when(variable.getType()).thenReturn(VmTypeName.get(typeName));
        Mockito.when(context.getReceiverType()).thenReturn(VmTypeName.get(typeName));
        Mockito.when(context.getVariable()).thenReturn(variable);

        Mockito.when(context.getReceiverName()).thenReturn(variableName);
        Mockito.when(context.getPrefixToken()).thenReturn("");
        Mockito.when(Integer.valueOf(context.getInvocationOffset())).thenReturn(Integer.valueOf(code.length()));
        Mockito.when(context.getReplacementRegion()).thenReturn(new Region(code.length(), 0));
        final IMethodName invokedMethod = TestUtils.getDefaultMethodCall().getInvokedMethod();
        Mockito.when(context.getEnclosingMethod()).thenReturn(invokedMethod);
        Mockito.when(context.getEnclosingType()).thenReturn(invokedMethod.getDeclaringType());

        final JavaContentAssistInvocationContext originalContext = Mockito
                .mock(JavaContentAssistInvocationContext.class);
        final IDocument doc = Mockito.mock(IDocument.class);
        try {
            Mockito.when(doc.get(Matchers.anyInt(), Matchers.anyInt())).thenReturn("");
        } catch (final BadLocationException e) {
            throw new IllegalStateException(e);
        }
        Mockito.when(originalContext.getDocument()).thenReturn(doc);
        Mockito.when(context.getOriginalContext()).thenReturn(originalContext);
        Mockito.when(context.findMatchingVariable(variableName)).thenReturn(Variable.create(variableName, null, null));

        return context;
    }

    public static IIntelligentCompletionContext getConstructorContextMock(final String code, final String variableName,
            final String typeName) {
        final IIntelligentCompletionContext context = getMockedContext(code, variableName, typeName);
        if (typeName != null) {
            Mockito.when(context.getExpectedType()).thenReturn(VmTypeName.get(typeName));
        }
        Mockito.when(context.findMatchingVariable(variableName)).thenReturn(null);
        final Statement node = Mockito.mock(Statement.class);
        final String bla = "Text".equals(variableName) ? variableName : (typeName == null ? code : VmTypeName.get(
                typeName).getClassName())
                + (variableName != null ? " " + variableName : "");
        Mockito.when(node.toString()).thenReturn("<test:" + bla + ">");
        Mockito.when(context.getCompletionNode()).thenReturn(node);

        final ICompilationUnit compilationUnit = Mockito.mock(ICompilationUnit.class);
        Mockito.when(context.getCompilationUnit()).thenReturn(compilationUnit);
        return context;
    }
}
