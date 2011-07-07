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
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.JavaFieldWithCastedReceiverCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;

@SuppressWarnings("restriction")
public class SubwordsFieldCastedCompletionProposal extends JavaFieldWithCastedReceiverCompletionProposal {

    public static SubwordsFieldCastedCompletionProposal create(final SubwordsProposalContext subwordsContext) {
        final JavaCompletionProposal jdtProposal = subwordsContext.getJdtProposal();

        return new SubwordsFieldCastedCompletionProposal(jdtProposal.getDisplayString(),
                jdtProposal.getReplacementOffset(), jdtProposal.getReplacementLength(), jdtProposal.getImage(),
                jdtProposal.getStyledDisplayString(), jdtProposal.getRelevance(), true, subwordsContext.getContext(),
                subwordsContext.getProposal(), subwordsContext);
    }

    private final SubwordsProposalContext subwordsContext;

    private SubwordsFieldCastedCompletionProposal(final String completion, final int start, final int length,
            final Image image, final StyledString label, final int relevance, final boolean inJavadoc,
            final JavaContentAssistInvocationContext invocationContext, final CompletionProposal proposal,
            final SubwordsProposalContext subwordsContext) {
        super(completion, start, length, image, label, relevance, inJavadoc, invocationContext, proposal);
        this.subwordsContext = subwordsContext;
    }

    @Override
    protected boolean isPrefix(final String prefix, final String completion) {
        subwordsContext.setPrefix(prefix);
        setRelevance(subwordsContext.calculateRelevance());
        return subwordsContext.isRegexMatch();
    }

    @Override
    public StyledString getStyledDisplayString() {
        final StyledString origin = super.getStyledDisplayString();
        return subwordsContext.getStyledDisplayString(origin);
    }
}
