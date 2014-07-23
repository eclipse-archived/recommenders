/**
 * Copyright (c) 2013 Madhuranga Lakjeewa.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Madhuranga Lakjeewa - initial API and implementation.
 *    Olav Lenz - introduce ISnippetRepositoryConfiguration.
 *    Olav Lenz - add wizard support for creating snippet repositories.
 */
package org.eclipse.recommenders.internal.snipmatch.rcp;

import static org.eclipse.recommenders.internal.snipmatch.rcp.SnipmatchRcpModule.REPOSITORY_CONFIGURATION_FILE;
import static org.eclipse.recommenders.utils.Checks.cast;

import java.io.File;
import java.util.Collection;
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
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.recommenders.internal.snipmatch.rcp.Repositories.SnippetRepositoryConfigurationChangedEvent;
import org.eclipse.recommenders.rcp.model.EclipseGitSnippetRepositoryConfiguration;
import org.eclipse.recommenders.rcp.model.SnippetRepositoryConfigurations;
import org.eclipse.recommenders.snipmatch.model.SnippetRepositoryConfiguration;
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
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class SnipmatchPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    private final EventBus bus;
    private final SnippetRepositoryConfigurations configuration;
    private boolean dirty;
    private final File repositoryConfigurationFile;

    @Inject
    public SnipmatchPreferencePage(EventBus bus, SnippetRepositoryConfigurations configuration,
            @Named(REPOSITORY_CONFIGURATION_FILE) File repositoryConfigurationFile) {
        super(GRID);
        setDescription(Messages.PREFPAGE_DESCRIPTION);
        this.bus = bus;
        this.configuration = configuration;
        this.repositoryConfigurationFile = repositoryConfigurationFile;
    }

    @Override
    public void createFieldEditors() {
        ConfigurationEditor configurationEditor = new ConfigurationEditor("", //$NON-NLS-1$
                Messages.PREFPAGE_LABEL_REMOTE_SNIPPETS_REPOSITORY, getFieldEditorParent());
        addField(configurationEditor);
        dirty = false;
    }

    @Override
    public void init(IWorkbench workbench) {
        ScopedPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, Constants.BUNDLE_ID);
        setPreferenceStore(store);
    }

    private final class ConfigurationEditor extends FieldEditor {

        private CheckboxTableViewer tableViewer;

        private Composite buttonBox;
        private Button newButton;
        private Button editButton;
        private Button removeButton;

        private ConfigurationEditor(String name, String labelText, Composite parent) {
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
                        dirty = true;
                    }
                    updateButtonStatus();
                }
            });
            tableViewer.getTable().addMouseListener(new MouseAdapter() {

                @Override
                public void mouseDoubleClick(MouseEvent e) {
                    TableItem item = tableViewer.getTable().getItem(new Point(e.x, e.y));
                    if (item == null) {
                        return;
                    }

                    Rectangle bounds = item.getBounds();
                    boolean isClickOnCheckbox = e.x < bounds.x;
                    if (isClickOnCheckbox) {
                        return;
                    }

                    SnippetRepositoryConfiguration selectedConfiguration = cast(item.getData());
                    editConfiguration(selectedConfiguration);
                    updateButtonStatus();
                }
            });

            buttonBox = getButtonControl(parent);
            updateButtonStatus();
            GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(buttonBox);
        }

        private void updateButtonStatus() {
            boolean selected = tableViewer.getTable().getSelectionIndex() != -1;
            boolean editableType = getSelectedConfiguration() instanceof EclipseGitSnippetRepositoryConfiguration;
            editButton.setEnabled(selected && editableType);
            removeButton.setEnabled(selected);
        }

        private Composite getButtonControl(Composite parent) {
            Composite box = new Composite(parent, SWT.NONE);
            GridLayoutFactory.fillDefaults().applyTo(box);

            newButton = createButton(box, Messages.PREFPAGE_BUTTON_NEW);
            newButton.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    addNewConfiguration();
                    updateButtonStatus();
                }

            });

            editButton = createButton(box, Messages.PREFPAGE_BUTTON_EDIT);
            editButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    editConfiguration(getSelectedConfiguration());
                    updateButtonStatus();
                }

            });

            editButton.setEnabled(false);

            removeButton = createButton(box, Messages.PREFPAGE_BUTTON_REMOVE);
            removeButton.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    removeConfiguration(getSelectedConfiguration());
                    updateButtonStatus();
                }

            });

            return box;
        }

        private SnippetRepositoryConfiguration getSelectedConfiguration() {
            List<SnippetRepositoryConfiguration> tableInput = getTableInput();
            int index = tableViewer.getTable().getSelectionIndex();
            if (index != -1) {
                return tableInput.get(index);
            }
            return null;
        }

        protected void removeConfiguration(SnippetRepositoryConfiguration configuration) {
            List<SnippetRepositoryConfiguration> configurations = getTableInput();
            configurations.remove(configuration);
            updateTableContent(configurations);
            dirty = true;
        }

        protected void editConfiguration(SnippetRepositoryConfiguration oldConfiguration) {
            List<WizardDescriptor> suitableWizardDescriptors = WizardDescriptors.filterApplicableWizardDescriptors(
                    WizardDescriptors.loadAvailableWizards(), oldConfiguration);
            if (!suitableWizardDescriptors.isEmpty()) {

                AbstractSnippetRepositoryWizard wizard;
                if (suitableWizardDescriptors.size() == 1) {
                    wizard = Iterables.getOnlyElement(suitableWizardDescriptors).getWizard();
                    wizard.setConfiguration(oldConfiguration);
                } else {
                    wizard = new SnippetRepositoryTypeSelectionWizard(oldConfiguration);
                }

                WizardDialog dialog = new WizardDialog(this.getPage().getShell(), wizard);
                if (dialog.open() == Window.OK) {
                    List<SnippetRepositoryConfiguration> configurations = getTableInput();
                    configurations.add(configurations.indexOf(oldConfiguration), wizard.getConfiguration());
                    configurations.remove(oldConfiguration);
                    updateTableContent(configurations);
                    dirty = true;
                }
            }
        }

        private List<SnippetRepositoryConfiguration> getTableInput() {
            List<SnippetRepositoryConfiguration> configurations = cast(tableViewer.getInput());
            if (configurations == null) {
                return Lists.newArrayList();
            }
            return Lists.newArrayList(configurations);
        }

        protected void addNewConfiguration() {
            List<WizardDescriptor> availableWizards = WizardDescriptors.loadAvailableWizards();
            if (!availableWizards.isEmpty()) {
                SnippetRepositoryTypeSelectionWizard newWizard = new SnippetRepositoryTypeSelectionWizard();
                WizardDialog dialog = new WizardDialog(this.getPage().getShell(), newWizard);
                if (dialog.open() == Window.OK) {
                    List<SnippetRepositoryConfiguration> configurations = getTableInput();
                    configurations.add(newWizard.getConfiguration());
                    updateTableContent(configurations);
                    dirty = true;
                }
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
            final CheckboxTableViewer tableViewer = CheckboxTableViewer.newCheckList(parent, SWT.BORDER
                    | SWT.FULL_SELECTION);

            tableViewer.setLabelProvider(new ColumnLabelProvider() {

                @Override
                public String getText(Object element) {
                    SnippetRepositoryConfiguration config = cast(element);
                    return config.getName();
                }

                @Override
                public String getToolTipText(Object element) {
                    SnippetRepositoryConfiguration config = cast(element);
                    return config.getDescription();
                }
            });
            ColumnViewerToolTipSupport.enableFor(tableViewer);
            tableViewer.setContentProvider(new ArrayContentProvider());
            return tableViewer;
        }

        @Override
        protected void doLoad() {
            updateTableContent(Lists.newArrayList(configuration.getRepos()));
        }

        public void updateTableContent(List<SnippetRepositoryConfiguration> configurations) {
            final List<SnippetRepositoryConfiguration> oldConfigurations = getTableInput();
            Collection<SnippetRepositoryConfiguration> checkedConfigurations = Collections2.filter(configurations,
                    new Predicate<SnippetRepositoryConfiguration>() {

                @Override
                public boolean apply(SnippetRepositoryConfiguration input) {
                    if (oldConfigurations != null && oldConfigurations.contains(input)) {
                        return tableViewer.getChecked(input);
                    }
                    return input.isEnabled();
                }

            });

            tableViewer.setInput(configurations);
            tableViewer.setCheckedElements(checkedConfigurations.toArray());
        }

        @Override
        public void loadDefault() {
            super.loadDefault();
            setPresentsDefaultValue(false);
        }

        @Override
        protected void doLoadDefault() {
            updateTableContent(RepositoryConfigurations.fetchDefaultConfigurations());
            dirty = true;
        }

        @Override
        protected void doStore() {
            if (!dirty) {
                return;
            }
            List<SnippetRepositoryConfiguration> oldconfigs = getTableInput();
            List<SnippetRepositoryConfiguration> newConfigs = Lists.newArrayList();
            for (SnippetRepositoryConfiguration config : oldconfigs) {
                config.setEnabled(tableViewer.getChecked(config));
                newConfigs.add(config);
            }

            configuration.getRepos().clear();
            configuration.getRepos().addAll(newConfigs);

            RepositoryConfigurations.storeConfigurations(configuration, repositoryConfigurationFile);
            bus.post(new SnippetRepositoryConfigurationChangedEvent());
            dirty = false;
        }

        @Override
        public int getNumberOfControls() {
            return 2;
        }
    }

}
