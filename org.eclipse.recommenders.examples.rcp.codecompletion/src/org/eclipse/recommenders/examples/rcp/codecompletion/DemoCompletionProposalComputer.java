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
package org.eclipse.recommenders.examples.rcp.codecompletion;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.recommenders.rcp.codecompletion.IIntelligentCompletionContext;
import org.eclipse.recommenders.rcp.codecompletion.IntelligentCompletionContextResolver;

import com.google.inject.Inject;

@SuppressWarnings("restriction")
public class DemoCompletionProposalComputer implements IJavaCompletionProposalComputer {

    private final IntelligentCompletionContextResolver contextResolver;

    @Inject
    public DemoCompletionProposalComputer(final IntelligentCompletionContextResolver contextResolver) {
        this.contextResolver = contextResolver;
    }

    @Override
    public List computeCompletionProposals(final ContentAssistInvocationContext context, final IProgressMonitor monitor) {
        final JavaContentAssistInvocationContext jCtx = (JavaContentAssistInvocationContext) context;
        if (contextResolver.hasProjectRecommendersNature(jCtx)) {
            final IIntelligentCompletionContext iContext = contextResolver.resolveContext(jCtx);
            return Collections.singletonList(makeDemoProposal(iContext));
        } else {
            return Collections.emptyList();
        }
    }

    IJavaCompletionProposal makeDemoProposal(final IIntelligentCompletionContext ctx) {
        final ASTNode node = ctx.getCompletionNode();
        final String displayString = node == null ? "<unkown>" : node.toString();
        //
        final JavaCompletionProposal p = new JavaCompletionProposal("", 0, 0, null, displayString, 1) {

            @Override
            public Object getAdditionalProposalInfo(final IProgressMonitor monitor) {
                return StringEscapeUtils.escapeHtml4(ctx.toString()).replaceAll("\\n", "<br>");
            }
        };
        return p;
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
