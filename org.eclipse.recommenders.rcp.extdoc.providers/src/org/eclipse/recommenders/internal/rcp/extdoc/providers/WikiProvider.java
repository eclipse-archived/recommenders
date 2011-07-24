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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.mylyn.wikitext.core.parser.MarkupParser;
import org.eclipse.mylyn.wikitext.mediawiki.core.MediaWikiLanguage;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.commons.selection.JavaElementLocation;
import org.eclipse.recommenders.internal.rcp.extdoc.providers.utils.ElementResolver;
import org.eclipse.recommenders.rcp.extdoc.AbstractProviderComposite;
import org.eclipse.recommenders.rcp.extdoc.ExtDocPlugin;
import org.eclipse.recommenders.rcp.extdoc.SwtFactory;
import org.eclipse.recommenders.rcp.extdoc.features.CommunityFeatures;
import org.eclipse.recommenders.server.extdoc.WikiServer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.progress.UIJob;

import com.google.inject.Inject;

public final class WikiProvider extends AbstractProviderComposite {

    private final WikiServer server;
    private final MarkupParser parser = new MarkupParser(new MediaWikiLanguage());

    private Composite parentComposite;
    private Composite composite;

    @Inject
    WikiProvider(final WikiServer server) {
        this.server = server;
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
        updateDisplay(element, server.getText(element));
        return true;
    }

    private void updateDisplay(final IJavaElement element, final String markup) {
        new UIJob("Updating Wiki provider") {
            @Override
            public IStatus runInUIThread(final IProgressMonitor monitor) {
                if (!parentComposite.isDisposed()) {
                    initComposite();
                    if (markup == null) {
                        displayNoText(element);
                    } else {
                        displayText(element, markup);
                    }
                    parentComposite.layout(true);
                }
                return Status.OK_STATUS;
            }
        }.schedule();
    }

    private void initComposite() {
        disposeChildren(parentComposite);
        composite = SwtFactory.createGridComposite(parentComposite, 1, 0, 11, 0, 0);
    }

    private void displayText(final IJavaElement element, final String markup) {
        CommunityFeatures.create(ElementResolver.resolveName(element), this, server)
                .loadStarsRatingComposite(composite);
        // TODO: Add editing option.

        final StyledText text = new StyledText(composite, SWT.NONE);
        // text.setText(parser.parseToHtml(markup));
        text.setText(markup);
    }

    private void displayNoText(final IJavaElement element) {
        String elementName = element.getElementName();
        if (element instanceof IMethod) {
            elementName = String.format("%s.%s", ((IMethod) element).getDeclaringType().getElementName(), elementName);
        }
        final StyledText text = SwtFactory.createStyledText(composite,
                String.format("Currently there is no Wiki available for %s.", elementName));
        SwtFactory.createStyleRange(text, 41, elementName.length(), SWT.NORMAL, false, true);

        SwtFactory.createCLabel(composite, "Click here to start writing.", false,
                ExtDocPlugin.getIcon("eview16/edit.png")).addMouseListener(new MouseListener() {
            @Override
            public void mouseDoubleClick(final MouseEvent e) {
            }

            @Override
            public void mouseDown(final MouseEvent e) {
            }

            @Override
            public void mouseUp(final MouseEvent e) {
                displayEditArea(element);
            }
        });
    }

    private void displayEditArea(final IJavaElement element) {
        initComposite();
        final Text text = SwtFactory.createText(composite, "", 100, 0);
        SwtFactory.createButton(composite, "Save Changes", new SelectionListener() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                update(element, text.getText());
            }

            @Override
            public void widgetDefaultSelected(final SelectionEvent e) {
            }
        });
        layout();
    }

    private void update(final IJavaElement javaElement, final String text) {
        server.setText(javaElement, text);
        initComposite();
        displayText(javaElement, text);
        layout();
    }

    private void layout() {
        composite.layout(true);
        parentComposite.layout(true);
        if (parentComposite.getParent() != null) {
            parentComposite.getParent().getParent().layout(true);
        }
    }

}
