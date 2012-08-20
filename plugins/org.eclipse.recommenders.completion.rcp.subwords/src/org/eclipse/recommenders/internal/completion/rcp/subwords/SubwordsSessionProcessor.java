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
package org.eclipse.recommenders.internal.completion.rcp.subwords;

import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;
import static org.eclipse.recommenders.internal.completion.rcp.subwords.LCSS.containsSubsequence;
import static org.eclipse.recommenders.internal.completion.rcp.subwords.SubwordsUtils.getTokensBetweenLastWhitespaceAndFirstOpeningBracket;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

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
import org.eclipse.recommenders.completion.rcp.IProcessableProposal;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContextFactory;
import org.eclipse.recommenders.completion.rcp.ProposalProcessor;
import org.eclipse.recommenders.completion.rcp.SessionProcessor;
import org.eclipse.ui.IEditorPart;

import com.google.common.collect.Sets;

public class SubwordsSessionProcessor extends SessionProcessor {

    private IRecommendersCompletionContextFactory computer;

    @Inject
    public SubwordsSessionProcessor(IRecommendersCompletionContextFactory ctxFactory) {
        this.computer = ctxFactory;
    }

    @Override
    public void startSession(IRecommendersCompletionContext crContext) {
        int length = crContext.getPrefix().length();
        if (length == 0) return;

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
        IEditorPart editor = EditorUtility.isOpenInEditor(cu);
        Map<IJavaCompletionProposal, CompletionProposal> baseProposals = crContext.getProposals();
        Set<String> sortkeys = Sets.newHashSet();
        for (IJavaCompletionProposal p : baseProposals.keySet()) {
            sortkeys.add(p.getDisplayString());
        }

        for (int trigger : triggerlocations) {
            JavaContentAssistInvocationContext newJavaContext =
                    new JavaContentAssistInvocationContext(viewer, trigger, editor);
            IRecommendersCompletionContext newCrContext = computer.create(newJavaContext);

            Map<IJavaCompletionProposal, CompletionProposal> newProposals = newCrContext.getProposals();

            for (IJavaCompletionProposal p : newProposals.keySet()) {
                String displayString = p.getDisplayString();
                String completion = getTokensBetweenLastWhitespaceAndFirstOpeningBracket(displayString);
                if (!sortkeys.contains(displayString) && containsSubsequence(completion, crContext.getPrefix())) {
                    baseProposals.put(p, newProposals.get(p));
                    sortkeys.add(p.getDisplayString());
                }
            }
        }
    }

    @Override
    public void process(final IProcessableProposal proposal) {
        proposal.getProposalProcessorManager().addProcessor(new ProposalProcessor() {

            int[] bestSequence = new int[0];
            String matchingArea = getTokensBetweenLastWhitespaceAndFirstOpeningBracket(proposal.getDisplayString());
            String prefix;

            @Override
            public boolean isPrefix(String prefix) {
                this.prefix = prefix;
                bestSequence = LCSS.bestSubsequence(matchingArea, prefix);
                return prefix.isEmpty() || bestSequence.length > 0;
            }

            @Override
            public void modifyDisplayString(StyledString displayString) {
                for (int index : bestSequence)
                    displayString.setStyle(index, 1, StyledString.COUNTER_STYLER);
            }

            @Override
            public int modifyRelevance() {
                if (ArrayUtils.isEmpty(bestSequence)) return 0;
                if (startsWithIgnoreCase(matchingArea, prefix)) {
                    return 1 << 30;
                } else {
                    return LCSS.scoreSubsequence(bestSequence);
                }

            }
        });
    }
}
