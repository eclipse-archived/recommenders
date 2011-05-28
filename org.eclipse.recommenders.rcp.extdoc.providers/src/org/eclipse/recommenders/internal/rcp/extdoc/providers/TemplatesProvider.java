/**
 * Copyright (c) 2011 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.extdoc.providers;

import com.google.inject.Inject;

import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.internal.rcp.codecompletion.templates.TemplatesCompletionProposalComputer;
import org.eclipse.recommenders.rcp.codecompletion.IIntelligentCompletionContext;
import org.eclipse.recommenders.rcp.codecompletion.IntelligentCompletionContextResolver;
import org.eclipse.recommenders.rcp.extdoc.AbstractBrowserProvider;

public final class TemplatesProvider extends AbstractBrowserProvider {

    private final TemplatesCompletionProposalComputer proposalComputer;
    private final IntelligentCompletionContextResolver contextResolver;

    @Inject
    public TemplatesProvider(final TemplatesCompletionProposalComputer proposalComputer,
            final IntelligentCompletionContextResolver contextResolver) {
        this.proposalComputer = proposalComputer;
        this.contextResolver = contextResolver;
    }

    @Override
    public String getHtmlContent(final IJavaElementSelection context) {
        final StringBuilder builder = new StringBuilder();
        IIntelligentCompletionContext completionContext = null;

        if (context.getInvocationContext() != null) {
            completionContext = contextResolver.resolveContext(context.getInvocationContext());
            for (final IJavaCompletionProposal proposal : proposalComputer
                    .computeCompletionProposals(completionContext)) {
                builder.append(proposal.getDisplayString() + "\n");
            }
        }

        builder.append("\n" + context + "\n\n" + completionContext);

        return builder.toString().replace("\n", "<br/>");
    }
}
