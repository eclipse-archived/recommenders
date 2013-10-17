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
package org.eclipse.recommenders.internal.subwords.rcp;

import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;
import static org.eclipse.recommenders.completion.rcp.processable.ProposalTag.*;
import static org.eclipse.recommenders.internal.subwords.rcp.LCSS.containsSubsequence;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnSingleNameReference;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnSingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.recommenders.completion.rcp.CompletionContexts;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;
import org.eclipse.recommenders.completion.rcp.RecommendersCompletionContext;
import org.eclipse.recommenders.completion.rcp.processable.IProcessableProposal;
import org.eclipse.recommenders.completion.rcp.processable.ProposalProcessor;
import org.eclipse.recommenders.completion.rcp.processable.SessionProcessor;
import org.eclipse.recommenders.rcp.IAstProvider;
import org.eclipse.ui.IEditorPart;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;

@SuppressWarnings("restriction")
public class SubwordsSessionProcessor extends SessionProcessor {

    private IAstProvider astProvider;

    @Inject
    public SubwordsSessionProcessor(IAstProvider astProvider) {
        this.astProvider = astProvider;
    }

    @Override
    public boolean startSession(IRecommendersCompletionContext crContext) {
        int length = crContext.getPrefix().length();
        if (length == 0) {
            return true;
        }

        TreeSet<Integer> triggerlocations = Sets.newTreeSet();

        ASTNode completionNode = crContext.getCompletionNode().orNull();
        ASTNode completionNodeParent = crContext.getCompletionNodeParent().orNull();

        int offset = crContext.getInvocationOffset();

        // List l = new list$
        if (completionNode instanceof CompletionOnSingleTypeReference
                && completionNodeParent instanceof LocalDeclaration && length > 1) {
            triggerlocations.add(offset - length + 1);
        }

        // getPath(pat$) --> getPath(p$):'pat'
        if (completionNode instanceof CompletionOnSingleNameReference && completionNodeParent instanceof MessageSend
                && length > 1) {
            triggerlocations.add(offset - length + 1);
        }
        if (completionNode instanceof CompletionOnSingleNameReference && completionNodeParent == null && length > 1) {
            // pat$ --> $pat
            triggerlocations.add(offset - length);
            // pat$ --> p$at
            triggerlocations.add(offset - length + 1);
        } else {
            triggerlocations.add(offset - length);
        }

        JavaContentAssistInvocationContext javaContext = crContext.getJavaContext();
        ICompilationUnit cu = crContext.getCompilationUnit();
        ITextViewer viewer = javaContext.getViewer();
        IEditorPart editor = lookupEditor(cu);
        Map<IJavaCompletionProposal, CompletionProposal> baseProposals = crContext.getProposals();
        Set<String> sortkeys = Sets.newHashSet();
        for (IJavaCompletionProposal p : baseProposals.keySet()) {
            sortkeys.add(p.getDisplayString());
        }

        for (int trigger : triggerlocations) {
            JavaContentAssistInvocationContext newJavaContext = new JavaContentAssistInvocationContext(viewer, trigger,
                    editor);
            IRecommendersCompletionContext newCrContext = new RecommendersCompletionContext(newJavaContext, astProvider);

            Map<IJavaCompletionProposal, CompletionProposal> newProposals = newCrContext.getProposals();

            for (IJavaCompletionProposal p : newProposals.keySet()) {
                String displayString = p.getDisplayString();
                String completion = CompletionContexts.getPrefixMatchingArea(displayString);
                if (!sortkeys.contains(displayString) && containsSubsequence(completion, crContext.getPrefix())) {
                    baseProposals.put(p, newProposals.get(p));
                    sortkeys.add(p.getDisplayString());
                }
            }
        }
        return true;
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
                    return 1 << 30;
                } else {
                    int score = LCSS.scoreSubsequence(bestSequence);
                    proposal.setTag(IS_PREFIX_MATCH, false);
                    proposal.setTag(SUBWORDS_SCORE, score);
                    return score;
                }
            }
        });
    }
}
