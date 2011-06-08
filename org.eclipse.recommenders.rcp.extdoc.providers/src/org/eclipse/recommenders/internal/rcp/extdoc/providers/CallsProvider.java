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

import java.util.List;

import com.google.inject.Inject;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.CallsCompletionProposalComputer;
import org.eclipse.recommenders.internal.rcp.extdoc.providers.swt.TemplateEditDialog;
import org.eclipse.recommenders.internal.rcp.extdoc.providers.utils.CommunityUtil;
import org.eclipse.recommenders.rcp.codecompletion.IntelligentCompletionContextResolver;
import org.eclipse.recommenders.rcp.extdoc.AbstractBrowserProvider;
import org.eclipse.recommenders.server.extdoc.CallsServer;

public final class CallsProvider extends AbstractBrowserProvider {

    private final CallsCompletionProposalComputer proposalComputer;
    private final CallsServer server = new CallsServer();

    @Inject
    public CallsProvider(final CallsCompletionProposalComputer proposalComputer,
            final IntelligentCompletionContextResolver contextResolver) {
        this.proposalComputer = proposalComputer;
    }

    @Override
    protected String getHtmlContent(final IJavaElementSelection context) {
        final IJavaElement element = context.getJavaElement();
        // TODO: IMethod is just for testing.
        if ((element instanceof IType || element instanceof IField || element instanceof ILocalVariable || element instanceof IMethod)
                && context.getInvocationContext() != null) {
            @SuppressWarnings("unchecked")
            final List<IJavaCompletionProposal> proposals = proposalComputer.computeCompletionProposals(
                    context.getInvocationContext(), null);
            if (!proposals.isEmpty()) {
                return getHtmlForProposals(context.getJavaElement(), proposals);
            }
            return "There are not method calls available for <i>" + context.getJavaElement().getElementName() + "</i>.";
        }
        return "Method calls are only available for Java types and variables.";
    }

    private String getHtmlForProposals(final IJavaElement element, final List<IJavaCompletionProposal> proposals) {
        final StringBuilder builder = new StringBuilder(64);
        builder.append("<p>By analyzing XXX occasions of " + element.getElementName()
                + ", the following patterns have been identified:</p>");
        builder.append("<ol>");

        for (final IJavaCompletionProposal proposal : proposals) {
            builder.append("<li><b>" + proposal.getDisplayString() + "</b> - <u>" + proposal.getRelevance()
                    + "</u></li>");
        }

        builder.append("</ol>");

        final TemplateEditDialog editDialog = new TemplateEditDialog(getShell());
        builder.append(CommunityUtil.getAllFeatures(element, this, editDialog, server));

        return builder.toString();
    }

}
