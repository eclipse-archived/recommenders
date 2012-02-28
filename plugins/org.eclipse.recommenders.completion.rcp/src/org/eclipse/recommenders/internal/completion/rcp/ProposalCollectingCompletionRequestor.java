/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.completion.rcp;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang3.ArrayUtils.subarray;
import static org.eclipse.jdt.core.CompletionProposal.ANNOTATION_ATTRIBUTE_REF;
import static org.eclipse.jdt.core.CompletionProposal.ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION;
import static org.eclipse.jdt.core.CompletionProposal.ANONYMOUS_CLASS_DECLARATION;
import static org.eclipse.jdt.core.CompletionProposal.CONSTRUCTOR_INVOCATION;
import static org.eclipse.jdt.core.CompletionProposal.FIELD_IMPORT;
import static org.eclipse.jdt.core.CompletionProposal.FIELD_REF;
import static org.eclipse.jdt.core.CompletionProposal.FIELD_REF_WITH_CASTED_RECEIVER;
import static org.eclipse.jdt.core.CompletionProposal.KEYWORD;
import static org.eclipse.jdt.core.CompletionProposal.LABEL_REF;
import static org.eclipse.jdt.core.CompletionProposal.LOCAL_VARIABLE_REF;
import static org.eclipse.jdt.core.CompletionProposal.METHOD_DECLARATION;
import static org.eclipse.jdt.core.CompletionProposal.METHOD_IMPORT;
import static org.eclipse.jdt.core.CompletionProposal.METHOD_NAME_REFERENCE;
import static org.eclipse.jdt.core.CompletionProposal.METHOD_REF;
import static org.eclipse.jdt.core.CompletionProposal.METHOD_REF_WITH_CASTED_RECEIVER;
import static org.eclipse.jdt.core.CompletionProposal.PACKAGE_REF;
import static org.eclipse.jdt.core.CompletionProposal.POTENTIAL_METHOD_DECLARATION;
import static org.eclipse.jdt.core.CompletionProposal.TYPE_IMPORT;
import static org.eclipse.jdt.core.CompletionProposal.TYPE_REF;
import static org.eclipse.jdt.core.CompletionProposal.VARIABLE_DECLARATION;
import static org.eclipse.recommenders.utils.Checks.cast;

import java.util.Map;

import org.eclipse.jdt.core.CompletionContext;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.internal.codeassist.InternalCompletionContext;
import org.eclipse.jdt.internal.ui.text.java.FillArgumentNamesCompletionProposalCollector;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.text.java.CompletionProposalCollector;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;

@SuppressWarnings("restriction")
public class ProposalCollectingCompletionRequestor extends CompletionRequestor {

    private final Map<IJavaCompletionProposal, CompletionProposal> proposals = Maps.newIdentityHashMap();
    private final JavaContentAssistInvocationContext jdtuiContext;
    private CompletionProposalCollector collector;
    private InternalCompletionContext compilerContext;

    public ProposalCollectingCompletionRequestor(final JavaContentAssistInvocationContext ctx) {
        super(false);
        checkNotNull(ctx);
        this.jdtuiContext = ctx;
        initalizeCollector();
    }

