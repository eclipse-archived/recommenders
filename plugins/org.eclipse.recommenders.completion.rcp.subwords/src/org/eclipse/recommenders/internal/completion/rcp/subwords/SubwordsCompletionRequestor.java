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
package org.eclipse.recommenders.internal.completion.rcp.subwords;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Math.floor;
import static java.lang.Math.log;
import static org.apache.commons.lang3.StringUtils.getLevenshteinDistance;
import static org.apache.commons.lang3.StringUtils.substring;
import static org.eclipse.recommenders.internal.completion.rcp.subwords.SubwordsUtils.getTokensBetweenLastWhitespaceAndFirstOpeningBracket;

import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.internal.ui.text.java.AbstractJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.CompletionProposalCollector;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@SuppressWarnings("restriction")
public class SubwordsCompletionRequestor extends CompletionRequestor {

    private final List<IJavaCompletionProposal> proposals = Lists.newLinkedList();

    private final Set<String> duplicates = Sets.newHashSet();
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
        if (isDuplicate(proposal)) {
            return;
        }

        final String subwordsMatchingRegion = getTokensBetweenLastWhitespaceAndFirstOpeningBracket(proposal
                .getCompletion());
        if (!SubwordsUtils.checkStringMatchesPrefixPattern(prefix, subwordsMatchingRegion)
                && !levenshtein(prefix, subwordsMatchingRegion)) {
            return;
        }

        final IJavaCompletionProposal jdtProposal = tryCreateJdtProposal(proposal);
        if (jdtProposal == null) {
            return;
        }

        final SubwordsProposalContext subwordsContext = new SubwordsProposalContext(prefix, proposal, jdtProposal, ctx);

        createSubwordsProposal(subwordsContext);

    }

    private boolean isDuplicate(final CompletionProposal proposal) {
        final String completion = String.valueOf(proposal.getCompletion());
        return !duplicates.add(completion);
    }

    private boolean levenshtein(String prefix, final String subwordsMatchingRegion) {
        if (prefix.length() < 2) {
            return false;
        }
        final int maxDistance = (int) floor(log(prefix.length()));

        prefix = prefix.toLowerCase();
        final String completionPrefix = substring(subwordsMatchingRegion, 0, prefix.length()).toLowerCase();
        final int distance = getLevenshteinDistance(completionPrefix, prefix, maxDistance);
        // no exact matches:
        if (distance <= 0) {
            return false;
        }
        return true;
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

    private void createSubwordsProposal(final SubwordsProposalContext subwordsContext) {
        final AbstractJavaCompletionProposal subWordProposal = SubwordsCompletionProposalFactory
                .createFromJDTProposal(subwordsContext);
        if (subWordProposal != null) {
            // subWordProposal.setRelevance(subwordsContext.calculateRelevance());
            proposals.add(subWordProposal);
        }
    }

    public List<IJavaCompletionProposal> getProposals() {
        return proposals;
    }
}