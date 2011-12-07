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
package org.eclipse.recommenders.internal.completion.rcp;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.recommenders.completion.rcp.IntelligentCompletionContextResolver;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

@SuppressWarnings({ "rawtypes" })
public class IntelligentCompletionProposalComputer implements IJavaCompletionProposalComputer {

    private final IntelligentCompletionContextResolver contextResolver;

    @Inject
    public IntelligentCompletionProposalComputer(final IntelligentCompletionContextResolver contextResolver) {
        this.contextResolver = contextResolver;
    }

    @Override
    public List computeCompletionProposals(final ContentAssistInvocationContext context,
            final IProgressMonitor /* actually a NullProgressMonitor in e3.6 --> */monitor) {
        final JavaContentAssistInvocationContext jContext = (JavaContentAssistInvocationContext) context;
        // if (contextResolver.hasProjectRecommendersNature(jContext)) {
        // return Collections.emptyList();
        // } else {
        final List<IJavaCompletionProposal> proposals = Lists.newLinkedList();
        // proposals.add(createEnableRecommendersProposal(jContext));
        return proposals;
        // }
    }

    // private CompletionProposalEnableRecommenders createEnableRecommendersProposal(
    // final JavaContentAssistInvocationContext jCtx) {
    // return new CompletionProposalEnableRecommenders(contextResolver.getProjectFromContext(jCtx),
    // jCtx.getInvocationOffset());
    // }

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

    @Override
    public void sessionStarted() {
    }
}
