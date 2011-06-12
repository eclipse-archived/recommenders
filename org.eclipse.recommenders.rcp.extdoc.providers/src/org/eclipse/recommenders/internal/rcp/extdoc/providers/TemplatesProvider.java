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
import org.eclipse.recommenders.internal.rcp.codecompletion.templates.TemplatesCompletionProposalComputer;
import org.eclipse.recommenders.internal.rcp.extdoc.providers.swt.TemplateEditDialog;
import org.eclipse.recommenders.internal.rcp.extdoc.providers.utils.CompletionInvocationContext;
import org.eclipse.recommenders.rcp.codecompletion.IIntelligentCompletionContext;
import org.eclipse.recommenders.rcp.codecompletion.IntelligentCompletionContextResolver;
import org.eclipse.recommenders.rcp.extdoc.AbstractProviderComposite;
import org.eclipse.recommenders.rcp.extdoc.IDeletionProvider;
import org.eclipse.recommenders.server.extdoc.TemplatesServer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public final class TemplatesProvider extends AbstractProviderComposite implements IDeletionProvider {

    private final TemplatesCompletionProposalComputer proposalComputer;
    private final IntelligentCompletionContextResolver contextResolver;
    private final TemplatesServer server;

    @Inject
    public TemplatesProvider(final TemplatesCompletionProposalComputer proposalComputer,
            final IntelligentCompletionContextResolver contextResolver) {
        this.proposalComputer = proposalComputer;
        this.contextResolver = contextResolver;
        server = new TemplatesServer();
    }

    public String getHtmlContent(final IJavaElementSelection selection) {
        final IJavaElement element = selection.getJavaElement();
        // TODO: IMethod is just for testing.
        if ((element instanceof IType || element instanceof IField || element instanceof ILocalVariable || element instanceof IMethod)
                && selection.getInvocationContext() != null) {
            final CompletionInvocationContext context = new CompletionInvocationContext(
                    selection.getInvocationContext(), selection.getEditor());
            final IIntelligentCompletionContext completionContext = contextResolver.resolveContext(context);
            final List<IJavaCompletionProposal> proposals = proposalComputer
                    .computeCompletionProposals(completionContext);
            if (!proposals.isEmpty()) {
                return getHtmlForProposals(element, proposals);
            }
            return "There are not templates available for <i>" + element.getElementName() + "</i>.";
        }
        return "Templates are only available for Java types and variables.";
    }

    private String getHtmlForProposals(final IJavaElement element, final List<IJavaCompletionProposal> proposals) {
        final StringBuilder builder = new StringBuilder(64);
        builder.append("<p>By analyzing XXX occasions of " + element.getElementName()
                + ", the following patterns have been identified:</p>");
        builder.append("<ol>");

        for (final IJavaCompletionProposal proposal : proposals) {
            builder.append("<li><p>" + proposal.getDisplayString() + "<span>");
            builder.append(getCommunityFeatures(proposal) + "</span></p><ol>");
            for (final String line : proposal.getAdditionalProposalInfo().split(";\r?\n")) {
                builder.append("<li><i>" + line + ";</i></li>");
            }
            builder.append("</ol></li>");
        }

        builder.append("</ol>");
        return builder.toString();
    }

    private String getCommunityFeatures(final IJavaCompletionProposal proposal) {
        final TemplateEditDialog editDialog = new TemplateEditDialog(getShell());

        final StringBuilder builder = new StringBuilder(128);
        // builder.append(addListenerAndGetHtml(CommunityUtil.getCommentsIcon(proposal,
        // proposal.getDisplayString(), this)));
        // builder.append(addListenerAndGetHtml(CommunityUtil.getEditIcon(editDialog)));
        // builder.append(addListenerAndGetHtml(CommunityUtil.getDeleteIcon(proposal,
        // proposal.getDisplayString(), this)));
        // builder.append(addListenerAndGetHtml(CommunityUtil.getStarsRating(proposal,
        // this, server)));
        return builder.toString();
    }

    @Override
    public void requestDeletion(final Object object) {
        // TODO Auto-generated method stub
    }

    @Override
    protected Control createContentControl(final Composite parent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void updateContent(final IJavaElementSelection selection) {
        // TODO Auto-generated method stub

    }
}
