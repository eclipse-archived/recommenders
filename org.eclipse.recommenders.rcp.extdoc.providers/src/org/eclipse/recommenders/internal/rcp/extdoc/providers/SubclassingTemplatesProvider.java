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
import org.eclipse.recommenders.commons.selection.JavaElementLocation;
import org.eclipse.recommenders.internal.rcp.extdoc.providers.swt.TemplateEditDialog;
import org.eclipse.recommenders.internal.rcp.extdoc.providers.swt.TextAndFeaturesLine;
import org.eclipse.recommenders.rcp.extdoc.AbstractProviderComposite;
import org.eclipse.recommenders.rcp.extdoc.IDeletionProvider;
import org.eclipse.recommenders.rcp.extdoc.SwtFactory;
import org.eclipse.recommenders.server.extdoc.SubclassingServer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public final class SubclassingTemplatesProvider extends AbstractProviderComposite implements IDeletionProvider {

    private final SubclassingServer server = new SubclassingServer();

    private Composite composite;

    @Override
    protected Control createContentControl(final Composite parent) {
        composite = SwtFactory.createGridComposite(parent, 1, 0, 11, 0, 0);
        return composite;
    }

    @Override
    public boolean isAvailableForLocation(final JavaElementLocation location) {
        return true;
    }

    @Override
    protected boolean updateContent(final IJavaElementSelection selection) {
        disposeChildren(composite);
        final IJavaElement element = selection.getJavaElement();
        if (element instanceof IType) {
            printProposals(element);
            return true;
        }
        return false;
    }

    private void printProposals(final IJavaElement element) {
        final int subclasses = 123;
        String text = "By analysing "
                + subclasses
                + " subclasses that override at least one method, the following subclassing patterns have been identified.";
        SwtFactory.createStyledText(composite, text);

        final Composite templates = SwtFactory.createGridComposite(composite, 1, 0, 12, 0, 0);

        for (int i = 0; i < 2; ++i) {
            text = "'pattern 403158' - covers approximately 29% of the examined subclasses (24 subclasses).";
            final TextAndFeaturesLine line = new TextAndFeaturesLine(templates, text, element,
                    element.getElementName(), this, server, new TemplateEditDialog(getShell()));
            line.createStyleRange(0, 16, SWT.BOLD, false, false);
            line.createStyleRange(40, 3, SWT.NORMAL, true, false);

            final Composite template = SwtFactory.createGridComposite(templates, 5, 12, 2, 12, 0);
            for (int j = 0; j < 3; ++j) {
                SwtFactory.createSquare(template);
                SwtFactory.createLabel(template, "should not", true, false, SWT.COLOR_BLACK);
                SwtFactory.createLabel(template, "override performFinish", false, true, SWT.COLOR_BLACK);
                SwtFactory.createLabel(template, "-");
                SwtFactory.createLabel(template, "~ 90%", false, false, SWT.COLOR_BLUE);
            }
        }
        composite.layout(true);
    }

    @Override
    public void requestDeletion(final Object object) {
        // TODO Auto-generated method stub
    }

}
