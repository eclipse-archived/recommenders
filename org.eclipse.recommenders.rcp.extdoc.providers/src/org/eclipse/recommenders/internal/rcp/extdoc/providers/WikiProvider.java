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

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.internal.rcp.extdoc.providers.swt.CommentsDialog;
import org.eclipse.recommenders.internal.rcp.extdoc.providers.swt.WikiEditDialog;
import org.eclipse.recommenders.rcp.extdoc.AbstractBrowserProvider;
import org.eclipse.recommenders.rcp.extdoc.MarkupParser;
import org.eclipse.recommenders.rcp.extdoc.features.CommentsIcon;
import org.eclipse.recommenders.rcp.extdoc.features.EditIcon;
import org.eclipse.recommenders.rcp.extdoc.features.StarsRating;
import org.eclipse.recommenders.rcp.extdoc.features.StarsRatingsFeature;
import org.eclipse.recommenders.server.extdoc.WikiServer;

public final class WikiProvider extends AbstractBrowserProvider {

    private final WikiServer server;
    private final MarkupParser parser;

    @Inject
    WikiProvider(final WikiServer server, final MarkupParser parser) {
        this.server = server;
        this.parser = parser;
    }

    @Override
    protected String getHtmlContent(final IJavaElementSelection selection) {
        final IJavaElement element = selection.getJavaElement();
        String markup = null;
        String txt = null;
        if (element != null) {
            markup = server.getText(element);
            if (markup != null) {
                txt = parser.parseTextile(markup);
            }
        }
        if (txt == null) {
            txt = "No Wiki available for " + element;
        }
        return String.format("%s<br/><br/>%s", getCommunityFeatures(element, markup), txt);
    }

    public void update(final IJavaElement javaElement, final String text) {
        server.setText(javaElement, text);
        redraw();
    }

    private String getCommunityFeatures(final IJavaElement element, final String markup) {
        final StringBuilder builder = new StringBuilder();
        builder.append(addListenerAndGetHtml(getEditIcon(element, markup)));
        builder.append(addListenerAndGetHtml(getCommentsIcon(element)));
        builder.append(addListenerAndGetHtml(getStarsRating(element)));
        return builder.toString();
    }

    private EditIcon getEditIcon(final IJavaElement element, final String markup) {
        final WikiEditDialog editDialog = new WikiEditDialog(getShell(), this, element, markup);
        return new EditIcon(editDialog);
    }

    private CommentsIcon getCommentsIcon(final IJavaElement element) {
        final CommentsDialog commentsDialog = new CommentsDialog(getShell(), this, element);
        return new CommentsIcon(commentsDialog);
    }

    private StarsRating getStarsRating(final IJavaElement element) {
        return new StarsRating(new StarsRatingsFeature(element, server, this));
    }
}
