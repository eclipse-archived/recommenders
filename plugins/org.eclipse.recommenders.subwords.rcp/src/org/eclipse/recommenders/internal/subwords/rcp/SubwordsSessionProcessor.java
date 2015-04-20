/**
 * Copyright (c) 2014 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.subwords.rcp;

import static java.lang.Math.min;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;
import static org.eclipse.recommenders.completion.rcp.CompletionContextKey.JAVA_PROPOSALS;
import static org.eclipse.recommenders.completion.rcp.processable.ProposalTag.*;
import static org.eclipse.recommenders.internal.subwords.rcp.LCSS.containsSubsequence;
import static org.eclipse.recommenders.internal.subwords.rcp.LogMessages.*;
import static org.eclipse.recommenders.utils.Logs.log;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;

import javax.inject.Inject;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.codeassist.InternalCompletionContext;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.internal.ui.text.java.LazyJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.recommenders.completion.rcp.CompletionContextKey;
import org.eclipse.recommenders.completion.rcp.CompletionContexts;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;
import org.eclipse.recommenders.completion.rcp.processable.IProcessableProposal;
import org.eclipse.recommenders.completion.rcp.processable.NoProposalCollectingCompletionRequestor;
import org.eclipse.recommenders.completion.rcp.processable.ProposalCollectingCompletionRequestor;
import org.eclipse.recommenders.completion.rcp.processable.ProposalProcessor;
import org.eclipse.recommenders.completion.rcp.processable.SessionProcessor;
import org.eclipse.recommenders.rcp.utils.TimeDelimitedProgressMonitor;
import org.eclipse.recommenders.utils.Checks;
import org.eclipse.recommenders.utils.Logs;
import org.eclipse.recommenders.utils.Reflections;
import org.eclipse.ui.IEditorPart;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;

@SuppressWarnings("restriction")
public class SubwordsSessionProcessor extends SessionProcessor {

    private static final long COMPLETION_TIME_OUT = SECONDS.toMillis(5);

    // Negative value ensures subsequence matches have a lower relevance than standard JDT or template proposals
    private static final int SUBWORDS_RANGE_START = -10000;

    private static final int[] EMPTY_SEQUENCE = new int[0];

    private static Field CORE_CONTEXT = Reflections.getDeclaredField(JavaContentAssistInvocationContext.class,
            "fCoreContext").orNull(); //$NON-NLS-1$
    private static Field CU = Reflections.getDeclaredField(JavaContentAssistInvocationContext.class, "fCU").orNull(); //$NON-NLS-1$
    private static Field CU_COMPUTED = Reflections.getDeclaredField(JavaContentAssistInvocationContext.class,
            "fCUComputed").orNull(); //$NON-NLS-1$

    private final SubwordsRcpPreferences prefs;

    @Inject
    public SubwordsSessionProcessor(SubwordsRcpPreferences prefs) {
        this.prefs = prefs;
    }

    @Override
    public void initializeContext(IRecommendersCompletionContext recContext) {
        try {
            JavaContentAssistInvocationContext jdtContext = recContext.getJavaContext();
            ICompilationUnit cu = jdtContext.getCompilationUnit();
            int offset = jdtContext.getInvocationOffset();

            // Tricky: we bypass the normal code completion request (triggered at the actual cursor position) by
            // replacing all required keys manually and triggering content assist where we need it:

            // TODO maybe we can get rid of that call by simply using the 'right' collector for the first time? This
            // would save ~5 ms I guess.
            NoProposalCollectingCompletionRequestor collector = new NoProposalCollectingCompletionRequestor();
            cu.codeComplete(offset, collector, new TimeDelimitedProgressMonitor(COMPLETION_TIME_OUT));
            InternalCompletionContext compContext = collector.getCoreContext();
            if (compContext == null) {
                Logs.log(LogMessages.ERROR_COMPLETION_CONTEXT_NOT_COLLECTED, cu.getPath());
                return;
            }

            CORE_CONTEXT.set(jdtContext, compContext);
            recContext.set(CompletionContextKey.INTERNAL_COMPLETIONCONTEXT, compContext);

            String prefix = getPrefix(jdtContext);
            int length = prefix.length();
            recContext.set(CompletionContextKey.COMPLETION_PREFIX, prefix);

            Map<IJavaCompletionProposal, CompletionProposal> baseProposals = Maps.newHashMap();

            recContext.set(JAVA_PROPOSALS, baseProposals);

            ASTNode completionNode = compContext.getCompletionNode();
            ASTNode completionNodeParent = compContext.getCompletionNodeParent();
            SortedSet<Integer> triggerlocations = computeTriggerLocations(offset, completionNode, completionNodeParent,
                    length);

            Set<String> sortkeys = Sets.newHashSet();
            for (int trigger : triggerlocations) {
                Map<IJavaCompletionProposal, CompletionProposal> newProposals = getNewProposals(jdtContext, trigger);
                testAndInsertNewProposals(recContext, baseProposals, sortkeys, newProposals);
            }

        } catch (Exception e) {
            Logs.log(LogMessages.EXCEPTION_DURING_CODE_COMPLETION, e);
        }
    }

    private SortedSet<Integer> computeTriggerLocations(int offset, ASTNode completionNode,
            ASTNode completionNodeParent, int length) {
        // It is important to trigger at higher locations first, as the base relevance assigned to a proposal by the JDT
        // may depend on the prefix. Proposals which are made for both an empty prefix and a non-empty prefix are thus
        // assigned a base relevance that is as close as possible to that the JDT would assign without subwords
        // completion enabled.
        // TODO MB Need an example.
        SortedSet<Integer> triggerlocations = Sets.newTreeSet(Ordering.natural().reverse());
        int emptyPrefix = offset - length;

        // Trigger first with either the specified prefix or the specified minimum prefix length. Note that this is only
        // effective for type and constructor completions, but this situation cannot be detected reliably.
        int triggerOffset = min(prefs.minPrefixLengthForTypes, length);
        triggerlocations.add(emptyPrefix + triggerOffset);

        // Always trigger with empty prefix to get all members at the current location:
        triggerlocations.add(emptyPrefix);

        return triggerlocations;
    }

    private String getPrefix(JavaContentAssistInvocationContext jdtContext) throws BadLocationException {
        CharSequence prefix = jdtContext.computeIdentifierPrefix();
        return prefix == null ? "" : prefix.toString(); //$NON-NLS-1$
    }

    private Map<IJavaCompletionProposal, CompletionProposal> getNewProposals(
            JavaContentAssistInvocationContext originalContext, int triggerOffset) {
        if (triggerOffset < 0) {
            // XXX not sure when this happens but is has happened in the past
            return Maps.<IJavaCompletionProposal, CompletionProposal>newHashMap();
        }
        ICompilationUnit cu = originalContext.getCompilationUnit();
        ITextViewer viewer = originalContext.getViewer();
        IEditorPart editor = lookupEditor(cu);
        JavaContentAssistInvocationContext newJdtContext = new JavaContentAssistInvocationContext(viewer,
                triggerOffset, editor);
        setCompilationUnit(newJdtContext, cu);
        ProposalCollectingCompletionRequestor collector = computeProposals(cu, newJdtContext, triggerOffset);
        Map<IJavaCompletionProposal, CompletionProposal> proposals = collector.getProposals();
        return proposals != null ? proposals : Maps.<IJavaCompletionProposal, CompletionProposal>newHashMap();
    }

    private void setCompilationUnit(JavaContentAssistInvocationContext newJdtContext, ICompilationUnit cu) {
        if (Checks.anyIsNull(CU, CU_COMPUTED)) {
            return;
        }
        try {
            CU.set(newJdtContext, cu);
            CU_COMPUTED.set(newJdtContext, true);
        } catch (Exception e) {
            Logs.log(EXCEPTION_DURING_CODE_COMPLETION, e);
        }
    }

    private void testAndInsertNewProposals(IRecommendersCompletionContext crContext,
            Map<IJavaCompletionProposal, CompletionProposal> baseProposals, Set<String> sortkeys,
            final Map<IJavaCompletionProposal, CompletionProposal> newProposals) {
        for (Entry<IJavaCompletionProposal, CompletionProposal> entry : newProposals.entrySet()) {
            IJavaCompletionProposal javaProposal = entry.getKey();
            CompletionProposal coreProposal = entry.getValue();

            // we need a completion string (close to a display string) that allows to spot duplicated proposals
            // key point: we don't want to use the display string of lazy completion proposals for performance reasons.
            String completionIdentifier = computeCompletionIdentifier(javaProposal, coreProposal);
            String completion = CompletionContexts.getPrefixMatchingArea(completionIdentifier);

            if (!sortkeys.contains(completionIdentifier) && containsSubsequence(completion, crContext.getPrefix())) {
                baseProposals.put(javaProposal, coreProposal);
                sortkeys.add(completionIdentifier);
            }
        }
    }

    private String computeCompletionIdentifier(IJavaCompletionProposal javaProposal, CompletionProposal coreProposal) {
        String completionIdentifier;
        if (javaProposal instanceof LazyJavaCompletionProposal && coreProposal != null) {
            switch (coreProposal.getKind()) {
            case CompletionProposal.CONSTRUCTOR_INVOCATION:
                // result: ClassSimpleName(Lsome/Param;I)V
                completionIdentifier = new StringBuilder().append(coreProposal.getName())
                        .append(coreProposal.getSignature()).toString();
                break;
            case CompletionProposal.TYPE_REF:
                // result: ClassSimpleName fully.qualified.ClassSimpleName
                char[] signature = coreProposal.getSignature();
                char[] simpleName = Signature.getSignatureSimpleName(signature);
                completionIdentifier = new StringBuilder().append(simpleName).append(' ').append(signature).toString();
                break;
            case CompletionProposal.PACKAGE_REF:
                // result: org.eclipse.my.package
                completionIdentifier = new String(coreProposal.getDeclarationSignature());
                break;
            case CompletionProposal.METHOD_REF:
                // result: myMethodName(Lsome/Param;I)V
                completionIdentifier = new StringBuilder().append(coreProposal.getName())
                        .append(coreProposal.getSignature()).toString();
                break;
            default:
                // result: display string. This should not happen. We should issue a warning here...
                completionIdentifier = javaProposal.getDisplayString();
                Logs.log(ERROR_UNEXPECTED_FALL_THROUGH, javaProposal.getClass());
                break;
            }
        } else {
            completionIdentifier = javaProposal.getDisplayString();
        }
        return completionIdentifier;
    }

    @Override
    public boolean startSession(IRecommendersCompletionContext crContext) {
        return true;
    }

    private ProposalCollectingCompletionRequestor computeProposals(ICompilationUnit cu,
            JavaContentAssistInvocationContext coreContext, int offset) {
        ProposalCollectingCompletionRequestor collector = new ProposalCollectingCompletionRequestor(coreContext);
        try {
            cu.codeComplete(offset, collector, new TimeDelimitedProgressMonitor(COMPLETION_TIME_OUT));
        } catch (final Exception e) {
            log(EXCEPTION_DURING_CODE_COMPLETION, e);
        }
        return collector;
    }

    @VisibleForTesting
    protected IEditorPart lookupEditor(ICompilationUnit cu) {
        return EditorUtility.isOpenInEditor(cu);
    }

    @Override
    public void process(final IProcessableProposal proposal) {

        String completionIdentifier = computeCompletionIdentifier(proposal, proposal.getCoreProposal().orNull());
        final String matchingArea = CompletionContexts.getPrefixMatchingArea(completionIdentifier);

        proposal.getProposalProcessorManager().addProcessor(new ProposalProcessor() {

            int[] bestSequence = EMPTY_SEQUENCE;

            String prefix;

            @Override
            public boolean isPrefix(String prefix) {
                this.prefix = prefix;
                bestSequence = LCSS.bestSubsequence(matchingArea, prefix);
                return prefix.isEmpty() || bestSequence.length > 0;
            }

            @Override
            public void modifyDisplayString(StyledString displayString) {
                for (int index : bestSequence) {
                    displayString.setStyle(index, 1, StyledString.COUNTER_STYLER);
                }
            }

            @Override
            public int modifyRelevance() {
                if (ArrayUtils.isEmpty(bestSequence)) {
                    proposal.setTag(IS_PREFIX_MATCH, true);
                    return 0;
                } else if (startsWithIgnoreCase(matchingArea, prefix)) {
                    proposal.setTag(SUBWORDS_SCORE, null);
                    proposal.setTag(IS_PREFIX_MATCH, true);
                    return 0;
                } else if (CharOperation.camelCaseMatch(prefix.toCharArray(), matchingArea.toCharArray())) {
                    proposal.setTag(IS_PREFIX_MATCH, false);
                    proposal.setTag(IS_CAMEL_CASE_MATCH, true);
                    return 0;
                } else {
                    int score = LCSS.scoreSubsequence(bestSequence);
                    proposal.setTag(IS_PREFIX_MATCH, false);
                    proposal.setTag(SUBWORDS_SCORE, score);
                    return score + SUBWORDS_RANGE_START;
                }
            }
        });
    }
}
