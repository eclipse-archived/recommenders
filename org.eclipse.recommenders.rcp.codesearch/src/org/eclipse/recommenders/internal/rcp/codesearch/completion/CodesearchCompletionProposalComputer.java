/**
 * Copyright (c) 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.codesearch.completion;

import static org.eclipse.recommenders.commons.utils.Checks.cast;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.CompilationUnit;
import org.eclipse.recommenders.internal.rcp.codesearch.views.CodesearchController;
import org.eclipse.recommenders.rcp.IArtifactStore;
import org.eclipse.recommenders.rcp.codecompletion.IIntelligentCompletionContext;
import org.eclipse.recommenders.rcp.codecompletion.IntelligentCompletionContextResolver;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

@SuppressWarnings("rawtypes")
public class CodesearchCompletionProposalComputer implements IJavaCompletionProposalComputer {
    private final IArtifactStore artifactStore;
    private final IntelligentCompletionContextResolver contextResolver;
    private final CodesearchController controller;

    @Inject
    public CodesearchCompletionProposalComputer(final IArtifactStore artifactStore,
            final IntelligentCompletionContextResolver contextResolver, final CodesearchController controller) {
        this.artifactStore = artifactStore;
        this.contextResolver = contextResolver;
        this.controller = controller;
    }

    @Override
    public void sessionStarted() {
    }

    @Override
    public List computeCompletionProposals(final ContentAssistInvocationContext context, final IProgressMonitor monitor) {
        final JavaContentAssistInvocationContext javaContext = cast(context);
        final IIntelligentCompletionContext recContext = contextResolver.resolveContext(javaContext);
        final ICompilationUnit jdtCu = recContext.getCompilationUnit();
        if (!artifactStore.hasArtifact(jdtCu, CompilationUnit.class)) {
            return Collections.emptyList();
        }
        final CompilationUnit recCu = artifactStore.loadArtifact(jdtCu, CompilationUnit.class);
        final List<IJavaCompletionProposal> res = Lists.newArrayList();
        // if (recContext.getVariable() != null) {
        // res.add(new SearchSimilarVariableUsagesProposal(100, recCu,
        // recContext, searchClient));
        // }
        if (recContext.getEnclosingMethod() != null) {
            res.add(new SearchSimilarMethodsProposal(100, recCu, recContext, controller));
        }
        return res;
    }

    @Override
    public List computeContextInformation(final ContentAssistInvocationContext context, final IProgressMonitor monitor) {
        return null;
    }

    @Override
    public String getErrorMessage() {
        return null;
    }

    @Override
    public void sessionEnded() {
    }
}
