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
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.text.java.AnonymousTypeCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.viewers.StyledString;

@SuppressWarnings("restriction")
public class SubwordsAnonymousCompletionProposal extends AnonymousTypeCompletionProposal {

    public static SubwordsAnonymousCompletionProposal create(final SubwordsProposalContext subwordsContext)
            throws JavaModelException {
        final JavaContentAssistInvocationContext context = subwordsContext.getContext();
        final CompletionProposal proposal = subwordsContext.getProposal();
        final JavaCompletionProposal jdtProposal = subwordsContext.getJdtProposal();
        final IJavaProject project = context.getProject();
        final String declarationSignature = String.valueOf(proposal.getDeclarationSignature());
        final String declarationKey = String.valueOf(proposal.getDeclarationKey());
        final String completionText = String.valueOf(proposal.getCompletion());

        return new SubwordsAnonymousCompletionProposal(project, context.getCompilationUnit(), context,
                proposal.getReplaceStart(), jdtProposal.getReplacementLength(), completionText,
                jdtProposal.getStyledDisplayString(), declarationSignature, (IType) project.findElement(declarationKey,
                        null), jdtProposal.getRelevance(), subwordsContext);
    }

    private final SubwordsProposalContext subwordsContext;

    private SubwordsAnonymousCompletionProposal(final IJavaProject jproject, final ICompilationUnit cu,
            final JavaContentAssistInvocationContext invocationContext, final int start, final int length,
            final String constructorCompletion, final StyledString displayName, final String declarationSignature,
            final IType superType, final int relevance, final SubwordsProposalContext subwordsContext) {
        super(jproject, cu, invocationContext, start, length, constructorCompletion, displayName, declarationSignature,
                superType, relevance);
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
