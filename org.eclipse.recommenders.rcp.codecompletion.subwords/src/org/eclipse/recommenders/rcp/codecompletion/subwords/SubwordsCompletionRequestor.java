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
import static org.eclipse.recommenders.rcp.codecompletion.subwords.RegexUtil.createRegexPatternFromPrefix;
import static org.eclipse.recommenders.rcp.codecompletion.subwords.RegexUtil.getTokensUntilFirstOpeningBracket;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private final Pattern pattern;

    private final JavaContentAssistInvocationContext ctx;

    private final CompletionProposalCollector collector;

    private final String token;

    public SubwordsCompletionRequestor(final String token, final JavaContentAssistInvocationContext ctx) {
        this.token = token;
        checkNotNull(token);
        checkNotNull(ctx);
        this.ctx = ctx;
        this.pattern = createRegexPatternFromPrefix(token);
        this.collector = new CompletionProposalCollector(ctx.getCompilationUnit());
        this.collector.acceptContext(ctx.getCoreContext());
    }

    @Override
    public void accept(final CompletionProposal proposal) {
        final IJavaCompletionProposal jdtProposal = tryCreateJdtProposal(proposal);
        if (jdtProposal == null) {
            return;
        }
        final String completion = getTokensUntilFirstOpeningBracket(proposal.getCompletion());
        final int distance = calculateDistance(completion);
        if (matchesToken(completion)) {
            createSubwordsProposal(proposal, jdtProposal, distance);
        }
    }

    private void createSubwordsProposal(final CompletionProposal proposal, final IJavaCompletionProposal jdtProposal,
            final int distance) {
        final AbstractJavaCompletionProposal subWordProposal = SubwordsCompletionProposalFactory.createFromJDTProposal(
                jdtProposal, proposal, ctx);
        if (subWordProposal != null) {
            subWordProposal.setRelevance(distance);
            proposals.add(subWordProposal);
        }
    }

    private boolean matchesToken(final String completion) {
        final Matcher m = pattern.matcher(completion);
        return m.matches();
    }

    private int calculateDistance(final String completion) {
        return SubwordsRelevanceCalculator.calculateRelevance(token, completion);
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

    public List<IJavaCompletionProposal> getProposals() {
        return proposals;
    }
}