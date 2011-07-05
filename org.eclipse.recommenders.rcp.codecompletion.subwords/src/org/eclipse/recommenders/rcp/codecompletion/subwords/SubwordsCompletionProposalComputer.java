/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 *    Paul-Emmanuel Faidherbe - Completion generalization
 */
package org.eclipse.recommenders.rcp.codecompletion.subwords;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;

public class SubwordsCompletionProposalComputer implements IJavaCompletionProposalComputer {

    private JavaContentAssistInvocationContext ctx;

    @Override
    public List<?> computeCompletionProposals(final ContentAssistInvocationContext context,
            final IProgressMonitor monitor) {
        ctx = (JavaContentAssistInvocationContext) context;
        return findSubwordMatchingProposals();
    }

    private String getToken() {
        final char[] token = ctx.getCoreContext().getToken();
        if (token == null) {
            return "";
        }
        return String.valueOf(token);
    }

    private List<IJavaCompletionProposal> findSubwordMatchingProposals() {

        final String token = getToken();
        final SubwordsCompletionRequestor requestor = new SubwordsCompletionRequestor(token, ctx);
        final ICompilationUnit cu = ctx.getCompilationUnit();
        final int offsetBeforeTokenBegin = ctx.getInvocationOffset() - token.length();
        try {
            cu.codeComplete(offsetBeforeTokenBegin, requestor);
        } catch (final JavaModelException e) {
            // TODO: won't do that in a later version... but for this early
            // prototype...
            // REVIEW: could improve my code and log this message to the error
            // log?
            e.printStackTrace();
        }
        return requestor.getProposals();
    }

    @Override
    public void sessionStarted() {
    }

    @Override
    public List<?> computeContextInformation(final ContentAssistInvocationContext context,
            final IProgressMonitor monitor) {
        return Collections.emptyList();
    }

    @Override
    public String getErrorMessage() {
        return null;
    }

    @Override
    public void sessionEnded() {
    }
}
