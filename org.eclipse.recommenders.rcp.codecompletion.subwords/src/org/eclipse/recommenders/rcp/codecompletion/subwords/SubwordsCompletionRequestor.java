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

    private final String prefix;

    public SubwordsCompletionRequestor(final String prefix, final JavaContentAssistInvocationContext ctx) {
        checkNotNull(prefix);
        checkNotNull(ctx);
        this.prefix = prefix;
        this.ctx = ctx;
        this.collector = new CompletionProposalCollector(ctx.getCompilationUnit());
        this.collector.acceptContext(ctx.getCoreContext());
    }

    @Override
    public void accept(final CompletionProposal proposal) {
        // REVIEW: Name mismatch: it's not just a score calculator. It also
        // computes regex match etc. Pls rename.

        // bug: proposal.getCompletion for method overrides does not play well
        // with getTokensUntilFirstOpeningBracket!
        final String subwordsMatchingRegion = getTokensBetweenLastWhitespaceAndFirstOpeningBracket(proposal
                .getCompletion());
        if (!SubwordsUtils.checkStringMatchesPrefixPattern(prefix, subwordsMatchingRegion)) {
            return;
        }

        final IJavaCompletionProposal jdtProposal = tryCreateJdtProposal(proposal);
        if (jdtProposal == null) {
            return;
        }

        final SubwordsProposalContext state = new SubwordsProposalContext(prefix, proposal, jdtProposal, ctx);

        createSubwordsProposal(state);

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

    private void createSubwordsProposal(final SubwordsProposalContext state) {
        final AbstractJavaCompletionProposal subWordProposal = SubwordsCompletionProposalFactory
                .createFromJDTProposal(state);
        if (subWordProposal != null) {
            subWordProposal.setRelevance(SubwordsUtils.calculateRelevance(state));
            proposals.add(subWordProposal);
        }
    }

    public List<IJavaCompletionProposal> getProposals() {
        return proposals;
    }
}