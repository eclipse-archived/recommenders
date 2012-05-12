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
package org.eclipse.recommenders.tests.completion.rcp.subwords;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.jdt.core.CompletionContext;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.recommenders.internal.completion.rcp.subwords.SubwordsCompletionProposalComputer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IEditorPart;
import org.mockito.Mockito;

public class SubwordsMockUtils {

    public static SubwordsCompletionProposalComputer createEngine() {
        return new SubwordsCompletionProposalComputer();
    }

    public static CompletionProposal mockCompletionProposal() {
        return mock(CompletionProposal.class);
    }

    public static CompletionProposal mockCompletionProposal(final int proposalKind, final String completion) {
        final CompletionProposal mock = mockCompletionProposal();
        when(mock.getKind()).thenReturn(proposalKind);
        when(mock.getCompletion()).thenReturn(completion.toCharArray());
        when(mock.getName()).thenReturn(completion.toCharArray());
        when(mock.getSignature()).thenReturn("()V".toCharArray());
        when(mock.getDeclarationKey()).thenReturn(new char[0]);
        when(mock.getDeclarationSignature()).thenReturn("()V".toCharArray());

        return mock;
    }

    public static JavaContentAssistInvocationContext mockInvocationContext() throws JavaModelException {
        final IJavaProject javaProject = mockJavaProject();
        final ICompilationUnit cu = mockICompilationUnit();
        final ITextViewer viewer = mock(ITextViewer.class);
        when(viewer.getSelectedRange()).thenReturn(new Point(-1, -1));

        final CompletionContext completionContext = mock(CompletionContext.class);
        final JavaContentAssistInvocationContext javaContext = new JavaContentAssistInvocationContext(viewer, 1,
                mock(IEditorPart.class)) {

            @Override
            public ICompilationUnit getCompilationUnit() {
                try {
                    final IType type = mockType();
                    when(cu.getElementAt(Mockito.anyInt())).thenReturn(type);
                    when(cu.getJavaProject()).thenReturn(javaProject);
                    return cu;
                } catch (final JavaModelException e) {
                    return null;
                }
            }

            @Override
            public CompletionContext getCoreContext() {
                return completionContext;
            }
        };

        when(javaContext.getProject()).thenReturn(javaProject);

        return javaContext;
    }

    private static IType mockType() throws JavaModelException {
        final IType type = mock(IType.class);
        when(type.getElementName()).thenReturn("DummyName");
        when(type.getFields()).thenReturn(new IField[0]);
        when(type.getMethods()).thenReturn(new IMethod[0]);
        when(type.getAncestor(Mockito.anyInt())).thenReturn(type);
        return type;
    }

    public static JavaContentAssistInvocationContext mockInvocationContext(final String token)
            throws JavaModelException {
        final JavaContentAssistInvocationContext javaContext = mockInvocationContext();
        final CompletionContext coreContext = javaContext.getCoreContext();
        when(coreContext.getToken()).thenReturn(token.toCharArray());

        return javaContext;
    }

    public static ICompilationUnit mockICompilationUnit() throws JavaModelException {
        final ICompilationUnit cu = mock(ICompilationUnit.class);
        final IJavaProject javaProject = mockJavaProject();
        when(cu.getJavaProject()).thenReturn(javaProject);
        return cu;
    }

    private static IJavaProject mockJavaProject() throws JavaModelException {
        final IJavaProject javaProject = mock(IJavaProject.class);
        final IType type = mockType();
        when(javaProject.findElement(Mockito.anyString(), Mockito.any(WorkingCopyOwner.class))).thenReturn(type);
        return javaProject;
    }

    @SuppressWarnings("restriction")
    public static JavaCompletionProposal mockJdtCompletion(final String proposalDisplayString) {
        final JavaCompletionProposal mock = mock(JavaCompletionProposal.class);
        when(mock.getDisplayString()).thenReturn(proposalDisplayString);
        return mock;
    }

}
