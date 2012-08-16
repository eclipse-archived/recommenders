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
package org.eclipse.recommenders.internal.completion.rcp.overrides;

import static java.lang.Math.rint;

import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.AbstractJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContextFactory;
import org.eclipse.recommenders.rcp.RecommendersPlugin;
import org.eclipse.recommenders.utils.rcp.CompletionProposalDecorator;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class OverridesCompletionProposalComputer implements IJavaCompletionProposalComputer {

    private IRecommendersCompletionContext ctx;
    private List<OverridesRecommendation> recommendations=Collections.emptyList();
    private List<IJavaCompletionProposal> proposals;

    private OverridesRecommender recommender;
    private IRecommendersCompletionContextFactory contextFactory;

    @Inject
    public OverridesCompletionProposalComputer(OverridesRecommender recommender,
            IRecommendersCompletionContextFactory contextFactory) {
        this.recommender = recommender;
        this.contextFactory = contextFactory;
    };

    @Override
    public List computeCompletionProposals(final ContentAssistInvocationContext context, final IProgressMonitor monitor) {
        proposals = Lists.newLinkedList();
        if (context instanceof JavaContentAssistInvocationContext) {
            try {
                JavaContentAssistInvocationContext jdtContext = (JavaContentAssistInvocationContext) context;
                ctx = contextFactory.create(jdtContext);
                recommender.startSession(ctx);
                recommendations = recommender.getRecommendations();
                computeProposals();
            } catch (CoreException e) {
                RecommendersPlugin.log(e);
            }
        }
        return proposals;
    }

    private void computeProposals() {
        final String prefix = ctx.getPrefix();
        for (Entry<IJavaCompletionProposal, CompletionProposal> pair : ctx.getProposals().entrySet()) {
            IJavaCompletionProposal uiProposal = pair.getKey();
            CompletionProposal cProposal = pair.getValue();
            switch (cProposal.getKind()) {
            case CompletionProposal.METHOD_DECLARATION:
                final String signature = String.valueOf(cProposal.getSignature()).replace('.', '/');
                final String name = String.valueOf(cProposal.getName());
                final String propSignature = (name + signature).replaceAll("<\\.>", "");
                for (final OverridesRecommendation r : recommendations) {
                    if (!r.method.getName().startsWith(prefix)) {
                        continue;
                    }

                    final String recSignature = r.method.getSignature();
                    if (recSignature.equals(propSignature)) {
                        if (uiProposal instanceof AbstractJavaCompletionProposal) {
                            int baseRelevance = uiProposal.getRelevance();
                            baseRelevance += (int) rint(r.probability * 100);
                            ((AbstractJavaCompletionProposal) uiProposal).setRelevance(baseRelevance);
                        }
                        final CompletionProposalDecorator decoratedProposal =
                                new CompletionProposalDecorator(uiProposal, r.probability);
                        proposals.add(decoratedProposal);
                    }
                }
            }
        }
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
