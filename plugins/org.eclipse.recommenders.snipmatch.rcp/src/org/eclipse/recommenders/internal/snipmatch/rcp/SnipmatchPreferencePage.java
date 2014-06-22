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
 */
package org.eclipse.recommenders.internal.snipmatch.rcp;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.eclipse.recommenders.utils.Checks.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.recommenders.internal.snipmatch.rcp.Repositories.SnippetRepositoryConfigurationChangedEvent;
import org.eclipse.recommenders.snipmatch.model.snipmatchmodel.EclipseGitSnippetRepositoryConfiguration;
import org.eclipse.recommenders.snipmatch.model.snipmatchmodel.SnipmatchFactory;
import org.eclipse.recommenders.snipmatch.model.snipmatchmodel.SnippetRepositoryConfiguration;
import org.eclipse.recommenders.snipmatch.model.snipmatchmodel.SnippetRepositoryConfigurations;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

public class SnipmatchPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    private EventBus bus;
    private SnippetRepositoryConfigurations configuration;

    @Inject
    public SnipmatchPreferencePage(EventBus bus, SnippetRepositoryConfigurations configuration) {
        super(GRID);
        setDescription(Messages.PREFPAGE_DESCRIPTION);
        this.bus = bus;
        this.configuration = configuration;
    }

    @Override
    public void createFieldEditors() {
        ConfigurationEditor configurationEditor = new ConfigurationEditor("", //$NON-NLS-1$
                Messages.PREFPAGE_LABEL_REMOTE_SNIPPETS_REPOSITORY, getFieldEditorParent());
        addField(configurationEditor);
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
            SnippetRepositoryConfiguration configuration = cast(tableViewer.getElementAt(tableViewer.getTable()
                    .getSelectionIndex()));
            return configuration;
        }

        protected void removeConfiguration(SnippetRepositoryConfiguration configuration) {
            List<SnippetRepositoryConfiguration> configurations = cast(tableViewer.getInput());
            configurations.remove(configuration);
            tableViewer.setInput(configurations);
        }

        protected void editConfiguration(SnippetRepositoryConfiguration configuration) {
            ensureIsTrue(configuration instanceof EclipseGitSnippetRepositoryConfiguration);
            EclipseGitSnippetRepositoryConfiguration oldConfig = cast(configuration);

            String name = showDialogForName(oldConfig.getName());
            if (isNullOrEmpty(name)) {
                return;
            }
            String repositoryUrl = showDialogForRepositoryUrl(oldConfig.getUrl());
            if (isNullOrEmpty(repositoryUrl)) {
                return;
            }

            EclipseGitSnippetRepositoryConfiguration newConfig = SnipmatchFactory.eINSTANCE
                    .createEclipseGitSnippetRepositoryConfiguration();
            newConfig.setName(name);
            newConfig.setUrl(repositoryUrl);
            newConfig.setEnabled(true);

            List<SnippetRepositoryConfiguration> configurations = cast(tableViewer.getInput());
            configurations.remove(oldConfig);
            configurations.add(newConfig);
            updateTableContent(configurations);
        }

        private String showDialogForRepositoryUrl(String initialValue) {
            InputDialog d = new InputDialog(getShell(), Messages.DIALOG_TITLE_SET_SNIPPET_REPOSITORY_URL,
                    Messages.DIALOG_MESSAGE_SET_SNIPPET_REPOSITORY_URL, initialValue, new UriInputValidator());
            if (d.open() == Window.OK) {
                return d.getValue();
            }
            return null;
        }

        private String showDialogForName(String initialValue) {
            InputDialog d = new InputDialog(getShell(), Messages.DIALOG_TITLE_CHANGE_CONFIGURATION_NAME,
                    Messages.DIALOG_MESSAGE_CHANGE_CONFIGURATION_NAME, initialValue, null);
            if (d.open() == Window.OK) {
                return d.getValue();
            }
            return null;
        }

        protected void addNewConfiguration() {
            String name = showDialogForName(""); //$NON-NLS-1$
            if (isNullOrEmpty(name)) {
                return;
            }

            String repositoryUrl = showDialogForRepositoryUrl(""); //$NON-NLS-1$
            if (isNullOrEmpty(repositoryUrl)) {
                return;
            }

            EclipseGitSnippetRepositoryConfiguration newConfig = SnipmatchFactory.eINSTANCE
                    .createEclipseGitSnippetRepositoryConfiguration();
            newConfig.setName(name);
            newConfig.setUrl(repositoryUrl);

            newConfig.setName(name);
            newConfig.setUrl(repositoryUrl);
            newConfig.setEnabled(true);

            List<SnippetRepositoryConfiguration> configurations = cast(tableViewer.getInput());
            configurations.add(newConfig);
            tableViewer.setInput(configurations);

            for (SnippetRepositoryConfiguration config : configurations) {
                tableViewer.setChecked(config, config.isEnabled());
            }

        }

        private final class UriInputValidator implements IInputValidator {
            @Override
            public String isValid(String newText) {
                // TODO this does not support git:// urls
                try {
                    new URI(newText);
                    return null;
                } catch (URISyntaxException e) {
                    return e.getMessage();
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
            tableViewer.setContentProvider(new ArrayContentProvider() {
                @Override
                public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
                    super.inputChanged(viewer, oldInput, newInput);
                }
            });
            return tableViewer;
        }

        @Override
        protected void doLoad() {
            updateTableContent(configuration.getRepos());
        }

        public void updateTableContent(List<SnippetRepositoryConfiguration> configurations) {
            Collection<SnippetRepositoryConfiguration> checkedConfigurations = Collections2.filter(configurations,
                    new Predicate<SnippetRepositoryConfiguration>() {

                        @Override
                        public boolean apply(SnippetRepositoryConfiguration input) {
                            return input.isEnabled();
                        }

                    });

            tableViewer.setInput(configurations);
            tableViewer.setCheckedElements(checkedConfigurations.toArray());
        }

        @Override
        protected void doLoadDefault() {
            updateTableContent(RepositoryConfigurations.loadConfigurations().getRepos());
        }

        @Override
        protected void doStore() {
            List<SnippetRepositoryConfiguration> oldconfigs = cast(tableViewer.getInput());
            List<SnippetRepositoryConfiguration> newConfigs = Lists.newArrayList();
            for (SnippetRepositoryConfiguration config : oldconfigs) {
                config.setEnabled(tableViewer.getChecked(config));
                newConfigs.add(config);
            }

            configuration.getRepos().clear();
            configuration.getRepos().addAll(newConfigs);

            RepositoryConfigurations.storeConfigurations(configuration);
            bus.post(new SnippetRepositoryConfigurationChangedEvent());
        }

        @Override
        public int getNumberOfControls() {
            return 2;
        }
    }

}
