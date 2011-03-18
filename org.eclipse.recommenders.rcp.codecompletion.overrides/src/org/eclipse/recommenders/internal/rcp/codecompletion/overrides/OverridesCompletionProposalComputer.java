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
package org.eclipse.recommenders.internal.rcp.codecompletion.overrides;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.recommenders.commons.utils.names.VmMethodName;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.CompilationUnit;
import org.eclipse.recommenders.rcp.IArtifactStore;
import org.eclipse.recommenders.rcp.codecompletion.CompletionProposalDecorator;
import org.eclipse.recommenders.rcp.codecompletion.IIntelligentCompletionContext;
import org.eclipse.recommenders.rcp.codecompletion.IntelligentCompletionContextResolver;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

@SuppressWarnings("restriction")
public class OverridesCompletionProposalComputer implements IJavaCompletionProposalComputer {
    private final IArtifactStore artifactStore;

    private IIntelligentCompletionContext ctx;

    private ICompilationUnit jdtCu;

    private final InstantOverridesRecommender recommender;

    private List<OverridesRecommendation> recommendations;

    private List<IJavaCompletionProposal> proposals;

    private final IntelligentCompletionContextResolver contextResolver;

    @Inject
    public OverridesCompletionProposalComputer(final IArtifactStore artifactStore,
            final InstantOverridesRecommender recommender, final IntelligentCompletionContextResolver contextResolver) {
        this.artifactStore = artifactStore;
        this.recommender = recommender;
        this.contextResolver = contextResolver;
    };

    @Override
    public List computeCompletionProposals(final ContentAssistInvocationContext context, final IProgressMonitor monitor) {
        final JavaContentAssistInvocationContext jCtx = (JavaContentAssistInvocationContext) context;
        if (contextResolver.hasProjectRecommendersNature(jCtx)) {
            final IIntelligentCompletionContext iCtx = contextResolver.resolveContext(jCtx);
            return computeProposals(iCtx);
        } else {
            return Collections.emptyList();
        }
    }

    private List<IJavaCompletionProposal> computeProposals(final IIntelligentCompletionContext ctx) {
        this.ctx = ctx;
        this.jdtCu = ctx.getCompilationUnit();
        if (!isCompletionTriggeredInTypeDeclarationBody()) {
            return Collections.emptyList();
        } else if (!artifactStore.hasArtifact(jdtCu, CompilationUnit.class)) {
            return Collections.emptyList();
        }
        final CompilationUnit recCu = artifactStore.loadArtifact(jdtCu, CompilationUnit.class);
        recommendations = recommender.createRecommendations(recCu.primaryType);
        computeProposals();
        return proposals;
    }

    private void computeProposals() {
        proposals = Lists.newLinkedList();
        createFilteredOverridesRecommendations();
    }

    private void createFilteredOverridesRecommendations() {
        for (final CompletionProposal eclProposal : ctx.getJdtProposals()) {
            switch (eclProposal.getKind()) {
            case CompletionProposal.METHOD_DECLARATION:
                createOverrideProposalIfRecommended(eclProposal);
            }
        }
    }

    private void createOverrideProposalIfRecommended(final CompletionProposal eclProposal) {
        final VmMethodName ref = getProposalKeyAsVmMethodName(eclProposal);
        for (final OverridesRecommendation recommendation : recommendations) {
            if (ref.getSignature().equals(recommendation.method.getSignature())) {
                final IJavaCompletionProposal javaProposal = ctx.toJavaCompletionProposal(eclProposal);
                final CompletionProposalDecorator decoratedProposal = new CompletionProposalDecorator(javaProposal,
                        recommendation);
                proposals.add(decoratedProposal);
            }
        }
    }

    private VmMethodName getProposalKeyAsVmMethodName(final CompletionProposal eclProposal) {
        String key = String.valueOf(eclProposal.getKey()).replaceAll(";\\.", ".");
        final int exceptionSeparator = key.indexOf("|");
        if (exceptionSeparator != -1) {
            key = key.substring(0, exceptionSeparator);
        }
        return VmMethodName.get(key);
    }

    private boolean isCompletionTriggeredInTypeDeclarationBody() {
        return ctx.getEnclosingMethod() == null && ctx.getEnclosingType() != null;
    }

    @Override
    public void sessionStarted() {
    }

    @Override
    public List computeContextInformation(final ContentAssistInvocationContext context, final IProgressMonitor monitor) {
        return Collections.emptyList();
    }

    @Override
    public String getErrorMessage() {
        return null;
    }

    @Override
    public void sessionEnded() {

    }
}
