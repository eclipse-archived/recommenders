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
package org.eclipse.recommenders.internal.completion.rcp.sandbox;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
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
import org.eclipse.recommenders.utils.annotations.Provisional;
import org.eclipse.ui.IEditorPart;

import com.google.common.collect.Sets;

@Provisional("for sandbox testing only")
public final class TypoSessionProcessor extends SessionProcessor {

    private static final int MAX_DISTANCE = 2;
    private IRecommendersCompletionContextFactory computer;

    @Inject
    public TypoSessionProcessor(IRecommendersCompletionContextFactory ctxFactory) {
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
                if (!sortkeys.contains(displayString) && isPotentialMatch(completion, crContext.getPrefix())) {
                    baseProposals.put(p, newProposals.get(p));
                    sortkeys.add(p.getDisplayString());
                }
            }
        }
    }

    @Override
    public void process(final IProcessableProposal proposal) throws JavaModelException {

        final String completion =
                getTokensBetweenLastWhitespaceAndFirstOpeningBracket(proposal.getDisplayString()).toLowerCase();

        proposal.getProposalProcessorManager().addProcessor(new ProposalProcessor() {

            String append = null;

            @Override
            public boolean isPrefix(String prefix) {
                int max_distance = MAX_DISTANCE;
                if (prefix.length() < 4) max_distance = 0;
                // if (prefix.length() > completion.length()) return false;
                String s = StringUtils.substring(completion, 0, prefix.length()).toLowerCase();
                int distance = StringUtils.getLevenshteinDistance(s, prefix.toLowerCase(), max_distance);
                if (distance == 0)
                    append = null;
                else
                    append = "typo!";
                System.out.printf("pref %s compl %s dist %d\n", prefix, completion, distance);
                return distance >= 0 ? true : false;
            }

            @Override
            public void modifyDisplayString(StyledString displayString) {
                if (append != null) {
                    // displayString.append("- " + append, StyledString.DECORATIONS_STYLER);
                }
            }

            @Override
            public int modifyRelevance() {
                // if (append != null) return -1;
                return 0;
            }

        });
    }

    private boolean isPotentialMatch(final String completion, String prefix) {
        String s = StringUtils.substring(completion, 0, prefix.length()).toLowerCase();
        int distance = StringUtils.getLevenshteinDistance(s, prefix.toLowerCase(), MAX_DISTANCE);
        System.out.printf("-create pref %s compl %s dist %d\n", prefix, completion, distance);
        return distance > 0 ? true : false;
    }

    public static String getTokensBetweenLastWhitespaceAndFirstOpeningBracket(final CompletionProposal proposal) {
        boolean isPotentialMethodDecl = proposal.getKind() == CompletionProposal.POTENTIAL_METHOD_DECLARATION;
        char[] token = proposal.getCompletion();
        if (Arrays.equals(token, new char[] { '(', ')' })) {
            token = proposal.getName();
        } else if (isPotentialMethodDecl && proposal.getCompletion().length == 0) {
            char[] signature = proposal.getDeclarationSignature();
            char[] typeName = Signature.getSignatureSimpleName(signature);
            return String.valueOf(typeName);
        }
        return getTokensBetweenLastWhitespaceAndFirstOpeningBracket(String.valueOf(token));
    }

    public static String getTokensBetweenLastWhitespaceAndFirstOpeningBracket(String completion) {
        if (completion.contains("(")) {
            completion = getMethodIdentifierFromProposalText(completion);
        } else {
            completion = StringUtils.substringBefore(completion, " ");
        }
        return completion;
    }

    private static String getMethodIdentifierFromProposalText(String completion) {
        completion = StringUtils.substringBefore(completion, "(");
        if (completion.contains(" ")) {
            completion = StringUtils.substringAfterLast(completion, " ");
        }
        return completion;
    }
}
