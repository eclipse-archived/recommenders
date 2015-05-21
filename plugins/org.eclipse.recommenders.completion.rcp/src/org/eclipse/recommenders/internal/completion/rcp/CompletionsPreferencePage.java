/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.completion.rcp;

import static org.eclipse.recommenders.internal.completion.rcp.Constants.PREF_SESSIONPROCESSORS;
import static org.eclipse.recommenders.utils.Checks.cast;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.recommenders.completion.rcp.processable.SessionProcessorDescriptor;
import org.eclipse.recommenders.internal.completion.rcp.l10n.Messages;
import org.eclipse.recommenders.rcp.utils.ContentAssistEnablementBlock;
import org.eclipse.recommenders.rcp.utils.Selections;
import org.eclipse.recommenders.utils.Checks;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

public class CompletionsPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    private final CompletionRcpPreferences preferences;

    @Inject
    public CompletionsPreferencePage(CompletionRcpPreferences preferences) {
        super(GRID);
        this.preferences = preferences;
    }

    @Override
    public void init(IWorkbench workbench) {
        setPreferenceStore(new ScopedPreferenceStore(InstanceScope.INSTANCE, Constants.BUNDLE_NAME));
        setMessage(Messages.PREFPAGE_TITLE_COMPLETIONS);
        setDescription(Messages.PREFPAGE_DESCRIPTION_COMPLETIONS);
    }

    @Override
    protected void createFieldEditors() {
        addField(new SessionProcessorEditor(PREF_SESSIONPROCESSORS, Messages.FIELD_LABEL_SESSION_PROCESSORS,
                getFieldEditorParent()));
        addField(new ContentAssistEnablementEditor(Constants.RECOMMENDERS_ALL_CATEGORY_ID, "enablement", //$NON-NLS-1$
                getFieldEditorParent()));
    }

    private final class SessionProcessorEditor extends FieldEditor {

        private CheckboxTableViewer tableViewer;
        private Composite buttonBox;
        private Button configureBtn;

        private SessionProcessorEditor(String name, String labelText, Composite parent) {
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
                    setPresentsDefaultValue(false);
                }
            });

            buttonBox = getButtonControl(parent);
            GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(buttonBox);
        }

        private CheckboxTableViewer getTableControl(Composite parent) {
            CheckboxTableViewer tableViewer = CheckboxTableViewer.newCheckList(parent, SWT.BORDER | SWT.FULL_SELECTION);
            tableViewer.setLabelProvider(new ColumnLabelProvider() {
                @Override
                public String getText(Object element) {
                    SessionProcessorDescriptor descriptor = cast(element);
                    return descriptor.getName();
                }

                @Override
                public String getToolTipText(Object element) {
                    SessionProcessorDescriptor descriptor = cast(element);
                    return descriptor.getDescription();
                }

                @Override
                public Image getImage(Object element) {
                    SessionProcessorDescriptor descriptor = cast(element);
                    return descriptor.getIcon();
                }
            });
            ColumnViewerToolTipSupport.enableFor(tableViewer);
            tableViewer.setContentProvider(new ArrayContentProvider());

            tableViewer.addDoubleClickListener(new IDoubleClickListener() {

                @Override
                public void doubleClick(DoubleClickEvent event) {
                    openSessionProcessorPreferencePage(event.getSelection());
                }
            });
            return tableViewer;
        }

        private Composite getButtonControl(Composite parent) {
            Composite box = new Composite(parent, SWT.NONE);
            GridLayoutFactory.fillDefaults().applyTo(box);

            configureBtn = createButton(box, Messages.BUTTON_LABEL_CONFIGURE);

            return box;
        }

        private Button createButton(Composite box, String text) {
            Button button = new Button(box, SWT.PUSH);
            button.setText(text);
            button.setEnabled(false);
            button.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    openSessionProcessorPreferencePage(tableViewer.getSelection());
                }
            });

            int widthHint = Math.max(convertHorizontalDLUsToPixels(button, IDialogConstants.BUTTON_WIDTH),
                    button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);

            GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).hint(widthHint, SWT.DEFAULT).applyTo(button);

            return button;
        }

        private void updateButtonStatus() {
            int selectionIndex = tableViewer.getTable().getSelectionIndex();
            if (selectionIndex == -1) {
                configureBtn.setEnabled(false);
                return;
            }
            IStructuredSelection selected = (IStructuredSelection) tableViewer.getSelection();
            SessionProcessorDescriptor descriptor = cast(selected.getFirstElement());
            configureBtn.setEnabled(descriptor.getPreferencePage().isPresent());
        }

        private void openSessionProcessorPreferencePage(ISelection selection) {
            Optional<SessionProcessorDescriptor> optionalDescriptor = Selections
                    .<SessionProcessorDescriptor>getFirstSelected(selection);
            if (!optionalDescriptor.isPresent()) {
                return;
            }
            SessionProcessorDescriptor descriptor = optionalDescriptor.get();
            if (!descriptor.getPreferencePage().isPresent()) {
                return;
            }
            String id = descriptor.getPreferencePage().get();
            PreferencesUtil.createPreferenceDialogOn(getShell(), id, null, null);
        }

        @Override
        protected void doLoad() {
            Set<SessionProcessorDescriptor> input = preferences.getAvailableSessionProcessors();
            List<SessionProcessorDescriptor> checkedElements = Lists.newArrayList();
            for (SessionProcessorDescriptor descriptor : input) {
                if (preferences.isEnabled(descriptor)) {
                    checkedElements.add(descriptor);
                }
            }

            tableViewer.setInput(input);
            tableViewer.setSorter(new ViewerSorter() {
                @Override
                public int compare(Viewer viewer, Object e1, Object e2) {
                    SessionProcessorDescriptor lhs = Checks.cast(e1);
                    SessionProcessorDescriptor rhs = Checks.cast(e2);
                    return lhs.getName().compareTo(rhs.getName());
                }
            });
            tableViewer.setCheckedElements(checkedElements.toArray());
        }

        @Override
        protected void doLoadDefault() {
            Set<SessionProcessorDescriptor> input = preferences.getAvailableSessionProcessors();
            List<SessionProcessorDescriptor> checkedElements = Lists.newArrayList();
            for (SessionProcessorDescriptor descriptor : input) {
                if (descriptor.isEnabledByDefault()) {
                    checkedElements.add(descriptor);
                }
            }

            tableViewer.setInput(input);
            tableViewer.setCheckedElements(checkedElements.toArray());
            updateButtonStatus();
        }

        @Override
        protected void doStore() {
            Set<SessionProcessorDescriptor> descriptors = cast(tableViewer.getInput());
            preferences.setSessionProcessorEnabled(Collections2.filter(descriptors, new EnabledPredicate(true)),
                    Collections2.filter(descriptors, new EnabledPredicate(false)));
            updateButtonStatus();
        }

        private final class EnabledPredicate implements Predicate<SessionProcessorDescriptor> {

            private final boolean enabled;

            public EnabledPredicate(boolean enabled) {
                this.enabled = enabled;
            }

            @Override
            public boolean apply(SessionProcessorDescriptor descriptor) {
                return !(enabled ^ tableViewer.getChecked(descriptor));
            }
        }

        @Override
        public int getNumberOfControls() {
            return 2;
        }
    }

    private final class ContentAssistEnablementEditor extends FieldEditor {

        public ContentAssistEnablementEditor(String recommendersAllCategoryId, String string,
                Composite fieldEditorParent) {
            super(recommendersAllCategoryId, string, fieldEditorParent);
        }

        @Override
        protected void adjustForNumColumns(int numColumns) {
            // TODO Auto-generated method stub
        }

        @Override
        protected void doFillIntoGrid(Composite parent, int numColumns) {
            ContentAssistEnablementBlock enable = new ContentAssistEnablementBlock(parent,
                    Messages.FIELD_LABEL_ENABLE_COMPLETION, Constants.RECOMMENDERS_ALL_CATEGORY_ID) {

                @Override
                protected void additionalExcludedCompletionCategoriesUpdates(final boolean isEnabled,
                        final Set<String> cats) {
                    if (isEnabled) {
                        // enable subwords - disable mylyn and jdt
                        cats.add(JDT_ALL_CATEGORY);
                        cats.add(MYLYN_ALL_CATEGORY);
                    } else {
                        // disable subwords - enable jdt -- or mylyn if installed.
                        if (isMylynInstalled()) {
                            cats.remove(MYLYN_ALL_CATEGORY);
                        } else {
                            cats.remove(JDT_ALL_CATEGORY);
                        }
                    }
                }

                @Override
                public void loadSelection() {
                    String[] excluded = PreferenceConstants.getExcludedCompletionProposalCategories();
                    boolean deactivated = ArrayUtils.contains(excluded, Constants.RECOMMENDERS_ALL_CATEGORY_ID);
                    boolean mylynActive = isMylynInstalled() && !ArrayUtils.contains(excluded, MYLYN_ALL_CATEGORY);
                    boolean jdtActive = !ArrayUtils.contains(excluded, JDT_ALL_CATEGORY);
                    enablement.setSelection(!(deactivated || mylynActive || jdtActive));
                    enablement.setToolTipText(Messages.FIELD_TOOLTIP_ENABLE_COMPLETION);
                }
            };
            enable.loadSelection();
            Link contentAssistLink = new Link(parent, SWT.NONE | SWT.WRAP);
            contentAssistLink.setLayoutData(GridDataFactory
                    .swtDefaults()
                    .span(2, 1)
                    .align(SWT.FILL, SWT.BEGINNING)
                    .grab(true, false)
                    .hint(super.convertHorizontalDLUsToPixels(contentAssistLink,
                            IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH), SWT.DEFAULT).create());
            contentAssistLink.setText(Messages.PREFPAGE_FOOTER_COMPLETIONS);
            contentAssistLink.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent event) {
                    PreferencesUtil.createPreferenceDialogOn(getShell(),
                            "org.eclipse.jdt.ui.preferences.CodeAssistPreferenceAdvanced", null, null); //$NON-NLS-1$
                }
            });
        }

        @Override
        protected void doLoad() {
            // No-op - functionality provided by ContentAssistEnablementBlock
        }

        @Override
        protected void doLoadDefault() {
            // No-op
        }

        @Override
        protected void doStore() {
            // No-op - functionality provided by ContentAssistEnablementBlock
        }

        @Override
        public int getNumberOfControls() {
            return 0;
        }
    }
}
