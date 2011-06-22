/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Paul-Emmanuel Faidherbe - Completion generalization
 */
package org.eclipse.recommenders.rcp.codecompletion.subwords;

import static org.eclipse.recommenders.rcp.codecompletion.subwords.RegexUtil.createRegexPatternFromPrefix;
import static org.eclipse.recommenders.rcp.codecompletion.subwords.RegexUtil.getTokensUntilFirstOpeningBracket;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.ui.text.java.OverrideCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;

@SuppressWarnings("restriction")
public class SubwordsOverrideCompletionProposal extends OverrideCompletionProposal {

    public SubwordsOverrideCompletionProposal(final OverrideCompletionProposal jdtProposal,
            final CompletionProposal completionProposal, final JavaContentAssistInvocationContext ctx) {
        super(ctx.getProject(), ctx.getCompilationUnit(), String.valueOf(completionProposal.getName()), Signature
                .getParameterTypes(String.valueOf(completionProposal.getSignature())), completionProposal
                .getReplaceStart(), jdtProposal.getReplacementLength(), jdtProposal.getStyledDisplayString(), String
                .valueOf(completionProposal.getCompletion()));
    }

    @Override
    protected boolean isPrefix(final String prefix, String completion) {
        final Pattern pattern = createRegexPatternFromPrefix(prefix);
        completion = getTokensUntilFirstOpeningBracket(completion);
        final Matcher m = pattern.matcher(completion);
        final boolean matches = m.matches();
        return matches;
    }

}
