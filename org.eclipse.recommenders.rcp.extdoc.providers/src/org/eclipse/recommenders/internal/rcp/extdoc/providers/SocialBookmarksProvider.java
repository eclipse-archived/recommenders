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

import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.internal.rcp.extdoc.providers.swt.ListingTable;
import org.eclipse.recommenders.internal.rcp.extdoc.providers.utils.ElementResolver;
import org.eclipse.recommenders.rcp.extdoc.AbstractTitledProvider;
import org.eclipse.recommenders.rcp.extdoc.ExtDocPlugin;
import org.eclipse.recommenders.rcp.extdoc.ProviderUiJob;
import org.eclipse.recommenders.rcp.extdoc.SwtFactory;
import org.eclipse.recommenders.server.extdoc.SocialBookmarksServer;
import org.eclipse.recommenders.server.extdoc.types.SocialBookmark;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.google.inject.Inject;

public final class SocialBookmarksProvider extends AbstractTitledProvider {

    private final SocialBookmarksServer server;

    @Inject
    public SocialBookmarksProvider(final SocialBookmarksServer server) {
        this.server = server;
    }

    @Override
    protected Composite createContentComposite(final Composite parent) {
        return SwtFactory.createGridComposite(parent, 1, 0, 5, 0, 0);
    }

    @Override
    protected boolean updateSelection(final IJavaElementSelection selection, final Composite composite) {
        new ProviderUiJob() {
            @Override
            public Composite run() {
                if (!composite.isDisposed()) {
                    updateDisplay(selection, composite);
                }
                return composite;
            }
        }.schedule();
        return true;
    }

    private void updateDisplay(final IJavaElementSelection selection, final Composite composite) {
        disposeChildren(composite);
        displayItems(selection, composite);
        displayAddControl(selection, composite);
    }

    private void displayItems(final IJavaElementSelection selection, final Composite composite) {
        SwtFactory.createLabel(composite, "The following resources on " + selection.getJavaElement().getElementName()
                + " have been provided by the community. You are welcome to add you own.");

        final ListingTable table = new ListingTable(composite, 3);
        for (final SocialBookmark bookmark : server.getBookmarks(
                ElementResolver.resolveName(selection.getJavaElement())).getBookmarks()) {
            table.startNewRow();
            SwtFactory.createLink(table, bookmark.getTitle(), null, true, new MouseListener() {
                @Override
                public void mouseUp(final MouseEvent e) {
                    Program.launch(bookmark.getUrl());
                }

                @Override
                public void mouseDown(final MouseEvent e) {
                }

                @Override
                public void mouseDoubleClick(final MouseEvent e) {
                }
            });
            table.addLabelItem(bookmark.getDescription(), false, false, SWT.COLOR_BLACK);
        }
    }

    private void displayAddControl(final IJavaElementSelection selection, final Composite composite) {
        final Composite addComposite = SwtFactory.createGridComposite(composite, 4, 6, 0, 0, 0);
        SwtFactory.createLink(addComposite, "Click here to add a new bookmark.",
                ExtDocPlugin.getIcon("eview16/add.gif"), false, new MouseListener() {
                    @Override
                    public void mouseUp(final MouseEvent e) {
                        displayAddArea(selection, composite, addComposite);
                    }

                    @Override
                    public void mouseDown(final MouseEvent e) {
                    }

                    @Override
                    public void mouseDoubleClick(final MouseEvent e) {
                    }
                });
    }

    private void displayAddArea(final IJavaElementSelection selection, final Composite composite,
            final Composite addComposite) {
        disposeChildren(addComposite);
        final Text title = SwtFactory.createText(addComposite, "Link Title", 180);
        final Text description = SwtFactory.createText(addComposite, "Link Description", 256);
        final Text url = SwtFactory.createText(addComposite, "URL", 128);
        SwtFactory.createButton(addComposite, "Add", new SelectionListener() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                addBookmark(title.getText(), description.getText(), url.getText(), selection, composite);
            }

            @Override
            public void widgetDefaultSelected(final SelectionEvent e) {
            }
        });
        layout(composite);
    }

    private void addBookmark(final String text, final String description, final String url,
            final IJavaElementSelection selection, final Composite composite) {
        final SocialBookmark bookmark = server.addBookmark(ElementResolver.resolveName(selection.getJavaElement()),
                text, description, url);
        updateDisplay(selection, composite);
        layout(composite);
    }

    private void layout(final Composite composite) {
        composite.layout(true);
        composite.getParent().getParent().layout(true);
    }
}
