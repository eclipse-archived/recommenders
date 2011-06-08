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
import org.eclipse.recommenders.internal.rcp.extdoc.providers.swt.WikiEditDialog;
import org.eclipse.recommenders.internal.rcp.extdoc.providers.utils.CommunityUtil;
import org.eclipse.recommenders.rcp.extdoc.AbstractBrowserProvider;
import org.eclipse.recommenders.rcp.extdoc.MarkupParser;
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
            txt = String.format("No Wiki available for %s", element);
        }
        return String.format("%s<br/><br/>%s", getCommunityFeatures(element, markup), txt);
    }

    public void update(final IJavaElement javaElement, final String text) {
        server.setText(javaElement, text);
        redraw();
    }

    private String getCommunityFeatures(final IJavaElement element, final String markup) {
        final WikiEditDialog editDialog = new WikiEditDialog(this, element, markup);
        return CommunityUtil.getAllFeatures(element, this, editDialog, server);
    }
}
