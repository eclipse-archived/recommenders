/**
 * Copyright (c) 2011 Paul-Emmanuel Faidherbe.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Paul-Emmanuel Faidherbe - Completion generalization
 */
package org.eclipse.recommenders.internal.completion.rcp.subwords.proposals;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.LazyJavaTypeCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.recommenders.internal.completion.rcp.subwords.SubwordsProposalContext;

@SuppressWarnings("restriction")
public class SwLazyJavaTypeCompletionProposal extends LazyJavaTypeCompletionProposal {

    private final SubwordsProposalContext subwordsContext;

    protected SwLazyJavaTypeCompletionProposal(final CompletionProposal proposal,
            final JavaContentAssistInvocationContext context, final SubwordsProposalContext subwordsContext) {
        super(proposal, context);
        this.subwordsContext = subwordsContext;
    }

    @Override
    public void apply(final IDocument document, final char trigger, final int offset) {
        // final JavaCompletionProposal p = subwordsContext.getJdtProposal();
        // p.apply(document, trigger, offset);
        super.apply(document, trigger, offset);
    }

    @Override
    protected boolean isPrefix(final String prefix, final String completion) {
        subwordsContext.setPrefix(prefix);
        // setRelevance(subwordsContext.calculateRelevance());
        return subwordsContext.isRegexMatch();
    }

    @Override
    public StyledString getStyledDisplayString() {
        final StyledString origin = super.getStyledDisplayString();
        return subwordsContext.getStyledDisplayString(origin);
    }
}
