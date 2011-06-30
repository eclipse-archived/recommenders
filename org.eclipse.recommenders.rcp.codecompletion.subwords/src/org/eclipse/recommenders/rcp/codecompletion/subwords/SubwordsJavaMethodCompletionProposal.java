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

import static org.eclipse.recommenders.rcp.codecompletion.subwords.RegexUtil.checkStringMatchesPrefixPattern;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.JavaMethodCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.viewers.StyledString;

@SuppressWarnings("restriction")
public final class SubwordsJavaMethodCompletionProposal extends JavaMethodCompletionProposal {
    private String token;

    public SubwordsJavaMethodCompletionProposal(final CompletionProposal proposal,
            final JavaContentAssistInvocationContext context, final String token) {
        super(proposal, context);
        this.token = token;
    }

    @Override
    protected boolean isPrefix(final String prefix, final String completion) {
        this.token = prefix;
        return checkStringMatchesPrefixPattern(prefix, completion);
    }

    @Override
    public StyledString getStyledDisplayString() {
        final StyledString origin = super.getStyledDisplayString();
        return NGramsUtils.style(origin, token);
    }
}