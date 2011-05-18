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
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;

import com.google.common.collect.Lists;

public class SubwordsCompletionRequestor extends CompletionRequestor {

    private final List<IJavaCompletionProposal> proposals = Lists.newLinkedList();

    private final Pattern pattern;

    private final JavaContentAssistInvocationContext ctx;

    SubwordsCompletionRequestor(final String token, final JavaContentAssistInvocationContext ctx) {
        checkNotNull(token);
        checkNotNull(ctx);
        this.ctx = ctx;
        pattern = createRegexPatternFromPrefix(token);
    }

    @Override
    public void accept(final CompletionProposal proposal) {
        switch (proposal.getKind()) {
        case CompletionProposal.METHOD_REF:
        case CompletionProposal.CONSTRUCTOR_INVOCATION:
        case CompletionProposal.METHOD_REF_WITH_CASTED_RECEIVER:
        case CompletionProposal.METHOD_NAME_REFERENCE:
        case CompletionProposal.JAVADOC_METHOD_REF:
            final String completion = getTokensUntilFirstOpeningBracket(proposal.getCompletion());
            final Matcher m = pattern.matcher(completion);
            if (m.matches()) {
                final IJavaCompletionProposal javaProposal = new SubwordsJavaMethodCompletionProposal(proposal, ctx);
                proposals.add(javaProposal);
            }
        }
    }

    public List<IJavaCompletionProposal> getProposals() {
        return proposals;
    }
}