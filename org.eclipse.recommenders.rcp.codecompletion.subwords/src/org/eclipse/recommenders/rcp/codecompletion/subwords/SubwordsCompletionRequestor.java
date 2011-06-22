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
import org.eclipse.jdt.ui.text.java.CompletionProposalCollector;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;

import com.google.common.collect.Lists;

public class SubwordsCompletionRequestor extends CompletionRequestor {

    private final List<IJavaCompletionProposal> proposals = Lists.newLinkedList();

    private final Pattern pattern;

    private final JavaContentAssistInvocationContext ctx;

    private final CompletionProposalCollector collector;

    public SubwordsCompletionRequestor(final String token, final JavaContentAssistInvocationContext ctx) {
        checkNotNull(token);
        checkNotNull(ctx);
        this.ctx = ctx;
        this.pattern = createRegexPatternFromPrefix(token);
        this.collector = new CompletionProposalCollector(ctx.getCompilationUnit());
        this.collector.acceptContext(ctx.getCoreContext());
    }

    @Override
    public void accept(final CompletionProposal proposal) {
        int previousProposalsCount = collector.getJavaCompletionProposals().length;
        collector.accept(proposal);
        boolean isAccepted = collector.getJavaCompletionProposals().length > previousProposalsCount;
        if (isAccepted) {
            IJavaCompletionProposal collectorProposal = collector.getJavaCompletionProposals()[previousProposalsCount];
            String completion = getTokensUntilFirstOpeningBracket(proposal.getCompletion());
            Matcher m = pattern.matcher(completion);
            if (m.matches()) {
                final IJavaCompletionProposal subWordProposal = SubwordsCompletionProposalFactory
                        .createFromJDTProposal(collectorProposal, proposal, ctx);
                proposals.add(subWordProposal);
            }
        }
    }

    public List<IJavaCompletionProposal> getProposals() {
        return proposals;
    }
}