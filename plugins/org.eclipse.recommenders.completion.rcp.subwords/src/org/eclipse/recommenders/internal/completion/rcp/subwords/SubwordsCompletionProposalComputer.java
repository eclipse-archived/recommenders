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

import static org.eclipse.recommenders.internal.completion.rcp.subwords.SubwordsUtils.JDT_ALL_CATEGORY;
import static org.eclipse.recommenders.internal.completion.rcp.subwords.SubwordsUtils.MYLYN_ALL_CATEGORY;
import static org.eclipse.recommenders.internal.completion.rcp.subwords.SubwordsUtils.isMylynInstalled;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.CompletionContext;
import org.eclipse.jdt.core.ICompilationUnit;
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

    private static final int TIMEOUT = 8000;
    public static String CATEGORY_ID = "org.eclipse.recommenders.subwords.rcp.category";
    private JavaContentAssistInvocationContext ctx;

    @Override
    public List computeCompletionProposals(final ContentAssistInvocationContext context, final IProgressMonitor monitor) {
        ctx = (JavaContentAssistInvocationContext) context;
        if (!shouldReturnResults()) return Collections.emptyList();
        return findSubwordMatchingProposals(new TimeDelimitedProgressMonitor(monitor, TIMEOUT));
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

        if (isJdtAllEnabled(cats) || isMylynInstalledAndEnabled(cats)) {
            // do not compute any recommendations and deactivate yourself in background
            new DisableContentAssistCategoryJob(CATEGORY_ID).schedule(300);
            return false;
        }
        return true;
    }

    private boolean isMylynInstalledAndEnabled(Set<String> cats) {
        return isMylynInstalled() && !cats.contains(MYLYN_ALL_CATEGORY);
    }

    private boolean isJdtAllEnabled(Set<String> cats) {
        return !cats.contains(JDT_ALL_CATEGORY);
    }

    private String getToken() {
        CompletionContext coreCtx = ctx.getCoreContext();
        if (coreCtx == null) return "";
        final char[] token = coreCtx.getToken();
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
        } catch (final Exception e) {
            RecommendersUtilsPlugin.logWarning(e, "Code completion failed: %s", e.getMessage());
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
