/**
 * Copyright (c) 2015 Pawel Nowak.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.recommenders.internal.news.rcp;

import static org.eclipse.recommenders.utils.Checks.cast;

import java.util.List;

import javax.inject.Inject;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.recommenders.internal.news.rcp.l10n.Messages;
import org.eclipse.recommenders.news.rcp.INewsService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.google.common.collect.Lists;

public class NewsFeedPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    private final INewsService service;
    private BooleanFieldEditor enabledEditor;
    private FeedEditor feedEditor;
    private IntegerFieldEditor pollingIntervalEditor;

    @Inject
    public NewsFeedPreferencePage(INewsService service) {
        super(GRID);
        this.service = service;
    }

    @Override
    protected void createFieldEditors() {
        enabledEditor = new BooleanFieldEditor(Constants.PREF_NEWS_ENABLED, Messages.FIELD_LABEL_NEWS_ENABLED, 0,
                getFieldEditorParent());
        pollingIntervalEditor = new IntegerFieldEditor(Constants.PREF_POLLING_INTERVAL,
                Messages.FIELD_LABEL_POLLING_INTERVAL, getFieldEditorParent(), 4);
        feedEditor = new FeedEditor(Constants.PREF_FEED_LIST_SORTED, Messages.FIELD_LABEL_FEEDS,
                getFieldEditorParent());
        addField(pollingIntervalEditor);
        addField(enabledEditor);
        addField(feedEditor);

    }

    @Override
    public void init(IWorkbench workbench) {
        setPreferenceStore(new ScopedPreferenceStore(InstanceScope.INSTANCE, Constants.PLUGIN_ID));
        setMessage(Messages.PREFPAGE_TITLE);
        setDescription(Messages.PREFPAGE_DESCRIPTION);
    }

    @Override
    public boolean performOk() {
        IPreferenceStore store = getPreferenceStore();
        boolean oldValue = store.getBoolean(Constants.PREF_NEWS_ENABLED);
        boolean newValue = enabledEditor.getBooleanValue();
        List<FeedDescriptor> oldFeedValue = FeedDescriptors.load(store.getString(Constants.PREF_FEED_LIST_SORTED),
                feedEditor.getValue());
        List<FeedDescriptor> newFeedValue = feedEditor.getValue();
        boolean result = super.performOk();
        if (!oldValue && newValue) {
            // News has been activated
            service.start();
        }

        // TODO make sure preference change takes effect immediately
        // for (FeedDescriptor oldFeed : oldFeedValue) {
        // FeedDescriptor newFeed = newFeedValue.get(newFeedValue.indexOf(oldFeed));
        // if (!oldFeed.isEnabled() && newFeed.isEnabled()) {
        // service.start(newFeed);
        // }
        // if (oldFeed.isEnabled() && !newFeed.isEnabled()) {
        // service.removeFeed(newFeed);
        // }
        // }
        return result;
    }

    private static final class FeedEditor extends FieldEditor {

        private CheckboxTableViewer tableViewer;
        private Composite buttonBox;

        private FeedEditor(String name, String labelText, Composite parent) {
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
            GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).span(numColumns, 1).grab(true, false)
                    .applyTo(tableViewer.getTable());
            buttonBox = getButtonControl(parent);
            GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(buttonBox);
        }

        private Composite getButtonControl(Composite parent) {
            Composite box = new Composite(parent, SWT.NONE);
            GridLayoutFactory.fillDefaults().applyTo(box);
            return box;
        }

        private CheckboxTableViewer getTableControl(Composite parent) {
            CheckboxTableViewer tableViewer = CheckboxTableViewer.newCheckList(parent, SWT.BORDER | SWT.FULL_SELECTION);
            tableViewer.setLabelProvider(new ColumnLabelProvider() {

                @Override
                public String getText(Object element) {
                    FeedDescriptor descriptor = cast(element);
                    return descriptor.getName();
                }

                @Override
                public String getToolTipText(Object element) {
                    FeedDescriptor descriptor = cast(element);
                    return descriptor.getDescription();
                }
            });
            ColumnViewerToolTipSupport.enableFor(tableViewer);
            tableViewer.setContentProvider(new ArrayContentProvider());
            return tableViewer;
        }

        @Override
        protected void doLoad() {
            String value = getPreferenceStore().getString(getPreferenceName());
            load(value);
        }

        private void load(String value) {
            List<FeedDescriptor> input = FeedDescriptors.load(value, FeedDescriptors.getRegisteredFeeds());
            List<FeedDescriptor> checkedElements = Lists.newArrayList();
            for (FeedDescriptor descriptor : input) {
                if (descriptor.isEnabled()) {
                    checkedElements.add(descriptor);
                }
            }

            tableViewer.setInput(input);
            tableViewer.setCheckedElements(checkedElements.toArray());
        }

        @Override
        protected void doLoadDefault() {
            String value = getPreferenceStore().getDefaultString(getPreferenceName());
            load(value);
        }

        @Override
        protected void doStore() {
            List<FeedDescriptor> descriptors = cast(tableViewer.getInput());
            for (FeedDescriptor descriptor : descriptors) {
                descriptor.setEnabled(tableViewer.getChecked(descriptor));
            }
            String newValue = FeedDescriptors.store(descriptors);
            getPreferenceStore().setValue(getPreferenceName(), newValue);
        }

        @Override
        public int getNumberOfControls() {
            return 2;
        }

        public List<FeedDescriptor> getValue() {
            List<FeedDescriptor> descriptors = cast(tableViewer.getInput());
            for (FeedDescriptor descriptor : descriptors) {
                descriptor.setEnabled(tableViewer.getChecked(descriptor));
            }
            return descriptors;
        }
    }
}
