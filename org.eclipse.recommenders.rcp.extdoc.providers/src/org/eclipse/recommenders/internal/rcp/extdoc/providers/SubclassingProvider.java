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
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.internal.rcp.extdoc.providers.swt.TemplateEditDialog;
import org.eclipse.recommenders.rcp.extdoc.AbstractProviderComposite;
import org.eclipse.recommenders.server.extdoc.SubclassingServer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public final class SubclassingProvider extends AbstractProviderComposite {

    private final SubclassingServer server = new SubclassingServer();

    public String getHtmlContent(final IJavaElementSelection context) {
        final IJavaElement element = context.getJavaElement();
        if (element instanceof IType) {
            return getHtmlContentForType(element);
        } else if (element instanceof IMethod) {
            return getHtmlContentForMethod(element);
        }
        return "Subclassing directives are only available for Java types and methods.";
    }

    private String getHtmlContentForType(final IJavaElement element) {
        final StringBuilder builder = new StringBuilder(64);
        builder.append("<p>Based on XXX direct subclasses of <i>" + element.getElementName());
        builder.append("</i> we created the following statistics. Subclassers may consider to override the following methods.<span>");
        builder.append(getCommunityFeatures(element));

        builder.append("</span></p><table>");
        for (int i = 0; i < 3; ++i) {
            builder.append("<tr><td><b>&middot;</b></td>");
            builder.append("<td><b>should not</b></td>");
            builder.append("<td>override <i>performFinish</i></td>");
            builder.append("<td>(249 times - <u>100%</u>)</td></tr>");
        }
        builder.append("</table>");

        builder.append("<p>Subclassers may consider to call the following methods to configure instances of this class via self calls.<span>");
        builder.append(getCommunityFeatures(element));

        builder.append("</span></p><table>");
        for (int i = 0; i < 3; ++i) {
            builder.append("<tr><td><b>&middot;</b></td>");
            builder.append("<td><b>should</b></td>");
            builder.append("<td>call <i>performFinish<i></td>");
            builder.append("<td>(249 times - <u>100%</u>)</td></tr>");
        }
        builder.append("</table>");

        return builder.toString();
    }

    private String getHtmlContentForMethod(final IJavaElement element) {
        final StringBuilder builder = new StringBuilder(64);
        builder.append("<p>Subclasses of <i>" + element.getParent().getElementName()
                + "</i> typically <b>should</b> overrride this method (<u>92%</u>).");
        builder.append("When overriding subclasses <b>may</b> call the <i>super</i> implementation (<u>25%</u>).</p>");

        builder.append("<p>Based on XXX implementations of <i>"
                + element.getElementName()
                + "</i> we created the following statistics. Implementors may consider to call the following methods.</p>");

        builder.append("<table>");
        for (int i = 0; i < 3; ++i) {
            builder.append("<tr><td><b>&middot;</b></td>");
            builder.append("<td><b>should</b></td>");
            builder.append("<td>call <i>performFinish<i></td>");
            builder.append("<td>(249 times - <u>100%</u>)</td></tr>");
        }
        builder.append("</table><p style=\"margin-top:1.5em\">" + getCommunityFeatures(element) + "</p>");

        return builder.toString();
    }

    private String getCommunityFeatures(final IJavaElement element) {
        final TemplateEditDialog editDialog = new TemplateEditDialog(getShell());
        // return CommunityUtil.getAllFeatures(element, this, editDialog,
        // server);
        return null;
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
