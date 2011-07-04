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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.jdt.core.CompletionContext;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;

public class SubwordsMockUtils {

    public static CompletionProposal mockCompletionProposal() {
        return mock(CompletionProposal.class);
    }

    public static CompletionProposal mockCompletionProposal(final int proposalKind, final String completion) {
        final CompletionProposal mock = mockCompletionProposal();
        when(mock.getKind()).thenReturn(proposalKind);
        when(mock.getCompletion()).thenReturn(completion.toCharArray());
        when(mock.getName()).thenReturn(completion.toCharArray());
        when(mock.getSignature()).thenReturn("()V".toCharArray());

        return mock;
    }

    public static JavaContentAssistInvocationContext mockInvocationContext() {
        final JavaContentAssistInvocationContext javaContext = mock(JavaContentAssistInvocationContext.class);

        final CompletionContext completionContext = mock(CompletionContext.class);
        when(javaContext.getCoreContext()).thenReturn(completionContext);

        final ICompilationUnit cu = mockICompilationUnit();
        when(javaContext.getCompilationUnit()).thenReturn(cu);

        return javaContext;
    }

    public static JavaContentAssistInvocationContext mockInvocationContext(final String token) {
        final JavaContentAssistInvocationContext javaContext = mockInvocationContext();
        final CompletionContext coreContext = javaContext.getCoreContext();
        when(coreContext.getToken()).thenReturn(token.toCharArray());

        return javaContext;
    }

    public static ICompilationUnit mockICompilationUnit() {
        return mock(ICompilationUnit.class);
    }

    public static JavaCompletionProposal mockJdtCompletion(final String proposalDisplayString) {
        final JavaCompletionProposal mock = mock(JavaCompletionProposal.class);
        when(mock.getDisplayString()).thenReturn(proposalDisplayString);
        return mock;
    }

}
