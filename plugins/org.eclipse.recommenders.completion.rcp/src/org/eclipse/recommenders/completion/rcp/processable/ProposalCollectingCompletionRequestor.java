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
package org.eclipse.recommenders.completion.rcp.processable;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang3.ArrayUtils.subarray;
import static org.eclipse.jdt.core.CompletionProposal.*;
import static org.eclipse.recommenders.utils.Checks.cast;

import java.util.Map;

import org.eclipse.jdt.core.CompletionContext;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.codeassist.InternalCompletionContext;
import org.eclipse.jdt.internal.ui.text.java.FillArgumentNamesCompletionProposalCollector;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.text.java.CompletionProposalCollector;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.swt.graphics.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;

@SuppressWarnings("restriction")
public class ProposalCollectingCompletionRequestor extends CompletionRequestor {

    private Logger log = LoggerFactory.getLogger(getClass());
    private final Map<IJavaCompletionProposal, CompletionProposal> proposals = Maps.newIdentityHashMap();
    private JavaContentAssistInvocationContext jdtuiContext;
    private CompletionProposalCollector collector;
    private InternalCompletionContext compilerContext;

    public ProposalCollectingCompletionRequestor(final JavaContentAssistInvocationContext ctx) {
        this(ctx, false, false);
    }

    public ProposalCollectingCompletionRequestor(final JavaContentAssistInvocationContext ctx,
            boolean ignoreConstructors, boolean ignoreTypes) {
        super(false);
        checkNotNull(ctx);
        jdtuiContext = ctx;
        initalizeCollector(ignoreConstructors, ignoreTypes);
    }

    private void initalizeCollector(boolean ignoreConstructors, boolean ignoreTypes) {
        if (shouldFillArgumentNames()) {
            collector = new FillArgumentNamesCompletionProposalCollector(jdtuiContext);
        } else {
            collector = new CompletionProposalCollector(jdtuiContext.getCompilationUnit(), false);
        }
        configureInterestedProposalTypes(ignoreConstructors, ignoreTypes);
        adjustProposalReplacementLength();
    }

    /**
     * Configures the delegate collector by calling a series of setters.
     *
     * Important: For this to work, this {@code CompletionRequestor} must then delegate all corresponding getters to
     * {@code collector}.
     */
    private void configureInterestedProposalTypes(boolean ignoreConstructors, boolean ignoreTypes) {
        collector.setIgnored(ANNOTATION_ATTRIBUTE_REF, false);
        collector.setIgnored(ANONYMOUS_CLASS_DECLARATION, ignoreTypes);
        collector.setIgnored(ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION, ignoreConstructors);
        collector.setIgnored(FIELD_REF, false);
        collector.setIgnored(FIELD_REF_WITH_CASTED_RECEIVER, false);
        collector.setIgnored(KEYWORD, false);
        collector.setIgnored(LABEL_REF, false);
        collector.setIgnored(LOCAL_VARIABLE_REF, false);
        collector.setIgnored(METHOD_DECLARATION, false);
        collector.setIgnored(METHOD_NAME_REFERENCE, false);
        collector.setIgnored(METHOD_REF, false);
        collector.setIgnored(CONSTRUCTOR_INVOCATION, ignoreConstructors);
        collector.setIgnored(METHOD_REF_WITH_CASTED_RECEIVER, false);
        collector.setIgnored(PACKAGE_REF, false);
        collector.setIgnored(POTENTIAL_METHOD_DECLARATION, false);
        collector.setIgnored(VARIABLE_DECLARATION, false);
        collector.setIgnored(TYPE_REF, ignoreTypes);
        collector.setIgnored(JAVADOC_BLOCK_TAG, false);
        collector.setIgnored(JAVADOC_FIELD_REF, false);
        collector.setIgnored(JAVADOC_INLINE_TAG, false);
        collector.setIgnored(JAVADOC_METHOD_REF, false);
        collector.setIgnored(JAVADOC_PARAM_REF, false);
        collector.setIgnored(JAVADOC_TYPE_REF, false);
        collector.setIgnored(JAVADOC_VALUE_REF, false);

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

    @Override
    public boolean isIgnored(final int completionProposalKind) {
        return collector.isIgnored(completionProposalKind);
    }

    @Override
    public boolean isAllowingRequiredProposals(final int proposalKind, final int requiredProposalKind) {
        return collector.isAllowingRequiredProposals(proposalKind, requiredProposalKind);
    }

    @Override
    public boolean isExtendedContextRequired() {
        return collector.isExtendedContextRequired();
    }

    @Override
    public String[] getFavoriteReferences() {
        return collector.getFavoriteReferences();
    }

    private void adjustProposalReplacementLength() {
        ITextViewer viewer = jdtuiContext.getViewer();
        Point selection = viewer.getSelectedRange();
        if (selection.y > 0) {
            collector.setReplacementLength(selection.y);
        }
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
    public void acceptContext(final CompletionContext context) {
        compilerContext = cast(context);
        collector.acceptContext(context);
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

    @Override
    public void completionFailure(IProblem problem) {
        log.debug(problem.toString());
    }
}
