/**
 * Copyright (c) 2015 Pawel Nowak.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.recommenders.internal.news.rcp.preferences;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.Window;
import org.eclipse.recommenders.internal.news.rcp.FeedDescriptor;
import org.eclipse.recommenders.internal.news.rcp.FeedDescriptors;
import org.eclipse.recommenders.internal.news.rcp.PreferenceConstants;
import org.eclipse.recommenders.internal.news.rcp.l10n.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TableItem;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

final class FeedListEditor extends FieldEditor {

    private CheckboxTableViewer tableViewer;

    private Composite buttonBox;
    private Button addButton;
    private Button editButton;
    private Button removeButton;

    FeedListEditor(String name, String labelText, Composite parent) {
        super(name, labelText, parent);
    }

    @Override
    protected void adjustForNumColumns(int numColumns) {
    }

    @Override
    protected void doFillIntoGrid(Composite parent, int numColumns) {
        Control control = getLabelControl(parent);
        GridDataFactory.swtDefaults().span(numColumns, 1).applyTo(control);

        tableViewer = getTableControl(parent);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).span(numColumns - 1, 1).grab(true, true)
                .applyTo(tableViewer.getTable());
        tableViewer.getTable().addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (e.detail == SWT.CHECK) {
                }
                updateButtonStatus();
            }
        });
        tableViewer.getTable().addMouseListener(new MouseAdapter() {

            @Override
            public void mouseDoubleClick(MouseEvent e) {
                tableViewer.setSelection(new StructuredSelection(FeedDescriptor.class), true);
                TableItem item = tableViewer.getTable().getItem(new Point(e.x, e.y));
                if (item == null) {
                    return;
                }

                Rectangle bounds = item.getBounds();
                boolean isClickOnCheckbox = e.x < bounds.x;
                if (isClickOnCheckbox) {
                    return;
                }
                FeedDescriptor feed = (FeedDescriptor) item.getData();
                if (!feed.isDefaultRepository()) {
                    editFeed(feed);
                }
                updateButtonStatus();
            }
        });

        buttonBox = getButtonControl(parent);
        updateButtonStatus();
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(buttonBox);
    }

    private void updateButtonStatus() {
        boolean selected = tableViewer.getTable().getSelectionIndex() != -1;
        FeedDescriptor feed = getSelectedFeed();
        if (feed == null) {
            editButton.setEnabled(false);
            removeButton.setEnabled(false);
            return;
        }
        editButton.setEnabled(selected && !feed.isDefaultRepository());
        removeButton.setEnabled(selected && !feed.isDefaultRepository());
    }

    private Composite getButtonControl(Composite parent) {
        Composite box = new Composite(parent, SWT.NONE);
        GridLayoutFactory.fillDefaults().applyTo(box);

        addButton = createButton(box, Messages.PREFPAGE_BUTTON_ADD);
        addButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                addNewFeed();
                updateButtonStatus();
            }

        });

        editButton = createButton(box, Messages.PREFPAGE_BUTTON_EDIT);
        editButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                editFeed(getSelectedFeed());
                updateButtonStatus();
            }

        });

        editButton.setEnabled(false);

        removeButton = createButton(box, Messages.PREFPAGE_BUTTON_REMOVE);
        removeButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                removeFeed(getSelectedFeed());
                updateButtonStatus();
            }

        });

        return box;
    }

    private FeedDescriptor getSelectedFeed() {
        IStructuredSelection selected = (IStructuredSelection) tableViewer.getSelection();
        return (FeedDescriptor) selected.getFirstElement();
    }

    protected void removeFeed(FeedDescriptor feed) {
        List<FeedDescriptor> feeds = getTableInput();
        feeds.remove(feed);
        updateTableContent(feeds);
    }

    protected void editFeed(FeedDescriptor oldFeed) {
        @SuppressWarnings("unchecked")
        List<FeedDescriptor> descriptors = (List<FeedDescriptor>) tableViewer.getInput();
        FeedDialog dialog = new FeedDialog(editButton.getShell(), oldFeed, descriptors);
        List<FeedDescriptor> feeds = getTableInput();
        if (dialog.open() == Window.OK) {
            int index = feeds.indexOf(oldFeed);
            feeds.remove(oldFeed);
            feeds.add(index, dialog.getFeed());
            updateTableContent(feeds);
        }
    }

    private List<FeedDescriptor> getTableInput() {
        @SuppressWarnings("unchecked")
        List<FeedDescriptor> configurations = (List<FeedDescriptor>) tableViewer.getInput();
        if (configurations == null) {
            return new ArrayList<>();
        } else {
            return new ArrayList<>(configurations);
        }
    }

    protected void addNewFeed() {
        @SuppressWarnings("unchecked")
        List<FeedDescriptor> descriptors = (List<FeedDescriptor>) tableViewer.getInput();
        FeedDialog dialog = new FeedDialog(addButton.getShell(), descriptors);
        List<FeedDescriptor> feeds = getTableInput();
        if (dialog.open() == Window.OK) {
            feeds.add(dialog.getFeed());
            updateTableContent(feeds);
        }
    }

    private Button createButton(Composite box, String text) {
        Button button = new Button(box, SWT.PUSH);
        button.setText(text);

        int widthHint = Math.max(convertHorizontalDLUsToPixels(button, IDialogConstants.BUTTON_WIDTH),
                button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);

        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).hint(widthHint, SWT.DEFAULT).applyTo(button);

        return button;
    }

    private CheckboxTableViewer getTableControl(Composite parent) {
        CheckboxTableViewer tableViewer = CheckboxTableViewer.newCheckList(parent, SWT.BORDER | SWT.FULL_SELECTION);
        tableViewer.setLabelProvider(new ColumnLabelProvider() {

            @Override
            public String getText(Object element) {
                FeedDescriptor feed = (FeedDescriptor) element;

                if (Strings.isNullOrEmpty(feed.getContributedBy())) {
                    return feed.getName();
                }

                return MessageFormat.format(Messages.FEED_CONTRIBUTED_BY, feed.getName(), feed.getContributedBy());
            }

            @Override
            public String getToolTipText(Object element) {
                FeedDescriptor feed = (FeedDescriptor) element;
                return MessageFormat.format(Messages.FEED_TOOLTIP, feed.getUri(), feed.getPollingInterval());
            }

        });
        ColumnViewerToolTipSupport.enableFor(tableViewer);
        tableViewer.setContentProvider(ArrayContentProvider.getInstance());
        return tableViewer;
    }

    @Override
    protected void doLoad() {
        String value = getPreferenceStore().getString(getPreferenceName());
        load(value, false);
    }

    private void load(String value, boolean loadDefaults) {
        List<FeedDescriptor> input = FeedDescriptors.load(value, FeedDescriptors.getRegisteredFeeds());
        if (!loadDefaults) {
            input.addAll(FeedDescriptors
                    .getFeeds(getPreferenceStore().getString(PreferenceConstants.CUSTOM_FEED_LIST_SORTED)));
        }
        List<FeedDescriptor> checkedElements = new ArrayList<>();
        for (FeedDescriptor feed : input) {
            if (feed.isEnabled()) {
                checkedElements.add(feed);
            }
        }

        tableViewer.setInput(input);
        tableViewer.setComparator(new ViewerComparator() {
            @Override
            public int compare(Viewer viewer, Object e1, Object e2) {
                String feedName = Strings.nullToEmpty(((FeedDescriptor) e1).getName());
                String otherFeedName = Strings.nullToEmpty(((FeedDescriptor) e2).getName());
                return ComparisonChain.start().compare(feedName, otherFeedName, String.CASE_INSENSITIVE_ORDER)
                        .compare(feedName, otherFeedName).result();
            }
        });
        tableViewer.setCheckedElements(checkedElements.toArray());
    }

    public void updateTableContent(List<FeedDescriptor> feeds) {
        final List<FeedDescriptor> oldFeeds = getTableInput();
        Collection<FeedDescriptor> checkedFeeds = Collections2.filter(feeds, new Predicate<FeedDescriptor>() {

            @Override
            public boolean apply(FeedDescriptor input) {
                if (oldFeeds != null && oldFeeds.contains(input)) {
                    return tableViewer.getChecked(input);
                }
                return input.isEnabled();
            }

        });

        tableViewer.setInput(feeds);
        tableViewer.setCheckedElements(checkedFeeds.toArray());
    }

    @Override
    public void loadDefault() {
        super.loadDefault();
        // Force storing of both provided and custom feeds
        setPresentsDefaultValue(false);
    }

    @Override
    protected void doLoadDefault() {
        String value = getPreferenceStore().getDefaultString(getPreferenceName());
        load(value, true);
    }

    @Override
    protected void doStore() {
        @SuppressWarnings("unchecked")
        List<FeedDescriptor> descriptors = (List<FeedDescriptor>) tableViewer.getInput();
        for (FeedDescriptor descriptor : descriptors) {
            descriptor.setEnabled(tableViewer.getChecked(descriptor));
        }
        List<FeedDescriptor> feeds = Lists
                .newArrayList(Iterables.filter(descriptors, new Predicate<FeedDescriptor>() {

                    @Override
                    public boolean apply(FeedDescriptor input) {
                        return input.isDefaultRepository();
                    }

                }));
        List<FeedDescriptor> customFeeds = Lists
                .newArrayList(Iterables.filter(descriptors, new Predicate<FeedDescriptor>() {

                    @Override
                    public boolean apply(FeedDescriptor input) {
                        return !input.isDefaultRepository();
                    }

                }));

        String newValue = FeedDescriptors.feedsToString(feeds);
        getPreferenceStore().setValue(getPreferenceName(), newValue);
        getPreferenceStore().setValue(PreferenceConstants.CUSTOM_FEED_LIST_SORTED,
                FeedDescriptors.customFeedsToString(customFeeds));
    }

    @Override
    public int getNumberOfControls() {
        return 2;
    }
}
