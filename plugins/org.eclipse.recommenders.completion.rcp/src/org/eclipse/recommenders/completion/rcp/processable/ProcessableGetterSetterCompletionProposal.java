/**
 * Copyright (c) 2010, 2012 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.completion.rcp.processable;

import static com.google.common.base.Optional.fromNullable;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.text.java.GetterSetterCompletionProposal;

import com.google.common.base.Optional;

@SuppressWarnings("restriction")
public class ProcessableGetterSetterCompletionProposal extends GetterSetterCompletionProposal implements
        IProcessableProposal {

    private String lastPrefix;
    private ProposalProcessorManager mgr;
    private CompletionProposal coreProposal;

    public ProcessableGetterSetterCompletionProposal(CompletionProposal coreProposal, IField field, boolean isGetter,
            int relevance) throws JavaModelException {
        super(field, coreProposal.getReplaceStart(), coreProposal.getReplaceEnd() - coreProposal.getReplaceStart(),
                isGetter, relevance);
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
