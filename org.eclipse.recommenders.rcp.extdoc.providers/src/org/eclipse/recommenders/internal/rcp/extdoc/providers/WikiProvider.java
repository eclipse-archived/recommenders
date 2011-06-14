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

public final class WikiProvider extends AbstractProviderComposite {

    private final WikiServer server = new WikiServer();
    private final MarkupParser parser;

    private Composite parentComposite;
    private Composite composite;

    @Inject
    WikiProvider(final MarkupParser parser) {
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
    protected boolean updateContent(final IJavaElementSelection selection) {
        final IJavaElement element = selection.getJavaElement();
        String markup = null;
        if (element != null) {
            markup = server.getText(element);
        }
        if (composite != null) {
            composite.dispose();
        }
        composite = SwtFactory.createGridComposite(parentComposite, 1, 0, 11, 0, 0);
        if (markup == null) {
            displayNoText(element);
        } else {
            displayText(element, markup);
        }
        parentComposite.layout(true);
        return true;
    }

    private void displayText(final IJavaElement element, final String markup) {
        final WikiEditDialog editDialog = new WikiEditDialog(this, element, markup);
        FeaturesComposite.create(composite, element, element.getElementName(), this, server, editDialog);

        final StyledText text = new StyledText(composite, SWT.NONE);
        text.setText(parser.parseTextile(markup));
    }

    private void displayNoText(final IJavaElement element) {
        final StyledText text = new StyledText(composite, SWT.NONE);
        text.setText(String.format("Currently there is no Wiki available for %s.", element.getElementName()));

        final WikiEditDialog editDialog = new WikiEditDialog(this, element, null);
        final Composite editLine = SwtFactory.createGridComposite(composite, 2, 0, 0, 0, 0);
        SwtFactory.createLabel(editLine, "You can start one by clicking on the pen icon: ", false, false, false);
        new FeaturesComposite(editLine).addEditIcon(editDialog);
    }

    public void update(final IJavaElement javaElement, final String text) {
        server.setText(javaElement, text);
        redraw();
    }

}
