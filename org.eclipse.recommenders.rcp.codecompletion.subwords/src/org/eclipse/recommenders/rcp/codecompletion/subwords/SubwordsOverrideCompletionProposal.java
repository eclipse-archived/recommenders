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
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.OverrideCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.viewers.StyledString;

@SuppressWarnings("restriction")
public class SubwordsOverrideCompletionProposal extends OverrideCompletionProposal {

    public static SubwordsOverrideCompletionProposal create(final SubwordsProposalContext subwordsContext) {
        final JavaContentAssistInvocationContext context = subwordsContext.getContext();
        final JavaCompletionProposal jdtProposal = subwordsContext.getJdtProposal();
        final CompletionProposal proposal = subwordsContext.getProposal();
        final String signature = String.valueOf(proposal.getSignature());
        final String completionText = String.valueOf(proposal.getCompletion());
        final String proposalName = String.valueOf(proposal.getName());
        return new SubwordsOverrideCompletionProposal(context.getProject(), context.getCompilationUnit(), proposalName,
                Signature.getParameterTypes(signature), proposal.getReplaceStart(), jdtProposal.getReplacementLength(),
                jdtProposal.getStyledDisplayString(), completionText, subwordsContext);
    }

    private final SubwordsProposalContext subwordsContext;

    private SubwordsOverrideCompletionProposal(final IJavaProject jproject, final ICompilationUnit cu,
            final String methodName, final String[] paramTypes, final int start, final int length,
            final StyledString displayName, final String completionProposal,
            final SubwordsProposalContext subwordsContext) {
        super(jproject, cu, methodName, paramTypes, start, length, displayName, completionProposal);
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