    private void initalizeCollector() {
        if (shouldFillArgumentNames()) {
            collector = new FillArgumentNamesCompletionProposalCollector(jdtuiContext);
        } else {
            collector = new CompletionProposalCollector(jdtuiContext.getCompilationUnit(), false);
        }
        collector.setIgnored(ANNOTATION_ATTRIBUTE_REF, false);
        collector.setIgnored(ANONYMOUS_CLASS_DECLARATION, false);
        collector.setIgnored(ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION, false);
        collector.setIgnored(FIELD_REF, false);
        collector.setIgnored(FIELD_REF_WITH_CASTED_RECEIVER, false);
        collector.setIgnored(KEYWORD, false);
        collector.setIgnored(LABEL_REF, false);
        collector.setIgnored(LOCAL_VARIABLE_REF, false);
        collector.setIgnored(METHOD_DECLARATION, false);
        collector.setIgnored(METHOD_NAME_REFERENCE, false);
        collector.setIgnored(METHOD_REF, false);
        collector.setIgnored(CONSTRUCTOR_INVOCATION, false);
        collector.setIgnored(METHOD_REF_WITH_CASTED_RECEIVER, false);
        collector.setIgnored(PACKAGE_REF, false);
        collector.setIgnored(POTENTIAL_METHOD_DECLARATION, false);
        collector.setIgnored(VARIABLE_DECLARATION, false);
        collector.setIgnored(TYPE_REF, false);

        collector.setAllowsRequiredProposals(FIELD_REF, TYPE_REF, true);
        collector.setAllowsRequiredProposals(FIELD_REF, TYPE_IMPORT, true);
        collector.setAllowsRequiredProposals(FIELD_REF, FIELD_IMPORT, true);
        collector.setAllowsRequiredProposals(METHOD_REF, TYPE_REF, true);
        collector.setAllowsRequiredProposals(METHOD_REF, TYPE_IMPORT, true);
        collector.setAllowsRequiredProposals(METHOD_REF, METHOD_IMPORT, true);
        collector.setAllowsRequiredProposals(CONSTRUCTOR_INVOCATION, TYPE_REF, true);
        collector.setAllowsRequiredProposals(ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION, TYPE_REF, true);
        collector.setAllowsRequiredProposals(ANONYMOUS_CLASS_DECLARATION, TYPE_REF, true);
        collector.setAllowsRequiredProposals(TYPE_REF, TYPE_REF, true);

        collector.setFavoriteReferences(getFavoriteStaticMembers());
        collector.setRequireExtendedContext(true);
    }

    @VisibleForTesting
    protected boolean shouldFillArgumentNames() {
        try {
            // when running a test suite this throws a NPE
            return PreferenceConstants.getPreferenceStore().getBoolean(
                    PreferenceConstants.CODEASSIST_FILL_ARGUMENT_NAMES);
        } catch (final Exception e) {
            return true;
        }
    }

    @Override
    public boolean isAllowingRequiredProposals(final int proposalKind, final int requiredProposalKind) {
        return collector.isAllowingRequiredProposals(proposalKind, requiredProposalKind);
    };

    @Override
    public boolean isIgnored(final int completionProposalKind) {
        return collector.isIgnored(completionProposalKind);
    };

    @Override
    public void acceptContext(final CompletionContext context) {
        this.compilerContext = cast(context);
        collector.acceptContext(context);
    }

    @Override
    public boolean isExtendedContextRequired() {
        return collector.isExtendedContextRequired();
    }

    private String[] getFavoriteStaticMembers() {
        final String serializedFavorites = PreferenceConstants.getPreferenceStore().getString(
                PreferenceConstants.CODEASSIST_FAVORITE_STATIC_MEMBERS);
        if (serializedFavorites != null && serializedFavorites.length() > 0) {
            return serializedFavorites.split(";"); //$NON-NLS-1$
        }
        return new String[0];
    }

    @Override
    public void accept(final CompletionProposal compilerProposal) {
        for (final IJavaCompletionProposal uiProposal : createJdtProposals(compilerProposal)) {
            proposals.put(uiProposal, compilerProposal);
        }
    }

    private IJavaCompletionProposal[] createJdtProposals(final CompletionProposal proposal) {
        final int oldLength = collector.getJavaCompletionProposals().length;
        collector.accept(proposal);
        // order matters ;)
        final IJavaCompletionProposal[] jdtProposals = collector.getJavaCompletionProposals();
        final IJavaCompletionProposal[] newProposals = subarray(jdtProposals, oldLength, jdtProposals.length);
        return newProposals;
    }

    public void setReplacementLength(final int y) {
        collector.setReplacementLength(y);
    }

    public InternalCompletionContext getCoreContext() {
        return compilerContext;
    }

    public Map<IJavaCompletionProposal, CompletionProposal> getProposals() {
        return proposals;
    }
}