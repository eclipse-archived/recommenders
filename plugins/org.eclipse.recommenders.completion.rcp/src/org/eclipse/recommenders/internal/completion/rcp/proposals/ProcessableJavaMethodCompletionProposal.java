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
package org.eclipse.recommenders.internal.completion.rcp.proposals;

import static com.google.common.base.Optional.fromNullable;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.JavaMethodCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.recommenders.completion.rcp.IProcessableProposal;
import org.eclipse.recommenders.completion.rcp.ProposalProcessorManager;

import com.google.common.base.Optional;

public final class ProcessableJavaMethodCompletionProposal extends JavaMethodCompletionProposal implements
        IProcessableProposal {

    private ProposalProcessorManager mgr;
    private CompletionProposal coreProposal;
    private String lastPrefix;

    protected ProcessableJavaMethodCompletionProposal(final CompletionProposal coreProposal,
            final JavaContentAssistInvocationContext context) {
        super(coreProposal, context);
        this.coreProposal = coreProposal;
    }

    // ===========

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
    }    @Override
    public ProposalProcessorManager getProposalProcessorManager() {
        return mgr;
    }
    @Override
    public void setProposalProcessorManager(ProposalProcessorManager mgr) {
        this.mgr = mgr;
    }

}