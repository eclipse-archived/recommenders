/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.tests.completion.rcp;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.jdt.core.CompletionContext;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.recommenders.tests.jdt.SimpleCompletionRequestor;
import org.eclipse.ui.IEditorPart;

public class JavaContentAssistContextMock extends JavaContentAssistInvocationContext {

    private final ICompilationUnit cu;
    private SimpleCompletionRequestor requestor;
    private final int completionOffset;

    public JavaContentAssistContextMock(final ICompilationUnit cu, final int completionOffset)
            throws JavaModelException {
        super(createTextViewer(cu), completionOffset, mock(IEditorPart.class));
        this.cu = cu;
        this.completionOffset = completionOffset;
        initializeCoreContext();
    }

    private static ITextViewer createTextViewer(final ICompilationUnit cu) throws JavaModelException {
        final ITextViewer mock = mock(ITextViewer.class);
        final Document document = new Document(cu.getSource());
        when(mock.getDocument()).thenReturn(document);
        return mock;
    }

    @Override
    public CharSequence computeIdentifierPrefix() throws BadLocationException {
        final char[] prefix = getCoreContext().getToken();
        if (prefix == null) {
            return "";
        }
        return String.valueOf(prefix);
    }

    private void initializeCoreContext() throws JavaModelException {
        requestor = new SimpleCompletionRequestor();
        cu.codeComplete(completionOffset, requestor);
    }

    @Override
    public ICompilationUnit getCompilationUnit() {
        return cu;
    }

    @Override
    public CompletionContext getCoreContext() {
        return requestor.context;
    }

}
