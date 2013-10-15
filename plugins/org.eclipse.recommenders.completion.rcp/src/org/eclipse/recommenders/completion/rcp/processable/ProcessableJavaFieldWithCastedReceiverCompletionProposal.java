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
import static org.eclipse.recommenders.utils.Checks.ensureIsNotNull;

import java.util.Map;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.JavaFieldWithCastedReceiverCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;

@SuppressWarnings("restriction")
public class ProcessableJavaFieldWithCastedReceiverCompletionProposal extends
        JavaFieldWithCastedReceiverCompletionProposal implements IProcessableProposal {

    private Map<String, Object> tags = Maps.newHashMap();
    private ProposalProcessorManager mgr;
    private CompletionProposal coreProposal;
    private String lastPrefix;

    protected ProcessableJavaFieldWithCastedReceiverCompletionProposal(CompletionProposal coreProposal,
            JavaCompletionProposal uiProposal, JavaContentAssistInvocationContext context) throws JavaModelException {
        super(uiProposal.getReplacementString(), uiProposal.getReplacementOffset(), uiProposal.getReplacementLength(),
                uiProposal.getImage(), uiProposal.getStyledDisplayString(), uiProposal.getRelevance(), true, context,
                coreProposal);
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
    }

    @Override
    public ProposalProcessorManager getProposalProcessorManager() {
        return mgr;
    }

    @Override
    public void setProposalProcessorManager(ProposalProcessorManager mgr) {
        this.mgr = mgr;
    }

    @Override
    public void setTag(String key, Object value) {
        ensureIsNotNull(key);
        if (value == null) {
            tags.remove(key);
        } else {
            tags.put(key, value);
        }
    }

    @Override
    public <T> Optional<T> getTag(String key) {
        return Optional.fromNullable((T) tags.get(key));
    }

    @Override
    public <T> T getTag(String key, T defaultValue) {
        T res = (T) tags.get(key);
        return res != null ? res : defaultValue;
    }
}
