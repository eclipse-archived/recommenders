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
import static org.eclipse.recommenders.internal.completion.rcp.l10n.LogMessages.ERROR_EXCEPTION_DURING_CODE_COMPLETION;
import static org.eclipse.recommenders.utils.Checks.cast;
import static org.eclipse.recommenders.utils.Logs.log;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.CompletionContext;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.codeassist.InternalCompletionContext;
import org.eclipse.jdt.internal.ui.text.java.FillArgumentNamesCompletionProposalCollector;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.text.java.CompletionProposalCollector;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.recommenders.utils.Reflections;
import org.eclipse.swt.graphics.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

@SuppressWarnings("restriction")
public class ProposalCollectingCompletionRequestor extends CompletionRequestor {

    private static final Field F_PROPOSALS = Reflections
            .getDeclaredField(CompletionProposalCollector.class, "fJavaProposals").orNull(); //$NON-NLS-1$
    private Logger log = LoggerFactory.getLogger(getClass());
    private final Map<IJavaCompletionProposal, CompletionProposal> proposals = Maps.newIdentityHashMap();
    private JavaContentAssistInvocationContext jdtuiContext;
    private CompletionProposalCollector collector;
    private InternalCompletionContext compilerContext;

    public ProposalCollectingCompletionRequestor(final JavaContentAssistInvocationContext ctx) {
        super(false);
        checkNotNull(ctx);
        jdtuiContext = ctx;
        initalizeCollector();
    }

    private void initalizeCollector() {
        if (shouldFillArgumentNames()) {
            collector = new FillArgumentNamesCompletionProposalCollector(jdtuiContext);
        } else {
            collector = new CompletionProposalCollector(jdtuiContext.getCompilationUnit(), false);
        }
        configureInterestedProposalTypes();
        adjustProposalReplacementLength();
    }

    /**
     * Configures the delegate collector by calling a series of setters.
     *
     * Important: For this to work, this {@code CompletionRequestor} must then delegate all corresponding getters to
     * {@code collector}.
     */
    private void configureInterestedProposalTypes() {
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
            return PreferenceConstants.getPreferenceStore()
                    .getBoolean(PreferenceConstants.CODEASSIST_FILL_ARGUMENT_NAMES);
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
        final String serializedFavorites = PreferenceConstants.getPreferenceStore()
                .getString(PreferenceConstants.CODEASSIST_FAVORITE_STATIC_MEMBERS);
        if (serializedFavorites != null && serializedFavorites.length() > 0) {
            return serializedFavorites.split(";"); //$NON-NLS-1$
        }
        return CharOperation.NO_STRINGS;
    }

    @Override
    public void accept(final CompletionProposal compilerProposal) {
        for (final IJavaCompletionProposal uiProposal : createJdtProposals(compilerProposal)) {
            proposals.put(uiProposal, compilerProposal);
        }
    }

    private IJavaCompletionProposal[] createJdtProposals(final CompletionProposal proposal) {
        if (F_PROPOSALS != null) {
            try {
                @SuppressWarnings("unchecked")
                List<IJavaCompletionProposal> list = (List<IJavaCompletionProposal>) F_PROPOSALS.get(collector);
                // call order (size, accept, size, get) matters.
                // First get the old amount of proposals. than add the new one. Then check how many new proposals
                // are actually added (it may be more than one). These new proposals are then returned:
                int oldSize = list.size();
                collector.accept(proposal);
                int newSize = list.size();
                List<IJavaCompletionProposal> res = list.subList(oldSize, newSize);
                return Iterables.toArray(res, IJavaCompletionProposal.class);
            } catch (Exception e) {
                // log and use the fallback mechanism
                log(ERROR_EXCEPTION_DURING_CODE_COMPLETION, e);
            }
        }
        // fallback if the above code fails (that's the old code). We may remove this later if we now it works reliably.
        // Error reporting will tell us.
        final int oldSize = collector.getJavaCompletionProposals().length;
        collector.accept(proposal);
        final IJavaCompletionProposal[] jdtProposals = collector.getJavaCompletionProposals();
        return subarray(jdtProposals, oldSize, jdtProposals.length);
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
