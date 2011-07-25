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

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.LazyJavaTypeCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.viewers.StyledString;

@SuppressWarnings("restriction")
public class SubwordsJavaTypeCompletionProposal extends LazyJavaTypeCompletionProposal {

    public static SubwordsJavaTypeCompletionProposal create(final SubwordsProposalContext subwordsContext) {
        return new SubwordsJavaTypeCompletionProposal(subwordsContext.getProposal(), subwordsContext.getContext(),
                subwordsContext);
    }

    private final SubwordsProposalContext subwordsContext;

    private SubwordsJavaTypeCompletionProposal(final CompletionProposal proposal,
            final JavaContentAssistInvocationContext context, final SubwordsProposalContext subwordsContext) {
        super(proposal, context);
        this.subwordsContext = subwordsContext;
    }

    @Override
    protected boolean isPrefix(final String prefix, final String completion) {
        subwordsContext.setPrefix(prefix);
        setRelevance(subwordsContext.calculateRelevance());
        return subwordsContext.isRegexMatchButNoPrefixMatch();
    }

    @Override
    public StyledString getStyledDisplayString() {
        final StyledString origin = super.getStyledDisplayString();
        return subwordsContext.getStyledDisplayString(origin);
    }
}
