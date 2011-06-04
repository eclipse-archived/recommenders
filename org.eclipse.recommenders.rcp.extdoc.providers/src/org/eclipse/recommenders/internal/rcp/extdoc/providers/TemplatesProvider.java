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

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.internal.rcp.codecompletion.templates.TemplatesCompletionProposalComputer;
import org.eclipse.recommenders.internal.rcp.extdoc.providers.swt.TemplateEditDialog;
import org.eclipse.recommenders.rcp.codecompletion.IIntelligentCompletionContext;
import org.eclipse.recommenders.rcp.codecompletion.IntelligentCompletionContextResolver;
import org.eclipse.recommenders.rcp.extdoc.AbstractBrowserProvider;
import org.eclipse.recommenders.rcp.extdoc.features.CommentsDialog;
import org.eclipse.recommenders.rcp.extdoc.features.CommentsIcon;
import org.eclipse.recommenders.rcp.extdoc.features.DeleteIcon;
import org.eclipse.recommenders.rcp.extdoc.features.EditIcon;
import org.eclipse.recommenders.rcp.extdoc.features.StarsRating;
import org.eclipse.recommenders.server.extdoc.TemplatesServer;

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

        if (context.getInvocationContext() != null) {
            final IIntelligentCompletionContext completionContext = contextResolver.resolveContext(context
                    .getInvocationContext());
            final List<IJavaCompletionProposal> proposals = proposalComputer
                    .computeCompletionProposals(completionContext);
            if (!proposals.isEmpty()) {
                return getHtmlForProposals(context.getJavaElement(), proposals);
            }
            return "There are not templates available for " + context.getJavaElement().getElementName();
        }
        return "Templates are not available for this element type.";
    }

    private String getHtmlForProposals(final IJavaElement element, final List<IJavaCompletionProposal> proposals) {
        final StringBuilder builder = new StringBuilder(64);
        builder.append("<p>By analyzing XXX occasions of " + element.getElementName()
                + ", the following patterns have been identified:</p>");
        builder.append("<ol>");

        for (final IJavaCompletionProposal proposal : proposals) {
            builder.append("<li><p>" + proposal.getDisplayString() + "<span>");
            builder.append(getCommunityFeatures(element) + "</span></p><ol>");
            for (final String line : proposal.getAdditionalProposalInfo().split(";\r?\n")) {
                builder.append("<li><i>" + line + ";</i></li>");
            }
            builder.append("</ol></li>");
        }

        builder.append("</ol>");
        return builder.toString();
    }

    private String getCommunityFeatures(final IJavaElement element) {
        final StringBuilder builder = new StringBuilder(128);
        builder.append(addListenerAndGetHtml(getCommentsIcon(element)));
        builder.append(addListenerAndGetHtml(getEditIcon(element)));
        builder.append(addListenerAndGetHtml(getDeleteIcon(element)));
        builder.append(addListenerAndGetHtml(getStarsRating(element)));
        return builder.toString();
    }

    private EditIcon getEditIcon(final IJavaElement element) {
        final TemplateEditDialog editDialog = new TemplateEditDialog(getShell());
        return new EditIcon(editDialog);
    }

    private DeleteIcon getDeleteIcon(final IJavaElement element) {
        final TemplateEditDialog editDialog = new TemplateEditDialog(getShell());
        return new DeleteIcon(editDialog);
    }

    private CommentsIcon getCommentsIcon(final IJavaElement element) {
        final CommentsDialog commentsDialog = new CommentsDialog(getShell(), null, this, element);
        return new CommentsIcon(commentsDialog);
    }

    private StarsRating getStarsRating(final IJavaElement element) {
        return new StarsRating(element, new TemplatesServer(), this);
    }
}
