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

import static com.google.common.base.Preconditions.checkNotNull;
import static org.eclipse.recommenders.rcp.codecompletion.subwords.SubwordsUtils.getTokensBetweenLastWhitespaceAndFirstOpeningBracket;

import java.util.List;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.internal.ui.text.java.AbstractJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.CompletionProposalCollector;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;

import com.google.common.collect.Lists;

@SuppressWarnings("restriction")
public class SubwordsCompletionRequestor extends CompletionRequestor {

    private final List<IJavaCompletionProposal> proposals = Lists.newLinkedList();

    private final JavaContentAssistInvocationContext ctx;

    private final CompletionProposalCollector collector;

    private final SubwordsRelevanceCalculator relevanceCalculator;

    private final String token;

    public SubwordsCompletionRequestor(final String token, final JavaContentAssistInvocationContext ctx) {
        checkNotNull(token);
        checkNotNull(ctx);
        this.token = token;
        this.ctx = ctx;
        this.collector = new CompletionProposalCollector(ctx.getCompilationUnit());
        this.collector.acceptContext(ctx.getCoreContext());
        relevanceCalculator = new SubwordsRelevanceCalculator(token);
    }

    @Override
    public void accept(final CompletionProposal proposal) {
        // REVIEW: Name mismatch: it's not just a score calculator. It also
        // computes regex match etc. Pls rename.

        // bug: proposal.getCompletion for method overrides does not play well
        // with getTokensUntilFirstOpeningBracket!
        final String completion = getTokensBetweenLastWhitespaceAndFirstOpeningBracket(proposal.getCompletion());
        relevanceCalculator.setCompletion(completion);
        if (!relevanceCalculator.matchesRegex()) {
            return;
        }

        final IJavaCompletionProposal jdtProposal = tryCreateJdtProposal(proposal);
        if (jdtProposal == null) {
            return;
        }
        relevanceCalculator.setJdtRelevance(jdtProposal.getRelevance());
        createSubwordsProposal(proposal, jdtProposal);

    }

    private IJavaCompletionProposal tryCreateJdtProposal(final CompletionProposal proposal) {
        final int previousProposalsCount = collector.getJavaCompletionProposals().length;
        collector.accept(proposal);
        final boolean isAccepted = collector.getJavaCompletionProposals().length > previousProposalsCount;
        if (isAccepted) {
            return collector.getJavaCompletionProposals()[previousProposalsCount];
        } else {
            return null;
        }
    }

    private void createSubwordsProposal(final CompletionProposal proposal, final IJavaCompletionProposal jdtProposal) {
        final AbstractJavaCompletionProposal subWordProposal = SubwordsCompletionProposalFactory.createFromJDTProposal(
                jdtProposal, proposal, ctx, token);
        if (subWordProposal != null) {
            subWordProposal.setRelevance(relevanceCalculator.getRelevance());
            proposals.add(subWordProposal);
        }
    }

    public List<IJavaCompletionProposal> getProposals() {
        return proposals;
    }
}