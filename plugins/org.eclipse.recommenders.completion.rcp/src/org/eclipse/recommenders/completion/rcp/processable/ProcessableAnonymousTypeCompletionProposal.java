/**
 * Copyright (c) 2010, 2012 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - Initial API
 */
package org.eclipse.recommenders.completion.rcp.processable;

import static com.google.common.base.Optional.fromNullable;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.text.java.AnonymousTypeCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.AnonymousTypeProposalInfo;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.ProposalInfo;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.viewers.StyledString;

import com.google.common.base.Optional;

@SuppressWarnings("restriction")
public class ProcessableAnonymousTypeCompletionProposal extends AnonymousTypeCompletionProposal implements
        IProcessableProposal {

    private ProposalProcessorManager mgr;
    private CompletionProposal coreProposal;
    private String lastPrefix;

    public ProcessableAnonymousTypeCompletionProposal(CompletionProposal coreProposal,
            JavaCompletionProposal uiProposal, JavaContentAssistInvocationContext context) throws JavaModelException {
        super(context.getProject(), context.getCompilationUnit(), context, coreProposal.getReplaceStart(), uiProposal
                .getReplacementLength(), String.valueOf(coreProposal.getCompletion()), uiProposal
                .getStyledDisplayString(), String.valueOf(coreProposal.getDeclarationSignature()), (IType) context
                .getProject().findElement(String.valueOf(coreProposal.getDeclarationKey()), null), uiProposal
                .getRelevance());
        this.coreProposal = coreProposal;
    }

    protected ProcessableAnonymousTypeCompletionProposal(final IJavaProject jproject, final ICompilationUnit cu,
            final JavaContentAssistInvocationContext invocationContext, final int start, final int length,
            final String constructorCompletion, final StyledString displayName, final String declarationSignature,
            final IType superType, final int relevance) {
        super(jproject, cu, invocationContext, start, length, constructorCompletion, displayName, declarationSignature,
                superType, relevance);
    }

    @Override
    protected ProposalInfo getProposalInfo() {
        ProposalInfo info = super.getProposalInfo();
        if (info == null) {
            final IJavaProject project = fInvocationContext.getProject();
            info = new AnonymousTypeProposalInfo(project, coreProposal);
            setProposalInfo(info);
        }
        return info;
    }

    @Override
    public boolean isPrefix(final String prefix, final String completion) {
        lastPrefix = prefix;
        if (mgr.prefixChanged(prefix)) {
            return true;
        }
        return super.isPrefix(prefix, completion);
    }

    @Override
    public String getPrefix() {
        return lastPrefix;
    }

    @Override
    public Optional<CompletionProposal> getCoreProposal() {
        return fromNullable(coreProposal);
    }

    @Override
    public ProposalProcessorManager getProposalProcessorManager() {
        return mgr;
    }

    @Override
    public void setProposalProcessorManager(ProposalProcessorManager mgr) {
        this.mgr = mgr;
    }
}
