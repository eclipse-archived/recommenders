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
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.ui.text.java.AnonymousTypeCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.recommenders.internal.completion.rcp.subwords.SubwordsProposalContext;

@SuppressWarnings("restriction")
public class SwAnonymousTypeCompletionProposal extends AnonymousTypeCompletionProposal {

    private final SubwordsProposalContext subwordsContext;

    protected SwAnonymousTypeCompletionProposal(final IJavaProject jproject, final ICompilationUnit cu,
            final JavaContentAssistInvocationContext invocationContext, final int start, final int length,
            final String constructorCompletion, final StyledString displayName, final String declarationSignature,
            final IType superType, final int relevance, final SubwordsProposalContext subwordsContext) {
        super(jproject, cu, invocationContext, start, length, constructorCompletion, displayName, declarationSignature,
                superType, relevance);
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
