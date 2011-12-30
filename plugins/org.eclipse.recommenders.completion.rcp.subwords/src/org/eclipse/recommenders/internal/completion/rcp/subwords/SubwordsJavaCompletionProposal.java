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
package org.eclipse.recommenders.internal.completion.rcp.subwords;

import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;

@SuppressWarnings("restriction")
public class SubwordsJavaCompletionProposal extends JavaCompletionProposal {

    public static SubwordsJavaCompletionProposal create(final SubwordsProposalContext subwordsContext) {
        final JavaCompletionProposal jdtProposal = subwordsContext.getJdtProposal();
        return new SubwordsJavaCompletionProposal(jdtProposal.getReplacementString(), subwordsContext.getProposal()
                .getReplaceStart(), jdtProposal.getReplacementLength(), jdtProposal.getImage(),
                jdtProposal.getStyledDisplayString(), jdtProposal.getRelevance(), true, subwordsContext.getContext(),
                subwordsContext);
    }

    private final SubwordsProposalContext subwordsContext;

    private SubwordsJavaCompletionProposal(final String replacementString, final int replacementOffset,
            final int replacementLength, final Image image, final StyledString displayString, final int relevance,
            final boolean inJavadoc, final JavaContentAssistInvocationContext invocationContext,
            final SubwordsProposalContext subwordsContext) {
        super(replacementString, replacementOffset, replacementLength, image, displayString, relevance, inJavadoc,
                invocationContext);
        this.subwordsContext = subwordsContext;
    }

    @Override
    public void apply(final IDocument document, final char trigger, final int offset) {
        // TODO Auto-generated method stub
        super.apply(document, trigger, offset);
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
