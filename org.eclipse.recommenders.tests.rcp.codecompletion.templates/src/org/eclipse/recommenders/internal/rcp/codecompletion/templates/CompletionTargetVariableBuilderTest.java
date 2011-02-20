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

import com.google.common.collect.Sets;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.Region;
import org.eclipse.recommenders.commons.utils.names.VmTypeName;
import org.eclipse.recommenders.internal.rcp.codecompletion.templates.types.CompletionTargetVariable;
import org.eclipse.recommenders.rcp.codecompletion.IIntelligentCompletionContext;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public final class CompletionTargetVariableBuilderTest {

    @Test
    public void testExistentVariable() {
        final IIntelligentCompletionContext context = CompletionTargetVariableBuilderTest.getMockedContext(
                "Button butto = new Button();\nbutto.", "butto", "Button");
        final CompletionTargetVariable completionTargetVariable = CompletionTargetVariableBuilder
                .createInvokedVariable(context);

        Assert.assertFalse(completionTargetVariable.isNeedsConstructor());
        Assert.assertEquals("butto", completionTargetVariable.getName());
        Assert.assertEquals(VmTypeName.get("Button"), completionTargetVariable.getType());
        Assert.assertEquals(new Region(29, 6), completionTargetVariable.getDocumentRegion());
    }

    @Test
    public void testConstructor() {
        final IIntelligentCompletionContext context = CompletionTargetVariableBuilderTest.getConstructorContextMock(
                "Button bu", "bu ", "Button");
        final CompletionTargetVariable completionTargetVariable = CompletionTargetVariableBuilder
                .createInvokedVariable(context);

        Assert.assertTrue(completionTargetVariable.isNeedsConstructor());
        Assert.assertEquals("bu", completionTargetVariable.getName());
        Assert.assertEquals(VmTypeName.get("Button"), completionTargetVariable.getType());
        Assert.assertEquals(new Region(9, 0), completionTargetVariable.getDocumentRegion());
    }

    protected static IIntelligentCompletionContext getMockedContext(final String code, final String variableName,
            final String typeSimpleName) {
        final IIntelligentCompletionContext context = Mockito.mock(IIntelligentCompletionContext.class);
        final ICompilationUnit compUnit = Mockito.mock(ICompilationUnit.class);

        Mockito.when(context.getReceiverName()).thenReturn(variableName);
        Mockito.when(context.getReceiverType()).thenReturn(VmTypeName.get(typeSimpleName));
        Mockito.when(context.getCompilationUnit()).thenReturn(compUnit);
        Mockito.when(Integer.valueOf(context.getInvocationOffset())).thenReturn(Integer.valueOf(code.length()));
        Mockito.when(context.getReplacementRegion()).thenReturn(new Region(code.length(), 0));
        Mockito.when(context.getEnclosingMethod()).thenReturn(AllTests.getDefaultMethodCall().getInvokedMethod());
        try {
            Mockito.when(compUnit.getSource()).thenReturn(code);
        } catch (final JavaModelException e) {
            throw new IllegalArgumentException(e);
        }

        return context;
    }

    public static IIntelligentCompletionContext getConstructorContextMock(final String code, final String variableName,
            final String typeSimpleName) {
        final IIntelligentCompletionContext context = CompletionTargetVariableBuilderTest.getMockedContext(code,
                variableName, typeSimpleName);
        final CompletionProposal prop = Mockito.mock(CompletionProposal.class);
        Mockito.when(prop.getSignature()).thenReturn("Lorg.eclipse.swt.widgets.Button;".toCharArray());
        Mockito.when(context.getJdtProposals()).thenReturn(Sets.newHashSet(prop));
        return context;
    }
}
