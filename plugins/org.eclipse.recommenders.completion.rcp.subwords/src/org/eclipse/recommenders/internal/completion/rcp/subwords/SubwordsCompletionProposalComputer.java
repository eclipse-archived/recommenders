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
package org.eclipse.recommenders.internal.completion.rcp.subwords;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.recommenders.utils.rcp.internal.RecommendersUtilsPlugin;
import org.eclipse.swt.graphics.Point;

import com.google.common.annotations.VisibleForTesting;

public class SubwordsCompletionProposalComputer implements IJavaCompletionProposalComputer {

    private JavaContentAssistInvocationContext ctx;

    @Override
    public List computeCompletionProposals(final ContentAssistInvocationContext context, final IProgressMonitor monitor) {
        ctx = (JavaContentAssistInvocationContext) context;
        if (!shouldReturnResults())
            return Collections.emptyList();
        return findSubwordMatchingProposals(monitor);
    }

    @VisibleForTesting
    protected boolean shouldReturnResults() {
        if (JavaUiUtil.isDefaultAssistActive(JavaUiUtil.ASSIST_JDT_ALL)) {
            // do not return duplicates if the default JDT processor is already enabled on Eclipse 3.5
            return false;
        }
        Set<String> disabledIds = JavaUiUtil.getDisabledIds(JavaPlugin.getDefault().getPreferenceStore());
        if (!disabledIds.contains(JavaUiUtil.ASSIST_JDT_NOTYPE) && !disabledIds.contains(JavaUiUtil.ASSIST_JDT_TYPE)) {
            // do not return duplicates if the default JDT processors are already enabled on on Eclipse 3.3 and 3.4
            return false;
        }
        return true;
    }

    private String getToken() {
        final char[] token = ctx.getCoreContext().getToken();
        if (token == null) {
            return "";
        }
        return String.valueOf(token);
    }

    private List<IJavaCompletionProposal> findSubwordMatchingProposals(IProgressMonitor monitor) {

        final String token = getToken();
        final SubwordsCompletionRequestor requestor = new SubwordsCompletionRequestor(token, ctx);

        final ITextViewer viewer = ctx.getViewer();
        final Point selection = viewer.getSelectedRange();
        if (selection.y > 0) {
            requestor.setReplacementLength(selection.y);
        }

        final ICompilationUnit cu = ctx.getCompilationUnit();
        final int offsetBeforeTokenBegin = ctx.getInvocationOffset() - token.length();
        try {
            // first on the original position
            cu.codeComplete(ctx.getInvocationOffset(), requestor, monitor);
            if (token.length() > 0) {
                // then on the 'virtual' position
                cu.codeComplete(offsetBeforeTokenBegin, requestor, monitor);
            }
        } catch (final JavaModelException e) {
            RecommendersUtilsPlugin.log(e);
        }
        return requestor.getProposals();
    }

    @Override
    public void sessionStarted() {
    }

    @Override
    public List computeContextInformation(final ContentAssistInvocationContext context, final IProgressMonitor monitor) {
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
