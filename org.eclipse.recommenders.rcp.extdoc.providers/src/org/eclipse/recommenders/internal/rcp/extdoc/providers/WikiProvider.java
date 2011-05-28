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
import org.eclipse.recommenders.commons.selection.JavaElementSelection;
import org.eclipse.recommenders.internal.rcp.extdoc.providers.swt.WikiEditDialog;
import org.eclipse.recommenders.rcp.extdoc.AbstractBrowserProvider;
import org.eclipse.recommenders.rcp.extdoc.listener.EditListener;
import org.eclipse.recommenders.rcp.extdoc.utils.MarkupParser;
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
    protected String getHtmlContent(final JavaElementSelection context) {
        String markup = null;
        String txt = null;
        if (context.getJavaElement() != null) {
            markup = server.read(context.getJavaElement());
            if (markup != null) {
                txt = parser.parseTextile(markup);
            }
        }
        if (txt == null) {
            txt = "No Wiki available for " + context.getJavaElement();
        }
        return String.format("%s<br/><br/>%s", getCommunityFeatures(context, markup), txt);
    }

    public void update(final IJavaElement javaElement, final String text) {
        server.write(javaElement, text);
        reload();
    }

    private String getCommunityFeatures(final JavaElementSelection context, final String markup) {
        final StringBuilder builder = new StringBuilder();

        builder.append(addListenerAndGetHtml(getEditListener(context, markup)));

        return builder.toString();
    }

    private EditListener getEditListener(final JavaElementSelection context, final String markup) {
        final WikiEditDialog editDialog = new WikiEditDialog(getShell(), this, context.getJavaElement(), markup);
        return new EditListener(editDialog);
    }
}
