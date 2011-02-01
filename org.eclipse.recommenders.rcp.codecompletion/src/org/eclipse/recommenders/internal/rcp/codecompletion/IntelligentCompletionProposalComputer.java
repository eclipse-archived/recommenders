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
package org.eclipse.recommenders.internal.rcp.codecompletion;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.time.StopWatch;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.recommenders.internal.rcp.RecommendersNature;
import org.eclipse.recommenders.rcp.RecommendersPlugin;
import org.eclipse.recommenders.rcp.codecompletion.IIntelligentCompletionEngine;
import org.eclipse.recommenders.rcp.utils.JavaElementResolver;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

@SuppressWarnings({ "rawtypes" })
public class IntelligentCompletionProposalComputer implements IJavaCompletionProposalComputer {

    private final Set<IIntelligentCompletionEngine> engines;
    private final JavaElementResolver resolver;

    @Inject
    public IntelligentCompletionProposalComputer(final Set<IIntelligentCompletionEngine> engines,
            final JavaElementResolver resolver) {
        this.engines = engines;
        this.resolver = resolver;
    }

    @Override
    public List computeCompletionProposals(final ContentAssistInvocationContext context,
            final IProgressMonitor /* actually a NullProgressMonitor in e3.6 --> */monitor) {
        try {
            return doComputeCompletionProposals(context);
        } catch (final Exception x) {
            RecommendersPlugin.logError(x, "Exception occured during analysis of context '%s'", context);
            return Collections.emptyList();
        }
    }

    private List doComputeCompletionProposals(final ContentAssistInvocationContext context) {
        final List<IJavaCompletionProposal> proposals = Lists.newLinkedList();
        final JavaContentAssistInvocationContext jCtx = (JavaContentAssistInvocationContext) context;

        if (hasProjectRecommendersNature(jCtx)) {
            final IntelligentCompletionContext iCtx = createCompletionContext(jCtx);

            for (final IIntelligentCompletionEngine engine : engines) {
                proposals.addAll(computeCompletionProposals(engine, iCtx));
            }
        } else {
            proposals.add(createEnableRecommendersProposal(jCtx));
        }
        return proposals;
    }

    private IntelligentCompletionContext createCompletionContext(final JavaContentAssistInvocationContext jCtx) {
        final StopWatch w = new StopWatch();
        w.start();
        final IntelligentCompletionContext iCtx = new IntelligentCompletionContext(jCtx, resolver);
        w.stop();
        System.out.println("building intelligent ctx took: " + w);
        return iCtx;
    }

    private Collection<? extends IJavaCompletionProposal> computeCompletionProposals(
            final IIntelligentCompletionEngine engine, final IntelligentCompletionContext iCtx) {
        final StopWatch w = new StopWatch();
        w.start();
        try {
            return engine.computeProposals(iCtx);
        } catch (final Throwable t) {
            logEngineCausedExcetion(engine, t);
            return Collections.emptyList();
        } finally {
            w.stop();
            System.out.printf("engine '%s' ctx took %s\n", engine.getClass(), w);
        }
    }

    private CompletionProposalEnableRecommenders createEnableRecommendersProposal(
            final JavaContentAssistInvocationContext jCtx) {
        return new CompletionProposalEnableRecommenders(getProjectFromContext(jCtx), jCtx.getInvocationOffset());
    }

    private boolean hasProjectRecommendersNature(final JavaContentAssistInvocationContext jCtx) {
        final IProject project = getProjectFromContext(jCtx);
        return RecommendersNature.hasNature(project);
    }

    private IProject getProjectFromContext(final JavaContentAssistInvocationContext jCtx) {
        final ICompilationUnit cu = jCtx.getCompilationUnit();
        final IJavaProject javaProject = cu.getJavaProject();
        final IProject project = javaProject.getProject();
        return project;
    }

    private void logEngineCausedExcetion(final IIntelligentCompletionEngine engine, final Throwable t) {
        final Class<? extends IIntelligentCompletionEngine> clazz = engine.getClass();
        IntelligentCompletionPlugin.logError(t, "Intelligent Completion Engine '%s' caused an exception (class:%s).",
                clazz.getSimpleName(), clazz);
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

    @Override
    public void sessionStarted() {
    }
}
