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

import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;
import static org.eclipse.recommenders.completion.rcp.CompletionContextKey.JAVA_PROPOSALS;
import static org.eclipse.recommenders.completion.rcp.processable.ProposalTag.*;
import static org.eclipse.recommenders.internal.subwords.rcp.LCSS.containsSubsequence;
import static org.eclipse.recommenders.internal.subwords.rcp.LogMessages.EXCEPTION_DURING_CODE_COMPLETION;
import static org.eclipse.recommenders.utils.Logs.log;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.codeassist.InternalCompletionContext;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
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
import org.eclipse.recommenders.utils.Logs;
import org.eclipse.recommenders.utils.Reflections;
import org.eclipse.ui.IEditorPart;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

@SuppressWarnings("restriction")
public class SubwordsSessionProcessor extends SessionProcessor {

    // Negative value ensures subsequence matches have a lower relevance than standard JDT or template proposals
    private static final int SUBWORDS_RANGE_START = -10000;

    private static Field CORE_CONTEXT = Reflections.getDeclaredField(JavaContentAssistInvocationContext.class,
            "fCoreContext").orNull();

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
            NoProposalCollectingCompletionRequestor collector = new NoProposalCollectingCompletionRequestor();
            cu.codeComplete(offset, collector, new TimeDelimitedProgressMonitor(5000));

            InternalCompletionContext compContext = collector.getCoreContext();
            CORE_CONTEXT.set(jdtContext, compContext);
            recContext.set(CompletionContextKey.INTERNAL_COMPLETIONCONTEXT, compContext);

            String prefix = getPrefix(jdtContext);
            int length = prefix.length();
            recContext.set(CompletionContextKey.COMPLETION_PREFIX, prefix);

            Map<IJavaCompletionProposal, CompletionProposal> baseProposals = Maps.newHashMap();

            recContext.set(JAVA_PROPOSALS, baseProposals);

            ASTNode completionNode = compContext.getCompletionNode();
            ASTNode completionNodeParent = compContext.getCompletionNodeParent();
            TreeSet<Integer> triggerlocations = computeTriggerLocations(offset, completionNode, completionNodeParent,
                    length);

            ITextViewer viewer = jdtContext.getViewer();
            IEditorPart editor = lookupEditor(cu);
            Set<String> sortkeys = Sets.newHashSet();
            for (int trigger : triggerlocations) {
                Map<IJavaCompletionProposal, CompletionProposal> newProposals = getNewProposals(viewer, editor, trigger);
                testAndInsertNewProposals(recContext, baseProposals, sortkeys, newProposals);
            }

        } catch (Exception e) {
            Logs.log(EXCEPTION_DURING_CODE_COMPLETION, e);
        }
    }

    private TreeSet<Integer> computeTriggerLocations(int offset, ASTNode completionNode, ASTNode completionNodeParent,
            int length) {
        TreeSet<Integer> triggerlocations = Sets.newTreeSet();
        int emptyPrefix = offset - length;
        triggerlocations.add(emptyPrefix);
        if (length == 0) {
            triggerlocations.add(emptyPrefix);
            return triggerlocations;
        }
        triggerlocations.add(emptyPrefix);
        triggerlocations.add(emptyPrefix + 1);
        return triggerlocations;
    }

    private String getPrefix(JavaContentAssistInvocationContext jdtContext) throws BadLocationException {
        CharSequence prefix = jdtContext.computeIdentifierPrefix();
        return prefix == null ? "" : prefix.toString();
    }

    private Map<IJavaCompletionProposal, CompletionProposal> getNewProposals(ITextViewer viewer, IEditorPart editor,
            int triggerOffset) {
        if (triggerOffset < 0) {
            // XXX not sure when this happens but is has happened in the past
            return Maps.<IJavaCompletionProposal, CompletionProposal>newHashMap();
        }
        JavaContentAssistInvocationContext newjdtContext = new JavaContentAssistInvocationContext(viewer,
                triggerOffset, editor);
        ICompilationUnit cu = newjdtContext.getCompilationUnit();
        ProposalCollectingCompletionRequestor collector = computeProposals(cu, newjdtContext, triggerOffset);
        Map<IJavaCompletionProposal, CompletionProposal> proposals = collector.getProposals();
        return proposals != null ? proposals : Maps.<IJavaCompletionProposal, CompletionProposal>newHashMap();
    }

    private void testAndInsertNewProposals(IRecommendersCompletionContext crContext,
            Map<IJavaCompletionProposal, CompletionProposal> baseProposals, Set<String> sortkeys,
            final Map<IJavaCompletionProposal, CompletionProposal> newProposals) {
        for (IJavaCompletionProposal p : newProposals.keySet()) {
            String displayString = p.getDisplayString();
            String completion = CompletionContexts.getPrefixMatchingArea(displayString);
            if (!sortkeys.contains(displayString) && containsSubsequence(completion, crContext.getPrefix())) {
                baseProposals.put(p, newProposals.get(p));
                sortkeys.add(p.getDisplayString());
            }
        }
    }

    @Override
    public boolean startSession(IRecommendersCompletionContext crContext) {
        return true;
    }

    private ProposalCollectingCompletionRequestor computeProposals(ICompilationUnit cu,
            JavaContentAssistInvocationContext coreContext, int offset) {
        ProposalCollectingCompletionRequestor collector = new ProposalCollectingCompletionRequestor(coreContext,
                !prefs.computeAdditionalConstructorProposals, !prefs.computeAdditionalTypeProposals);
        try {
            cu.codeComplete(offset, collector, new TimeDelimitedProgressMonitor(5000));
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
        proposal.getProposalProcessorManager().addProcessor(new ProposalProcessor() {

            int[] bestSequence = new int[0];
            String matchingArea = CompletionContexts.getPrefixMatchingArea(proposal.getDisplayString());
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
                    proposal.setTag(SUBWORDS_SCORE, null);
                    proposal.setTag(IS_PREFIX_MATCH, true);
                    return 0;
                }
                if (startsWithIgnoreCase(matchingArea, prefix)) {
                    proposal.setTag(IS_PREFIX_MATCH, true);
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
