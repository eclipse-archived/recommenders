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
import org.eclipse.recommenders.rcp.extdoc.SwtFactory;
import org.eclipse.recommenders.rcp.extdoc.features.FeaturesComposite;
import org.eclipse.recommenders.server.extdoc.SubclassingServer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public final class SubclassingProvider extends AbstractProviderComposite {

    private final SubclassingServer server = new SubclassingServer();

    private Composite parentComposite;
    private Composite composite;

    @Override
    protected Control createContentControl(final Composite parent) {
        parentComposite = SwtFactory.createGridComposite(parent, 1, 0, 8, 0, 0);
        return parentComposite;
    }

    @Override
    protected void updateContent(final IJavaElementSelection selection) {
        final IJavaElement element = selection.getJavaElement();
        if (element instanceof IType) {
            displayContentForType(element);
        } else if (element instanceof IMethod) {
            displayContentForMethod(element);
        } else {
            printUnavailable();
        }
    }

    private void displayContentForType(final IJavaElement element) {
        initComposite();

        Composite line = SwtFactory.createGridComposite(composite, 2, 10, 0, 0, 0);
        String lineText = "Based on XXX direct subclasses of " + element.getElementName()
                + " we created the following statistics. Subclassers may consider to override the following methods.";
        final StyledText styledText = SwtFactory.createStyledText(line, lineText);
        SwtFactory.createStyleRange(styledText, 34, element.getElementName().length(), SWT.NORMAL, false, true);
        FeaturesComposite.create(line, element, element.getElementName(), this, server, new TemplateEditDialog(
                getShell()));

        Composite directive = SwtFactory.createGridComposite(composite, 4, 12, 3, 15, 0);
        for (int i = 0; i < 3; ++i) {
            SwtFactory.createSquare(directive);
            SwtFactory.createLabel(directive, "should not", true, false, false);
            SwtFactory.createLabel(directive, "override performFinish", false, false, true);
            final StyledText txt = SwtFactory.createStyledText(directive, "(249 times - 62%)");
            SwtFactory.createStyleRange(txt, 13, 3, SWT.NORMAL, true, false);
        }

        line = SwtFactory.createGridComposite(composite, 2, 10, 0, 0, 0);
        lineText = "Subclassers may consider to call the following methods to configure instances of this class via self calls.";
        SwtFactory.createLabel(line, lineText, false, false, false);
        FeaturesComposite.create(line, element, element.getElementName(), this, server, new TemplateEditDialog(
                getShell()));

        directive = SwtFactory.createGridComposite(composite, 4, 12, 3, 15, 0);
        for (int i = 0; i < 3; ++i) {
            SwtFactory.createSquare(directive);
            SwtFactory.createLabel(directive, "should", true, false, false);
            SwtFactory.createLabel(directive, "call performFinish", false, false, true);
            final StyledText txt = SwtFactory.createStyledText(directive, "(249 times - 62%)");
            SwtFactory.createStyleRange(txt, 13, 3, SWT.NORMAL, true, false);
        }
        parentComposite.layout(true);
    }

    private void displayContentForMethod(final IJavaElement element) {
        initComposite();

        final StyledText styledText = SwtFactory.createStyledText(composite, "");
        styledText
                .setText("Subclasses of "
                        + element.getParent().getElementName()
                        + " typically should overrride this method (92%). When overriding subclasses may call the super implementation (25%).");
        final int length = element.getParent().getElementName().length();
        SwtFactory.createStyleRange(styledText, 14, length, SWT.NORMAL, false, true);
        SwtFactory.createStyleRange(styledText, length + 25, 6, SWT.BOLD, false, false);
        SwtFactory.createStyleRange(styledText, length + 55, 3, SWT.NORMAL, true, false);
        SwtFactory.createStyleRange(styledText, length + 88, 3, SWT.BOLD, false, false);
        SwtFactory.createStyleRange(styledText, length + 101, 5, SWT.NORMAL, false, true);
        SwtFactory.createStyleRange(styledText, length + 123, 3, SWT.NORMAL, true, false);

        final String line = "Based on XXX implementations of " + element.getElementName()
                + " we created the following statistics. Implementors may consider to call the following methods.";
        final StyledText text = SwtFactory.createStyledText(composite, line);
        SwtFactory.createStyleRange(text, 32, element.getElementName().length(), SWT.NORMAL, false, true);

        final Composite directive = SwtFactory.createGridComposite(composite, 4, 12, 3, 15, 0);
        for (int i = 0; i < 3; ++i) {
            SwtFactory.createSquare(directive);
            SwtFactory.createLabel(directive, "should", true, false, false);
            SwtFactory.createLabel(directive, "call performFinish", false, false, true);
            final StyledText txt = SwtFactory.createStyledText(directive, "(249 times - 62%)");
            SwtFactory.createStyleRange(txt, 13, 3, SWT.NORMAL, true, false);
        }

        FeaturesComposite.create(composite, element, element.getElementName(), this, server, new TemplateEditDialog(
                getShell()));
        parentComposite.layout(true);
    }

    private void printUnavailable() {
        initComposite();
        SwtFactory.createStyledText(composite, "Subclassing directives are only available for Java types and methods.");
    }

    private void initComposite() {
        if (composite != null) {
            composite.dispose();
        }
        composite = SwtFactory.createGridComposite(parentComposite, 1, 0, 12, 0, 0);
    }

}
