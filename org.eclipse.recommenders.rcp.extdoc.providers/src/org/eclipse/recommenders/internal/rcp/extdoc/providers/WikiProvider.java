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
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.commons.selection.JavaElementLocation;
import org.eclipse.recommenders.internal.rcp.extdoc.providers.swt.WikiEditDialog;
import org.eclipse.recommenders.rcp.extdoc.AbstractProviderComposite;
import org.eclipse.recommenders.rcp.extdoc.MarkupParser;
import org.eclipse.recommenders.rcp.extdoc.SwtFactory;
import org.eclipse.recommenders.rcp.extdoc.features.FeaturesComposite;
import org.eclipse.recommenders.server.extdoc.WikiServer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.google.inject.Inject;

public final class WikiProvider extends AbstractProviderComposite {

    private final WikiServer server;
    private final MarkupParser parser;

    private Composite parentComposite;
    private Composite composite;

    @Inject
    WikiProvider(final WikiServer server, final MarkupParser parser) {
        this.server = server;
        this.parser = parser;
    }

    @Override
    protected Control createContentControl(final Composite parent) {
        parentComposite = SwtFactory.createGridComposite(parent, 1, 0, 0, 0, 0);
        return parentComposite;
    }

    @Override
    public boolean isAvailableForLocation(final JavaElementLocation location) {
        return true;
    }

    @Override
    public boolean selectionChanged(final IJavaElementSelection selection) {
        final IJavaElement element = selection.getJavaElement();
        if (element == null || element instanceof ILocalVariable || element.getElementName().isEmpty()) {
            return false;
        }
        initComposite();
        final String markup = server.getText(element);
        if (markup == null) {
            displayNoText(element);
        } else {
            displayText(element, markup);
        }
        parentComposite.layout(true);
        return true;
    }

    private void initComposite() {
        disposeChildren(parentComposite);
        composite = SwtFactory.createGridComposite(parentComposite, 1, 0, 11, 0, 0);
    }

    private void displayText(final IJavaElement element, final String markup) {
        final WikiEditDialog editDialog = new WikiEditDialog(this, element, markup);
        FeaturesComposite.create(composite, element, element.getElementName(), this, server, editDialog);

        final StyledText text = new StyledText(composite, SWT.NONE);
        text.setText(parser.parseTextile(markup));
    }

    private void displayNoText(final IJavaElement element) {
        String elementName = element.getElementName();
        if (element instanceof IMethod) {
            elementName = String.format("%s.%s", ((IMethod) element).getDeclaringType().getElementName(), elementName);
        }
        final StyledText text = SwtFactory.createStyledText(composite,
                String.format("Currently there is no Wiki available for %s.", elementName));
        SwtFactory.createStyleRange(text, 41, elementName.length(), SWT.NORMAL, false, true);

        final WikiEditDialog editDialog = new WikiEditDialog(this, element, null);
        final Composite editLine = SwtFactory.createGridComposite(composite, 2, 0, 0, 0, 0);
        SwtFactory.createLabel(editLine, "You can start one by clicking on the pen icon: ");
        new FeaturesComposite(editLine).addEditIcon(editDialog);
    }

    public void update(final IJavaElement javaElement, final String text) {
        server.setText(javaElement, text);
        initComposite();
        displayText(javaElement, text);
        parentComposite.layout(true);
    }

}
