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

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.ArrayUtils.subarray;
import static org.eclipse.jdt.core.CompletionProposal.*;
import static org.eclipse.recommenders.internal.completion.rcp.l10n.LogMessages.ERROR_EXCEPTION_DURING_CODE_COMPLETION;
import static org.eclipse.recommenders.utils.Checks.cast;
import static org.eclipse.recommenders.utils.Logs.log;

import java.lang.reflect.Field;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
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
import org.eclipse.recommenders.internal.completion.rcp.Constants;
import org.eclipse.recommenders.internal.completion.rcp.l10n.LogMessages;
import org.eclipse.recommenders.utils.Logs;
import org.eclipse.recommenders.utils.Reflections;
import org.eclipse.swt.graphics.Point;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Iterables;

@SuppressWarnings("restriction")
public class ProposalCollectingCompletionRequestor extends CompletionRequestor {

    private static final Field F_PROPOSALS = Reflections
            .getDeclaredField(true, CompletionProposalCollector.class, "fJavaProposals").orNull(); //$NON-NLS-1$

    private final Map<IJavaCompletionProposal, CompletionProposal> proposals = new IdentityHashMap<>();

    private JavaContentAssistInvocationContext jdtuiContext;
    private CompletionProposalCollector collector;
    private InternalCompletionContext compilerContext;

    public ProposalCollectingCompletionRequestor(final JavaContentAssistInvocationContext ctx) {
        super(false);
        jdtuiContext = requireNonNull(ctx);
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
        String[] excludes = PreferenceConstants.getExcludedCompletionProposalCategories();
        if (doesJdtProposeTypesOnly(excludes)) {
            setIgnoreNonTypes(true);
        } else {
            setIgnoreNonTypes(false);
        }

        if (doesJdtProposeNonTypesOnly(excludes)) {
            setIgnoreTypes(true);
        } else {
            setIgnoreTypes(false);
        }

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

    private boolean doesJdtProposeTypesOnly(String[] excludes) {
        return !ArrayUtils.contains(excludes, Constants.JDT_TYPE_CATEGORY)
                && ArrayUtils.contains(excludes, Constants.JDT_ALL_CATEGORY)
                && ArrayUtils.contains(excludes, Constants.JDT_NON_TYPE_CATEGORY);
    }

    private boolean doesJdtProposeNonTypesOnly(String[] excludes) {
        return !ArrayUtils.contains(excludes, Constants.JDT_NON_TYPE_CATEGORY)
                && ArrayUtils.contains(excludes, Constants.JDT_ALL_CATEGORY)
                && ArrayUtils.contains(excludes, Constants.JDT_TYPE_CATEGORY);
    }

    private void setIgnoreNonTypes(boolean ignored) {
        collector.setIgnored(ANNOTATION_ATTRIBUTE_REF, ignored);
        collector.setIgnored(ANONYMOUS_CLASS_DECLARATION, ignored);
        collector.setIgnored(ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION, ignored);
        collector.setIgnored(FIELD_REF, ignored);
        collector.setIgnored(FIELD_REF_WITH_CASTED_RECEIVER, ignored);
        collector.setIgnored(KEYWORD, ignored);
        collector.setIgnored(LABEL_REF, ignored);
        collector.setIgnored(LOCAL_VARIABLE_REF, ignored);
        collector.setIgnored(METHOD_DECLARATION, ignored);
        collector.setIgnored(METHOD_NAME_REFERENCE, ignored);
        collector.setIgnored(METHOD_REF, ignored);
        collector.setIgnored(CONSTRUCTOR_INVOCATION, ignored);
        collector.setIgnored(METHOD_REF_WITH_CASTED_RECEIVER, ignored);
        collector.setIgnored(PACKAGE_REF, ignored);
        collector.setIgnored(POTENTIAL_METHOD_DECLARATION, ignored);
        collector.setIgnored(VARIABLE_DECLARATION, ignored);
    }

    private void setIgnoreTypes(boolean ignored) {
        collector.setIgnored(TYPE_REF, ignored);
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
        if (Constants.DEBUG) {
            Logs.log(LogMessages.ERROR_COMPLETION_FAILURE_DURING_DEBUG_MODE, problem.toString());
        }
    }
}
