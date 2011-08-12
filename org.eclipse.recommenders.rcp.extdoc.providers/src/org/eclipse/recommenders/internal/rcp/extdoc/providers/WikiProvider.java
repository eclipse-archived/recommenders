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
import org.eclipse.mylyn.wikitext.core.parser.MarkupParser;
import org.eclipse.mylyn.wikitext.mediawiki.core.MediaWikiLanguage;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.commons.selection.JavaElementLocation;
import org.eclipse.recommenders.internal.rcp.extdoc.providers.utils.ElementResolver;
import org.eclipse.recommenders.rcp.extdoc.AbstractTitledProvider;
import org.eclipse.recommenders.rcp.extdoc.ExtDocPlugin;
import org.eclipse.recommenders.rcp.extdoc.ProviderUiJob;
import org.eclipse.recommenders.rcp.extdoc.SwtFactory;
import org.eclipse.recommenders.rcp.extdoc.features.CommunityFeatures;
import org.eclipse.recommenders.server.extdoc.WikiServer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.google.inject.Inject;

public final class WikiProvider extends AbstractTitledProvider {

    private final WikiServer server;
    private final MarkupParser parser = new MarkupParser(new MediaWikiLanguage());

    @Inject
    WikiProvider(final WikiServer server) {
        this.server = server;
    }

    @Override
    protected Composite createContentComposite(final Composite parent) {
        return SwtFactory.createGridComposite(parent, 1, 0, 11, 0, 0);
    }

    @Override
    public boolean isAvailableForLocation(final JavaElementLocation location) {
        return true;
    }

    @Override
    public ProviderUiJob updateSelection(final IJavaElementSelection selection) {
        final IJavaElement element = selection.getJavaElement();
        if (element == null || element instanceof ILocalVariable || element.getElementName().isEmpty()) {
            return null;
        }
        return updateDisplay(element, server.getText(element));
    }

    private ProviderUiJob updateDisplay(final IJavaElement element, final String markup) {
        return new ProviderUiJob() {
            @Override
            public void run(final Composite composite) {
                disposeChildren(composite);
                if (markup == null) {
                    displayNoText(element, composite);
                } else {
                    displayText(element, markup, composite);
                }
            }
        };
    }

    void displayText(final IJavaElement element, final String markup, final Composite composite) {
        CommunityFeatures.create(ElementResolver.resolveName(element), null, this, server).loadStarsRatingComposite(
                composite);
        // TODO: Add editing option.

        final StyledText text = new StyledText(composite, SWT.NONE);
        // text.setText(parser.parseToHtml(markup));
        text.setText(markup);
    }

    void displayNoText(final IJavaElement element, final Composite composite) {
        String elementName = element.getElementName();
        if (element instanceof IMethod) {
            elementName = String.format("%s.%s", ((IMethod) element).getDeclaringType().getElementName(), elementName);
        }
        final StyledText text = SwtFactory.createStyledText(composite,
                String.format("Currently there is no Wiki available for %s.", elementName), SWT.COLOR_BLACK, true);
        SwtFactory.createStyleRange(text, 41, elementName.length(), SWT.NORMAL, false, true);

        SwtFactory.createCLabel(composite, "Click here to start writing.", false,
                ExtDocPlugin.getIcon("eview16/edit.png")).addMouseListener(new MouseAdapter() {
            @Override
            public void mouseUp(final MouseEvent event) {
                displayEditArea(element, composite);
            }
        });
    }

    void displayEditArea(final IJavaElement element, final Composite composite) {
        disposeChildren(composite);
        final Text text = SwtFactory.createTextArea(composite, "", 100, 0);
        SwtFactory.createButton(composite, "Save Changes", new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent event) {
                update(element, text.getText(), composite);
            }
        });
        layout(composite);
    }

    void update(final IJavaElement javaElement, final String text, final Composite composite) {
        server.setText(javaElement, text);
        disposeChildren(composite);
        displayText(javaElement, text, composite);
        layout(composite);
    }

    private static void layout(final Composite composite) {
        composite.layout(true);
        if (composite.getParent() != null) {
            composite.getParent().getParent().layout(true);
        }
    }

}
