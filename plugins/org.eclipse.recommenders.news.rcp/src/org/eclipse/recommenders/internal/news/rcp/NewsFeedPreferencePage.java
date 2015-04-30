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

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.google.common.collect.Lists;

public class NewsFeedPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public NewsFeedPreferencePage() {
        super(GRID);
    }

    @Override
    protected void createFieldEditors() {
        addField(new FeedEditor(Constants.PREF_FEED_LIST_SORTED, Messages.FIELD_LABEL_FEEDS, getFieldEditorParent()));
    }

    @Override
    public void init(IWorkbench workbench) {
        setPreferenceStore(new ScopedPreferenceStore(InstanceScope.INSTANCE, Constants.PLUGIN_ID));
        setMessage(Messages.PREFPAGE_TITLE);
        setDescription(Messages.PREFPAGE_DESCRIPTION);
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
            GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).span(numColumns - 1, 1).grab(true, false)
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
    }
}
