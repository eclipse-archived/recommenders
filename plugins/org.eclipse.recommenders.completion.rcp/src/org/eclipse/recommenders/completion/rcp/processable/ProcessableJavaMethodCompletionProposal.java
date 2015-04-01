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
import static org.eclipse.recommenders.completion.rcp.processable.ProposalTag.IS_VISIBLE;
import static org.eclipse.recommenders.utils.Checks.ensureIsNotNull;

import java.lang.reflect.Field;
import java.util.Map;

import org.eclipse.jdt.core.CompletionContext;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.JavaMethodCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.LazyJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.CompletionProposalCollector;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.recommenders.utils.Reflections;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

@SuppressWarnings({ "restriction", "unchecked" })
public class ProcessableJavaMethodCompletionProposal extends JavaMethodCompletionProposal implements
        IProcessableProposal {


    private Map<IProposalTag, Object> tags = Maps.newHashMap();
    private ProposalProcessorManager mgr;
    private CompletionProposal coreProposal;
    private String lastPrefix;

    protected ProcessableJavaMethodCompletionProposal(final CompletionProposal coreProposal,
            final JavaContentAssistInvocationContext context) {
        super(coreProposal, context);
        this.coreProposal = coreProposal;
    }

    // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=435597
    private static final Field JAVA_CONTENT_ASSIST_INVOCATION_CONTEXT_F_CORE_CONTEXT = Reflections.getDeclaredField(
            JavaContentAssistInvocationContext.class, "fCoreContext").orNull();
    private static final Field JAVA_CONTENT_ASSIST_INVOCATION_CONTEXT_F_COLLECTOR = Reflections.getDeclaredField(
            JavaContentAssistInvocationContext.class, "fCollector").orNull();
    private static final Field COMPLETION_PROPOSAL_COLLECTOR_F_CONTEXT = Reflections.getDeclaredField(
            CompletionProposalCollector.class, "fContext").orNull();

    @Override
    protected LazyJavaCompletionProposal createRequiredTypeCompletionProposal(CompletionProposal completionProposal,
            JavaContentAssistInvocationContext invocationContext) {
        if (JAVA_CONTENT_ASSIST_INVOCATION_CONTEXT_F_CORE_CONTEXT != null && JAVA_CONTENT_ASSIST_INVOCATION_CONTEXT_F_COLLECTOR != null && COMPLETION_PROPOSAL_COLLECTOR_F_CONTEXT != null) {
            try {
                CompletionContext oldCoreContext = (CompletionContext) JAVA_CONTENT_ASSIST_INVOCATION_CONTEXT_F_CORE_CONTEXT
                        .get(invocationContext);
                CompletionProposalCollector collector = (CompletionProposalCollector) JAVA_CONTENT_ASSIST_INVOCATION_CONTEXT_F_COLLECTOR
                        .get(invocationContext);
                CompletionContext newCoreContext = (CompletionContext) COMPLETION_PROPOSAL_COLLECTOR_F_CONTEXT.get(collector);
                JAVA_CONTENT_ASSIST_INVOCATION_CONTEXT_F_CORE_CONTEXT.set(invocationContext, newCoreContext);
                LazyJavaCompletionProposal proposal = super.createRequiredTypeCompletionProposal(completionProposal,
                        invocationContext);
                JAVA_CONTENT_ASSIST_INVOCATION_CONTEXT_F_CORE_CONTEXT.set(invocationContext, oldCoreContext);
                return proposal;
            } catch (IllegalArgumentException | IllegalAccessException e) {
                return super.createRequiredTypeCompletionProposal(completionProposal, invocationContext);
            }
        } else {
            return super.createRequiredTypeCompletionProposal(completionProposal, invocationContext);
        }
    }

    // ===========

    @Override
    public boolean isPrefix(final String prefix, final String completion) {
        lastPrefix = prefix;
        boolean res = mgr.prefixChanged(prefix) || super.isPrefix(prefix, completion);
        setTag(IS_VISIBLE, res);
        return res;
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
    public void setTag(IProposalTag key, Object value) {
        ensureIsNotNull(key);
        if (value == null) {
            tags.remove(key);
        } else {
            tags.put(key, value);
        }
    }

    @Override
    public <T> Optional<T> getTag(IProposalTag key) {
        return Optional.fromNullable((T) tags.get(key));
    }

    @Override
    public <T> Optional<T> getTag(String key) {
        return Proposals.getTag(this, key);
    }

    @Override
    public <T> T getTag(IProposalTag key, T defaultValue) {
        T res = (T) tags.get(key);
        return res != null ? res : defaultValue;
    }

    @Override
    public <T> T getTag(String key, T defaultValue) {
        return this.<T>getTag(key).or(defaultValue);
    }

    @Override
    public ImmutableSet<IProposalTag> tags() {
        return ImmutableSet.copyOf(tags.keySet());
    }
}
