/**
 * Copyright (c) 2011 Paul-Emmanuel Faidherbe.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Paul-Emmanuel Faidherbe - Completion generalization
 */
package org.eclipse.recommenders.internal.completion.rcp.subwords.proposals;

import org.eclipse.jdt.core.CompletionContext;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.ui.text.java.AnonymousTypeCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.FilledArgumentNamesMethodProposal;
import org.eclipse.jdt.internal.ui.text.java.GetterSetterCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.JavaFieldWithCastedReceiverCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.JavaMethodCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.LazyGenericTypeProposal;
import org.eclipse.jdt.internal.ui.text.java.LazyJavaTypeCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.OverrideCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.ParameterGuessingProposal;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.recommenders.internal.completion.rcp.subwords.SubwordsProposalContext;
import org.eclipse.recommenders.utils.rcp.internal.RecommendersUtilsPlugin;
import org.eclipse.swt.graphics.Image;

@SuppressWarnings("restriction")
public class ProposalFactory {

    public static IJavaCompletionProposal createFromJDTProposal(final SubwordsProposalContext subwordsContext) {
        final IJavaCompletionProposal jdtProposal = subwordsContext.getJdtProposal();
        final Class<? extends IJavaCompletionProposal> c = jdtProposal.getClass();

        try {
            if (JavaMethodCompletionProposal.class == c) {
                return createJavaMethodCompletionProposal(subwordsContext);
            } else if (JavaFieldWithCastedReceiverCompletionProposal.class == c) {
                return createJavaFieldWithCastedReceiverCompletionProposal(subwordsContext);
            } else if (OverrideCompletionProposal.class == c) {
                return createOverrideCompletionProposal(subwordsContext);
            } else if (AnonymousTypeCompletionProposal.class == c) {
                return createAnonymousTypeCompletionProposal(subwordsContext);
            } else if (JavaCompletionProposal.class == c) {
                return createJavaCompletionProposal(subwordsContext);
            } else if (LazyGenericTypeProposal.class == c) {
                return createLazyGenericTypeProposal(subwordsContext);
            } else if (LazyJavaTypeCompletionProposal.class == c) {
                return createLazyJavaTypeCompletionProposal(subwordsContext);
            } else if (FilledArgumentNamesMethodProposal.class == c) {
                return createFilledArgumentNamesMethodProposal(subwordsContext);
            } else if (ParameterGuessingProposal.class == c) {
                return createParameterGuessingProposal(subwordsContext);
            } else if (GetterSetterCompletionProposal.class == c) {
            }
        } catch (final Exception e) {
            RecommendersUtilsPlugin.logError(e, "wrapping jdt proposal failed");
        }
        return jdtProposal;
    }

    private static IJavaCompletionProposal createLazyGenericTypeProposal(final SubwordsProposalContext subwordsContext) {
        return new SwLazyGenericTypeProposal(subwordsContext.getProposal(), subwordsContext.getContext(),
                subwordsContext);
    }

    private static IJavaCompletionProposal createFilledArgumentNamesMethodProposal(
            final SubwordsProposalContext subwordsContext) {
        return new SwFilledArgumentNamesMethodProposal(subwordsContext.getProposal(), subwordsContext.getContext(),
                subwordsContext);
    }

    public static SwParameterGuessingProposal createParameterGuessingProposal(
            final SubwordsProposalContext subwordsContext) {

        final CompletionProposal proposal = subwordsContext.getProposal();
        final JavaContentAssistInvocationContext context = subwordsContext.getContext();
        final boolean fillBestGuess = true;
        final CompletionContext coreContext = context.getCoreContext();
        return new SwParameterGuessingProposal(proposal, context, coreContext, fillBestGuess, subwordsContext);
    }

