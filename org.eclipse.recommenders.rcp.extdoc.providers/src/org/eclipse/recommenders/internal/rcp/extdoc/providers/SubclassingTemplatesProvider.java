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

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.internal.rcp.extdoc.providers.swt.TemplateEditDialog;
import org.eclipse.recommenders.internal.rcp.extdoc.providers.utils.CommunityUtil;
import org.eclipse.recommenders.rcp.extdoc.AbstractBrowserProvider;
import org.eclipse.recommenders.rcp.extdoc.IDeletionProvider;
import org.eclipse.recommenders.server.extdoc.SubclassingServer;

public final class SubclassingTemplatesProvider extends AbstractBrowserProvider implements IDeletionProvider {

    private final SubclassingServer server = new SubclassingServer();

    @Override
    protected String getHtmlContent(final IJavaElementSelection selection) {
        final IJavaElement element = selection.getJavaElement();
        if (element instanceof IType) {
            return getHtmlForType((IType) element);
        } else {
            return "Subclassing templates are only available for Java types, not methods or variables.";
        }
    }

    private String getHtmlForType(final IType element) {
        final StringBuilder builder = new StringBuilder(64);

        builder.append("<p>By analysing XXX subclasses that override at least one method, the following subclassing patterns have been identified.");

        builder.append("<ol>");
        for (int i = 0; i < 2; ++i) {
            builder.append("<li><p><b>'pattern 403158'</b> - covers approximately <u>29%</u> of the examined subclasses (24 subclasses).");
            builder.append("<span>" + getCommunityFeatures(element) + "</span></p>");
            builder.append("<table>");
            for (int j = 0; j < 3; ++j) {
                builder.append("<tr><td><b>&middot;</b></td>");
                builder.append("<td><b>should not</b></td>");
                builder.append("<td>override <i>performFinish</i></td>");
                builder.append("<td>-</td>");
                builder.append("<td>~ <u>90%</u></td></tr>");
            }
            builder.append("</table></li>");
        }
        builder.append("</ol>");

        return builder.toString();
    }

    private String getCommunityFeatures(final IJavaElement element) {
        final TemplateEditDialog editDialog = new TemplateEditDialog(getShell());
        return CommunityUtil.getAllFeatures(element, this, editDialog, server);
    }

    @Override
    public void requestDeletion(final Object object) {
        // TODO Auto-generated method stub
    }

}
