/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.rcp.codecompletion.subwords;

import static org.eclipse.recommenders.rcp.codecompletion.subwords.SubwordsMockUtils.mockInvocationContext;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.eclipse.jdt.core.CompletionContext;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.recommenders.rcp.codecompletion.subwords.SubwordsCompletionProposalComputer;
import org.junit.Test;

public class SubwordsCompletionProposalComputerTest {

    @Test
    public void testSmokeComputeProposalsWithSomeToken() throws JavaModelException {
        // setup:
        final SubwordsCompletionProposalComputer sut = new SubwordsCompletionProposalComputer();
        // exercise:
        final JavaContentAssistInvocationContext ctx = mockInvocationContext("aToken");
        sut.computeCompletionProposals(ctx, null);
        // verify:
        final CompletionContext coreCtx = ctx.getCoreContext();
        verify(coreCtx, atLeastOnce()).getToken();
        verifyCodeCompleteIsCalled(ctx);
    }

    private void verifyCodeCompleteIsCalled(final JavaContentAssistInvocationContext ctx) throws JavaModelException {
        final ICompilationUnit cu = ctx.getCompilationUnit();
        verify(cu, times(1)).codeComplete(anyInt(), (CompletionRequestor) any());
    }

    @Test
    public void testComputeProposalsWithEmptyToken() {
        // setup:
        final SubwordsCompletionProposalComputer sut = new SubwordsCompletionProposalComputer();
        final JavaContentAssistInvocationContext ctx = mockInvocationContext("");
        // exercise:
        sut.computeCompletionProposals(ctx, null);
        // verify:
        // ensure that no further computation is performed if token is empty!
        verify(ctx, never()).getCompilationUnit();
    }

    @Test
    public void testComputeProposalsWithNullToken() {
        // setup:
        final SubwordsCompletionProposalComputer sut = new SubwordsCompletionProposalComputer();
        final JavaContentAssistInvocationContext ctx = mockInvocationContext();
        // exercise:
        sut.computeCompletionProposals(ctx, null);
        // verify:
        // ensure that no further computation is performed if token is null!
        verify(ctx, never()).getCompilationUnit();
    }

    @Test
    public void testComputeProposalsWithJavaModelException() throws JavaModelException {
        // setup:
        final SubwordsCompletionProposalComputer sut = new SubwordsCompletionProposalComputer();
        final JavaContentAssistInvocationContext ctx = mockInvocationContext("aToken");
        // configure that 'complete' throws an exception:
        final ICompilationUnit cu = ctx.getCompilationUnit();
        final JavaModelException exception = mock(JavaModelException.class);
        doThrow(exception).when(cu).codeComplete(anyInt(), (CompletionRequestor) anyObject());
        // exercise:
        sut.computeCompletionProposals(ctx, null);
        // verify:
        // exception should be handled but not propagated.
    }
}