    public static SwAnonymousTypeCompletionProposal createAnonymousTypeCompletionProposal(
            final SubwordsProposalContext subwordsContext) throws JavaModelException {
        final JavaContentAssistInvocationContext context = subwordsContext.getContext();
        final CompletionProposal proposal = subwordsContext.getProposal();
        final JavaCompletionProposal jdtProposal = subwordsContext.getJdtProposal();
        final IJavaProject project = context.getProject();
        final String declarationSignature = String.valueOf(proposal.getDeclarationSignature());
        final String declarationKey = String.valueOf(proposal.getDeclarationKey());
        final String completionText = String.valueOf(proposal.getCompletion());

        return new SwAnonymousTypeCompletionProposal(project, context.getCompilationUnit(), context,
                proposal.getReplaceStart(), jdtProposal.getReplacementLength(), completionText,
                jdtProposal.getStyledDisplayString(), declarationSignature, (IType) project.findElement(declarationKey,
                        null), jdtProposal.getRelevance(), subwordsContext);
    }

    public static SwJavaFieldWithCastedReceiverCompletionProposal createJavaFieldWithCastedReceiverCompletionProposal(
            final SubwordsProposalContext subwordsContext) {
        final JavaCompletionProposal jdtProposal = subwordsContext.getJdtProposal();
        final int relevance = subwordsContext.calculateRelevance();
        return new SwJavaFieldWithCastedReceiverCompletionProposal(jdtProposal.getDisplayString(),
                jdtProposal.getReplacementOffset(), jdtProposal.getReplacementLength(), jdtProposal.getImage(),
                jdtProposal.getStyledDisplayString(), relevance, true, subwordsContext.getContext(),
                subwordsContext.getProposal(), subwordsContext);
    }

    public static SwJavaCompletionProposal createJavaCompletionProposal(final SubwordsProposalContext subwordsContext) {
        final JavaCompletionProposal jdtProposal = subwordsContext.getJdtProposal();
        final int relevance = subwordsContext.calculateRelevance();

        return new SwJavaCompletionProposal(jdtProposal.getReplacementString(), subwordsContext.getProposal()
                .getReplaceStart(), jdtProposal.getReplacementLength(), jdtProposal.getImage(),
                jdtProposal.getStyledDisplayString(), relevance, true, subwordsContext.getContext(), subwordsContext);
    }

    public static SwJavaMethodCompletionProposal createJavaMethodCompletionProposal(
            final SubwordsProposalContext subwordsContext) {
        final SwJavaMethodCompletionProposal res = new SwJavaMethodCompletionProposal(subwordsContext.getProposal(),
                subwordsContext.getContext(), subwordsContext);
        res.setRelevance(subwordsContext.calculateRelevance());
        return res;
    }

    public static SwLazyJavaTypeCompletionProposal createLazyJavaTypeCompletionProposal(
            final SubwordsProposalContext subwordsContext) {

        final CompletionProposal proposal = subwordsContext.getProposal();
        final SwLazyJavaTypeCompletionProposal res = new SwLazyJavaTypeCompletionProposal(proposal,
                subwordsContext.getContext(), subwordsContext);
        res.setRelevance(proposal.getRelevance());
        return res;
    }

    public static SwOverrideCompletionProposal createOverrideCompletionProposal(
            final SubwordsProposalContext subwordsContext) {
        final JavaContentAssistInvocationContext context = subwordsContext.getContext();
        final JavaCompletionProposal jdtProposal = subwordsContext.getJdtProposal();
        final CompletionProposal proposal = subwordsContext.getProposal();
        final String signature = String.valueOf(proposal.getSignature());
        final String completionText = String.valueOf(proposal.getCompletion());
        final String proposalName = String.valueOf(proposal.getName());
        final SwOverrideCompletionProposal res = new SwOverrideCompletionProposal(context.getProject(),
                context.getCompilationUnit(), proposalName, Signature.getParameterTypes(signature),
                proposal.getReplaceStart(), jdtProposal.getReplacementLength(), jdtProposal.getStyledDisplayString(),
                completionText, subwordsContext);
        final Image image = jdtProposal.getImage();
        res.setImage(image);
        res.setRelevance(jdtProposal.getRelevance());
        return res;
    }
}
