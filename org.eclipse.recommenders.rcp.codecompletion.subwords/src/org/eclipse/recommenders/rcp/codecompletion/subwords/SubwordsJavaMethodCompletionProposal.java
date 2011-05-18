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

import static org.eclipse.recommenders.rcp.codecompletion.subwords.RegexUtil.createRegexPatternFromPrefix;
import static org.eclipse.recommenders.rcp.codecompletion.subwords.RegexUtil.getTokensUntilFirstOpeningBracket;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.JavaMethodCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;

@SuppressWarnings("restriction")
public final class SubwordsJavaMethodCompletionProposal extends JavaMethodCompletionProposal {
    public SubwordsJavaMethodCompletionProposal(final CompletionProposal proposal,
            final JavaContentAssistInvocationContext context) {
        super(proposal, context);
    }

    @Override
    public boolean isPrefix(final String prefix, String completion) {
        final Pattern pattern = createRegexPatternFromPrefix(prefix);
        completion = getTokensUntilFirstOpeningBracket(completion);
        final Matcher m = pattern.matcher(completion);
        final boolean matches = m.matches();
        System.out.println(completion);
        return matches;
    }

}