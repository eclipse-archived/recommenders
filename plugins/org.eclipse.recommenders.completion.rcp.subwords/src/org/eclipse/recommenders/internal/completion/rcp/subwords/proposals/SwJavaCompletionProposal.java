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
package org.eclipse.recommenders.internal.completion.rcp.subwords.proposals;

import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.recommenders.internal.completion.rcp.subwords.SubwordsProposalContext;
import org.eclipse.swt.graphics.Image;

@SuppressWarnings("restriction")
public class SwJavaCompletionProposal extends org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal {

    private final SubwordsProposalContext subwordsContext;

    protected SwJavaCompletionProposal(final String replacementString, final int replacementOffset,
            final int replacementLength, final Image image, final StyledString displayString, final int relevance,
            final boolean inJavadoc, final JavaContentAssistInvocationContext invocationContext,
            final SubwordsProposalContext subwordsContext) {
        super(replacementString, replacementOffset, replacementLength, image, displayString, relevance, inJavadoc,
                invocationContext);
        this.subwordsContext = subwordsContext;
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
