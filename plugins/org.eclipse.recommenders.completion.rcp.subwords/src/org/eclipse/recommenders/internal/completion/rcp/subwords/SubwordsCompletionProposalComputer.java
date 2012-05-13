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

import static org.eclipse.recommenders.internal.completion.rcp.subwords.PreferencePage.JDT_ALL_CATEGORY;
import static org.eclipse.recommenders.internal.completion.rcp.subwords.PreferencePage.MYLYN_ALL_CATEGORY;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.recommenders.utils.rcp.internal.RecommendersUtilsPlugin;
import org.eclipse.swt.graphics.Point;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class SubwordsCompletionProposalComputer implements IJavaCompletionProposalComputer {

    public static String CATEGORY_ID = "org.eclipse.recommenders.subwords.rcp.category";
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
        Set<String> cats = Sets.newHashSet(PreferenceConstants.getExcludedCompletionProposalCategories());
        if (cats.contains(CATEGORY_ID)) {
            // we are excluded on default tab?
            // then we are not on default tab NOW. We are on a subsequent tab.
            // then make completions:
            return true;
        }

        // is jdt all enabled?
        // is mylyn all enabled?
        if (!(cats.contains(JDT_ALL_CATEGORY) || cats.contains(MYLYN_ALL_CATEGORY))) {
            // do not compute any recommendations and deactivate yourself in background
            new DisableSubwordsJob().schedule(300);
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
