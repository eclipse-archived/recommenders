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
package org.eclipse.recommenders.internal.extdoc.rcp.providers;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.recommenders.extdoc.rcp.AbstractTitledProvider;
import org.eclipse.recommenders.extdoc.rcp.ExtDocPlugin;
import org.eclipse.recommenders.extdoc.rcp.ProviderUiUpdateJob;
import org.eclipse.recommenders.extdoc.rcp.SwtFactory;
import org.eclipse.recommenders.extdoc.rcp.UiUtils;
import org.eclipse.recommenders.extdoc.rcp.feedback.CommunityFeedback;
import org.eclipse.recommenders.extdoc.rcp.selection.selection.IJavaElementSelection;
import org.eclipse.recommenders.extdoc.transport.SocialBookmarksServer;
import org.eclipse.recommenders.extdoc.transport.types.SocialBookmark;
import org.eclipse.recommenders.extdoc.transport.types.SocialBookmarks;
import org.eclipse.recommenders.internal.extdoc.rcp.providers.swt.TableListing;
import org.eclipse.recommenders.internal.extdoc.rcp.providers.utils.ElementResolver;
import org.eclipse.recommenders.utils.names.IName;
import org.eclipse.recommenders.utils.rcp.internal.RecommendersUtilsPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.google.inject.Inject;

public final class SocialBookmarksProvider extends AbstractTitledProvider {
    private static final Image ADD_ICON = ExtDocPlugin.getIcon("eview16/add.gif");

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
    protected ProviderUiUpdateJob updateSelection(final IJavaElementSelection selection) {
        final IName name = ElementResolver.resolveName(selection.getJavaElement());
        if (name == null) {
            return null;
        }
        final SocialBookmarks bookmarks = server.getBookmarks(name);
        // Prefetching of bookmarks and ratings before we enter the UI job.
        bookmarks.getBookmarks(name, server, this);
        return new ProviderUiUpdateJob() {
            @Override
            public void run(final Composite composite) {
                updateDisplay(selection.getJavaElement(), composite, bookmarks);
            }
        };
    }

    void updateDisplay(final IJavaElement element, final Composite composite, final SocialBookmarks bookmarks) {
        UiUtils.disposeChildren(composite);
        displayItems(element, composite, bookmarks);
        displayAddControl(element, composite, bookmarks);
    }

    private void displayItems(final IJavaElement element, final Composite composite, final SocialBookmarks bookmarks) {
        if (bookmarks.isEmpty()) {
            final String text = "Social bookmarks are collections of web resources for specific Java elements, shared by all users. You can add the first bookmark for "
                    + element.getElementName() + " by clicking on the link below.";
            SwtFactory.createLabel(composite, text, true);
        } else {
            SwtFactory.createLabel(composite, "The following resources on " + element.getElementName()
                    + " have been provided by the community. You are welcome to add your own.", true);

            final TableListing table = new TableListing(composite, 4);
            final IName elementName = ElementResolver.resolveName(element);
            for (final SocialBookmark bookmark : bookmarks.getBookmarks(elementName, server, this)) {
                displayBookmark(table, bookmark, elementName);
            }
        }
    }

    private void displayBookmark(final TableListing table, final SocialBookmark bookmark, final IName elementName) {
        table.startNewRow();
        SwtFactory.createLink(table, bookmark.getTitle(), bookmark.getUrl(), null, true, new MouseAdapter() {
            @Override
            public void mouseUp(final MouseEvent event) {
                Program.launch(bookmark.getUrl());
            }
        });
        final String url = StringUtils.abbreviate(bookmark.getUrl().replaceFirst("http://(www.)?", ""), 40);
        final StyledText urlText = SwtFactory.createStyledText(table, url, SWT.COLOR_DARK_GRAY, false);
        final int indexOfSeparator = url.indexOf('/');
        SwtFactory.createStyleRange(urlText, 0, indexOfSeparator > 0 ? indexOfSeparator : url.length(), SWT.BOLD,
                false, false);
        CommunityFeedback.create(elementName, bookmark.getUrl(), this, server, bookmark.getUserFeedback())
                .loadStarsRatingComposite(table);
    }

    void displayAddControl(final IJavaElement element, final Composite composite, final SocialBookmarks bookmarks) {
        final Composite addComposite = SwtFactory.createGridComposite(composite, 4, 6, 0, 0, 0);
        SwtFactory.createLink(addComposite, "Click here to add a new bookmark.", null, ADD_ICON, false,
                new MouseAdapter() {
                    @Override
                    public void mouseUp(final MouseEvent event) {
                        UiUtils.disposeChildren(addComposite);
                        displayAddArea(element, composite, addComposite, bookmarks);
                    }
                });
    }

    void displayAddArea(final IJavaElement element, final Composite composite, final Composite addComposite,
            final SocialBookmarks bookmarks) {
        final Text title = SwtFactory.createText(addComposite, "Link Title", 300);
        final Text url = SwtFactory.createText(addComposite, "URL", 200);
        SwtFactory.createButton(addComposite, "Add", new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent event) {
                addBookmark(title.getText(), url.getText(), element, composite, bookmarks);
            }
        });
        SwtFactory.createButton(addComposite, "Cancel", new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent event) {
                UiUtils.disposeChildren(addComposite);
                displayAddControl(element, composite, bookmarks);
                UiUtils.layoutParents(composite);
            }
        });
        UiUtils.layoutParents(composite);
    }

    void addBookmark(final String text, final String url, final IJavaElement element, final Composite composite,
            final SocialBookmarks bookmarks) {
        try {
            server.addBookmark(bookmarks, text, url);
            updateDisplay(element, composite, bookmarks);
        } catch (final IllegalArgumentException e) {
            SwtFactory.createLabel(composite, e.getMessage(), false, false, SWT.COLOR_RED, true);
            RecommendersUtilsPlugin.logError(e, "validation failed: %s", e.getMessage());
        }
        UiUtils.layoutParents(composite);
    }
}
