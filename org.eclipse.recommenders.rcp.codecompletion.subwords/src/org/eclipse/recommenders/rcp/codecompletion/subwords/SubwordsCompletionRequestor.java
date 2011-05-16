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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.internal.ui.text.java.JavaMethodCompletionProposal;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;

import com.google.common.collect.Lists;

@SuppressWarnings("restriction")
final class SubwordsCompletionRequestor extends CompletionRequestor {

    private final List<IJavaCompletionProposal> proposals = Lists.newLinkedList();

    final Pattern subwordPattern;

    private final JavaContentAssistInvocationContext ctx;

    SubwordsCompletionRequestor(final String token, final JavaContentAssistInvocationContext ctx) {
        checkNotNull(token);
        checkNotNull(ctx);
        this.ctx = ctx;
        subwordPattern = createRegexPatternFromPrefix(token);
    }

    private Pattern createRegexPatternFromPrefix(final String prefixToken) {
        final StringBuilder sb = new StringBuilder();
        sb.append(".*");
        for (final char c : prefixToken.toCharArray()) {
            sb.append(c);
            sb.append(".*");
        }
        final Pattern p = Pattern.compile(sb.toString());
        return p;
    }

    @Override
    public void accept(final CompletionProposal proposal) {
        switch (proposal.getKind()) {
        case CompletionProposal.METHOD_REF:
        case CompletionProposal.CONSTRUCTOR_INVOCATION:
        case CompletionProposal.METHOD_REF_WITH_CASTED_RECEIVER:
        case CompletionProposal.METHOD_NAME_REFERENCE:
            String completion = String.valueOf(proposal.getCompletion());
            completion = getIdentifierInLowerCase(completion);
            final Matcher m = subwordPattern.matcher(completion);
            if (m.matches()) {
                final IJavaCompletionProposal javaProposal = createJavaCompletionProposal(proposal);
                proposals.add(javaProposal);
            }
        }
    }

    private IJavaCompletionProposal createJavaCompletionProposal(final CompletionProposal proposal) {
        return new JavaMethodCompletionProposal(proposal, ctx) {
            @Override
            protected boolean isPrefix(final String prefix, final String completion) {
                final String testee = getIdentifierInLowerCase(completion);
                final Matcher m = createRegexPatternFromPrefix(prefix).matcher(testee);
                final boolean matches = m.matches();
                return matches;
            };
        };
    }

    private String getIdentifierInLowerCase(final String proposal) {
        String identifier = StringUtils.substringBefore(proposal, "(");
        identifier = identifier.toLowerCase();
        return identifier;
    }

    public List<IJavaCompletionProposal> getProposals() {
        return proposals;
    }
}