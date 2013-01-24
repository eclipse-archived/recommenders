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

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.recommenders.internal.completion.rcp.subwords.SubwordsProposalContext;

@SuppressWarnings("restriction")
public class SwOverrideCompletionProposal extends org.eclipse.jdt.internal.ui.text.java.OverrideCompletionProposal {

    private final SubwordsProposalContext subwordsContext;

    protected SwOverrideCompletionProposal(final IJavaProject jproject, final ICompilationUnit cu,
            final String methodName, final String[] paramTypes, final int start, final int length,
            final StyledString displayName, final String completionProposal,
            final SubwordsProposalContext subwordsContext) {
        super(jproject, cu, methodName, paramTypes, start, length, displayName, completionProposal);

        this.subwordsContext = subwordsContext;
        setRelevance(subwordsContext.calculateRelevance());
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
