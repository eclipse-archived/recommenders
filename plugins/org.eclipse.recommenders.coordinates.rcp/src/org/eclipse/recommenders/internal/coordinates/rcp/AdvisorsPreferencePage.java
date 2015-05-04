/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Olav Lenz - initial API and implementation.
 */
package org.eclipse.recommenders.internal.coordinates.rcp;

import static java.lang.Math.*;
import static org.eclipse.recommenders.utils.Checks.cast;

import java.util.List;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.recommenders.internal.coordinates.rcp.l10n.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.google.common.collect.Lists;

public class AdvisorsPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    private static final int UP = -1;
    private static final int DOWN = +1;

    public AdvisorsPreferencePage() {
        super(GRID);
    }

    @Override
    public void init(IWorkbench workbench) {
        setPreferenceStore(new ScopedPreferenceStore(InstanceScope.INSTANCE, Constants.BUNDLE_ID));
        setMessage(Messages.PREFPAGE_TITLE_ADVISORS);
        setDescription(Messages.PREFPAGE_DESCRIPTION_ADVISORS);
    }

    @Override
    protected void createFieldEditors() {
        addField(new AdvisorEditor(Constants.PREF_SORTED_ADVISOR_LIST, Messages.FIELD_LABEL_ADVISORS,
                getFieldEditorParent()));
    }

    private static final class AdvisorEditor extends FieldEditor {

        private CheckboxTableViewer tableViewer;
        private Composite buttonBox;
        private Button upButton;
        private Button downButton;

        private AdvisorEditor(String name, String labelText, Composite parent) {
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
            tableViewer.getTable().addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    updateButtonStatus();
                }
            });

            buttonBox = getButtonControl(parent);
            GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(buttonBox);
        }

        private void updateButtonStatus() {
            int selectionIndex = tableViewer.getTable().getSelectionIndex();
            upButton.setEnabled(selectionIndex != -1 && selectionIndex > 0);
            downButton.setEnabled(selectionIndex != -1 && selectionIndex < tableViewer.getTable().getItemCount() - 1);
        }

        private final class MoveSelectionListener extends SelectionAdapter {

            private final int direction;

            public MoveSelectionListener(int direction) {
                this.direction = direction;
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                List<AdvisorDescriptor> input = cast(tableViewer.getInput());
                int index = tableViewer.getTable().getSelectionIndex();
                AdvisorDescriptor movedElement = input.remove(index);
                int newIndex = min(max(0, index + direction), input.size());
                input.add(newIndex, movedElement);
                tableViewer.setInput(input);
                updateButtonStatus();
            }
        }

        private Composite getButtonControl(Composite parent) {
            Composite box = new Composite(parent, SWT.NONE);
            GridLayoutFactory.fillDefaults().applyTo(box);

            upButton = createUpDownButton(box, Messages.BUTTON_LABEL_UP, UP);
            downButton = createUpDownButton(box, Messages.BUTTON_LABEL_DOWN, DOWN);

            return box;
        }

        private Button createUpDownButton(Composite box, String text, int mode) {
            Button button = new Button(box, SWT.PUSH);
            button.setText(text);
            button.setEnabled(false);
            button.addSelectionListener(new MoveSelectionListener(mode));

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
                    AdvisorDescriptor descriptor = cast(element);
                    return descriptor.getName();
                }

                @Override
                public String getToolTipText(Object element) {
                    AdvisorDescriptor descriptor = cast(element);
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
            List<AdvisorDescriptor> input = AdvisorDescriptors.load(value, AdvisorDescriptors.getRegisteredAdvisors());
            List<AdvisorDescriptor> checkedElements = Lists.newArrayList();
            for (AdvisorDescriptor descriptor : input) {
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
            List<AdvisorDescriptor> descriptors = cast(tableViewer.getInput());
            for (AdvisorDescriptor descriptor : descriptors) {
                descriptor.setEnabled(tableViewer.getChecked(descriptor));
            }
            String newValue = AdvisorDescriptors.store(descriptors);
            getPreferenceStore().setValue(getPreferenceName(), newValue);
        }

        @Override
        public int getNumberOfControls() {
            return 2;
        }
    }
}
