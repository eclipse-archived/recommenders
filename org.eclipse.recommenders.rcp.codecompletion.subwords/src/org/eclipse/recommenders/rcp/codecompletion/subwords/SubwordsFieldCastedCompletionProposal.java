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

import static org.eclipse.recommenders.commons.utils.Checks.ensureIsNotNull;
import static org.eclipse.recommenders.rcp.codecompletion.subwords.SubwordsUtils.checkStringMatchesPrefixPattern;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.JavaFieldWithCastedReceiverCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.viewers.StyledString;

@SuppressWarnings("restriction")
public class SubwordsFieldCastedCompletionProposal extends JavaFieldWithCastedReceiverCompletionProposal {

    private String token;

    public SubwordsFieldCastedCompletionProposal(final JavaFieldWithCastedReceiverCompletionProposal jdtProposal,
            final CompletionProposal originalProposal, final JavaContentAssistInvocationContext context,
            final String token) {
        super(jdtProposal.getDisplayString(), jdtProposal.getReplacementOffset(), jdtProposal.getReplacementLength(),
                jdtProposal.getImage(), jdtProposal.getStyledDisplayString(), jdtProposal.getRelevance(), true,
                context, originalProposal);
        this.token = ensureIsNotNull(token);
    }

    @Override
    protected boolean isPrefix(final String prefix, final String completion) {
        this.token = ensureIsNotNull(prefix);
        return checkStringMatchesPrefixPattern(prefix, completion);
    }

    @Override
    public StyledString getStyledDisplayString() {
        final StyledString origin = super.getStyledDisplayString();
        return SubwordsUtils.createStyledProposalDisplayString(origin, token);
    }
}
