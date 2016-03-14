/**
 * Copyright (c) 2014 Olav Lenz.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Olav Lenz - initial API and implementation.
 */
package org.eclipse.recommenders.internal.snipmatch.rcp;

import static org.eclipse.recommenders.internal.snipmatch.rcp.Constants.*;
import static org.eclipse.ui.plugin.AbstractUIPlugin.imageDescriptorFromPlugin;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.recommenders.internal.snipmatch.rcp.l10n.Messages;
import org.eclipse.recommenders.internal.snipmatch.rcp.util.RepositoryUrlValidator;
import org.eclipse.recommenders.snipmatch.Snippet;
import org.eclipse.recommenders.snipmatch.model.SnippetRepositoryConfiguration;
import org.eclipse.recommenders.snipmatch.rcp.ISnippetRepositoryWizard;
import org.eclipse.recommenders.snipmatch.rcp.model.EclipseGitSnippetRepositoryConfiguration;
import org.eclipse.recommenders.snipmatch.rcp.model.SnipmatchRcpModelFactory;
import org.eclipse.recommenders.utils.Checks;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

public class GitBasedRepositoryConfigurationWizard extends Wizard implements ISnippetRepositoryWizard {

    private GitBasedRepositoryConfigurationWizardPage page = new GitBasedRepositoryConfigurationWizardPage(
            Messages.WIZARD_GIT_REPOSITORY_PAGE_NAME);

    private EclipseGitSnippetRepositoryConfiguration configuration;
    private final BranchInputValidator branchInputValidator = new BranchInputValidator();

    private static final List<String> REPOSITORY_OPTIONS = ImmutableList.of(
            Messages.WIZARD_GIT_REPOSITORY_OPTION_GIT_PUSH_BRANCH_PREFIX,
            Messages.WIZARD_GIT_REPOSITORY_OPTION_GERRIT_PUSH_BRANCH_PREFIX,
            Messages.WIZARD_GIT_REPOSITORY_OPTION_OTHER_PUSH_BRANCH_PREFIX);

    private static final List<String> PUSH_BRANCH_PREFIXES = ImmutableList.of("refs/heads", //$NON-NLS-1$
            "refs/for"); //$NON-NLS-1$

    public GitBasedRepositoryConfigurationWizard() {
        setWindowTitle(Messages.WIZARD_GIT_REPOSITORY_WINDOW_TITLE);
        page.setWizard(this);
        page.setImageDescriptor(imageDescriptorFromPlugin(BUNDLE_ID, WIZBAN_ADD_GIT_REPOSITORY));
    }

    @Override
    public boolean performFinish() {
        configuration = SnipmatchRcpModelFactory.eINSTANCE.createEclipseGitSnippetRepositoryConfiguration();
        configuration.setName(page.txtName.getText());
        configuration.setUrl(page.txtFetchUri.getText());
        configuration.setPushUrl(page.txtPushUri.getText());
        configuration.setPushBranchPrefix(page.txtPushBranchPrefix.getText());
        return true;
    }

    @Override
    public SnippetRepositoryConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public void addPages() {
        addPage(page);
    }

    @Override
    public IWizardPage getStartingPage() {
        return page;
    }

    @Override
    public boolean canFinish() {
        return page.canFinish();
    }

    @Override
    public boolean isApplicable(SnippetRepositoryConfiguration configuration) {
        return configuration instanceof EclipseGitSnippetRepositoryConfiguration;
    }

    @Override
    public void setConfiguration(SnippetRepositoryConfiguration configuration) {
        this.configuration = Checks.cast(configuration);
        if (configuration != null) {
            page.setImageDescriptor(imageDescriptorFromPlugin(BUNDLE_ID, WIZBAN_EDIT_GIT_REPOSITORY));
            page.setDescription(Messages.WIZARD_GIT_REPOSITORY_EDIT_DESCRIPTION);
        }
    }

    class GitBasedRepositoryConfigurationWizardPage extends WizardPage {

        private Text txtName;
        private Text txtFetchUri;
        private Text txtPushUri;
        private Text txtPushBranchPrefix;
        private Combo cmbPushBranchRepository;

        protected GitBasedRepositoryConfigurationWizardPage(String pageName) {
            super(pageName);
            setTitle(Messages.WIZARD_GIT_REPOSITORY_TITLE);
            setDescription(Messages.WIZARD_GIT_REPOSITORY_ADD_DESCRIPTION);
        }

        @Override
        public void createControl(Composite parent) {
            Composite container = new Composite(parent, SWT.NONE);
            GridLayoutFactory.swtDefaults().numColumns(3).applyTo(container);

            Label lblName = new Label(container, SWT.NONE);
            lblName.setText(Messages.WIZARD_GIT_REPOSITORY_LABEL_NAME);
            txtName = new Text(container, SWT.BORDER | SWT.SINGLE);
            GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(txtName);
            txtName.addModifyListener(new ModifyListener() {

                @Override
                public void modifyText(ModifyEvent e) {
                    updatePageComplete();
                }
            });

            addFetchGroup(container);
            addPushGroup(container);

            if (configuration != null) {
                txtName.setText(configuration.getName());
                txtFetchUri.setText(configuration.getUrl());
                txtPushUri.setText(configuration.getPushUrl());

                String pushBranchPrefix = configuration.getPushBranchPrefix();
                if (PUSH_BRANCH_PREFIXES.contains(pushBranchPrefix)) {
                    cmbPushBranchRepository.select(PUSH_BRANCH_PREFIXES.indexOf(pushBranchPrefix));
                    txtPushBranchPrefix.setText(pushBranchPrefix);
                    txtPushBranchPrefix.setEditable(false);
                } else {
                    cmbPushBranchRepository.select(
                            REPOSITORY_OPTIONS.indexOf(Messages.WIZARD_GIT_REPOSITORY_OPTION_OTHER_PUSH_BRANCH_PREFIX));
                    txtPushBranchPrefix.setText(pushBranchPrefix);
                    txtPushBranchPrefix.setEditable(true);
                }
            } else {
                cmbPushBranchRepository.select(0);
                txtPushBranchPrefix.setText(PUSH_BRANCH_PREFIXES.get(0));
                txtPushBranchPrefix.setEditable(false);
            }

            txtName.forceFocus();

            setControl(container);
            updatePageComplete();
        }

        private void addFetchGroup(Composite parent) {
            Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
            group.setText(Messages.WIZARD_GIT_REPOSITORY_GROUP_FETCH_SETTINGS);
            GridDataFactory.fillDefaults().grab(true, false).span(3, 1).applyTo(group);
            GridLayoutFactory.swtDefaults().margins(5, 5).numColumns(2).applyTo(group);

            Label lblFetchUri = new Label(group, SWT.NONE);
            lblFetchUri.setText(Messages.WIZARD_GIT_REPOSITORY_LABEL_FETCH_URL);
            txtFetchUri = new Text(group, SWT.BORDER | SWT.SINGLE);
            GridDataFactory.fillDefaults().grab(true, false).span(1, 1).applyTo(txtFetchUri);
            txtFetchUri.addModifyListener(new ModifyListener() {

                @Override
                public void modifyText(ModifyEvent e) {
                    updatePageComplete();
                }
            });

        }

        private void addPushGroup(Composite parent) {
            Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
            group.setText(Messages.WIZARD_GIT_REPOSITORY_GROUP_PUSH_SETTINGS);
            GridDataFactory.fillDefaults().grab(true, false).span(3, 1).applyTo(group);
            GridLayoutFactory.swtDefaults().margins(5, 5).numColumns(4).applyTo(group);

            Label lblPushUri = new Label(group, SWT.NONE);
            lblPushUri.setText(Messages.WIZARD_GIT_REPOSITORY_LABEL_PUSH_URL);
            txtPushUri = new Text(group, SWT.BORDER | SWT.SINGLE);
            GridDataFactory.fillDefaults().grab(true, false).span(3, 1).applyTo(txtPushUri);
            txtPushUri.addModifyListener(new ModifyListener() {

                @Override
                public void modifyText(ModifyEvent e) {
                    updatePageComplete();
                }
            });

            Label lblPushSettingsDescription = new Label(group, SWT.NONE);
            lblPushSettingsDescription.setText(MessageFormat
                    .format(Messages.WIZARD_GIT_REPOSITORY_PUSH_SETTINGS_DESCRIPTION, Snippet.FORMAT_VERSION));
            GridDataFactory.fillDefaults().grab(true, false).span(4, 1).applyTo(lblPushSettingsDescription);

            Label lblPushBranchPrefix = new Label(group, SWT.NONE);
            lblPushBranchPrefix.setText(Messages.WIZARD_GIT_REPOSITORY_LABEL_PUSH_BRANCH_PREFIX);

            cmbPushBranchRepository = new Combo(group, SWT.DROP_DOWN | SWT.READ_ONLY);
            cmbPushBranchRepository.setItems(REPOSITORY_OPTIONS.toArray(new String[REPOSITORY_OPTIONS.size()]));
            GridDataFactory.fillDefaults().grab(true, false).span(1, 1).applyTo(cmbPushBranchRepository);

            cmbPushBranchRepository.addSelectionListener(new SelectionListener() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (cmbPushBranchRepository.getText()
                            .equals(Messages.WIZARD_GIT_REPOSITORY_OPTION_OTHER_PUSH_BRANCH_PREFIX)) {
                        txtPushBranchPrefix.setText("");
                        txtPushBranchPrefix.setEditable(true);
                    } else {
                        txtPushBranchPrefix
                                .setText(PUSH_BRANCH_PREFIXES.get(cmbPushBranchRepository.getSelectionIndex()));
                        txtPushBranchPrefix.setEditable(false);
                    }
                }

                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                }
            });

            txtPushBranchPrefix = new Text(group, SWT.BORDER | SWT.SINGLE);
            GridDataFactory.fillDefaults().grab(true, false).span(1, 1).applyTo(txtPushBranchPrefix);
            txtPushBranchPrefix.addModifyListener(new ModifyListener() {

                @Override
                public void modifyText(ModifyEvent e) {
                    updatePageComplete();
                }
            });

            Label lblBranch = new Label(group, SWT.NONE);
            lblBranch.setText("/" + Snippet.FORMAT_VERSION); //$NON-NLS-1$

        }

        public void updatePageComplete() {
            setErrorMessage(null);

            String pushBranchPrefixValid = branchInputValidator.isValid(txtPushBranchPrefix.getText());
            IStatus fetchUriValidation = RepositoryUrlValidator.isValidUri(txtFetchUri.getText());
            IStatus pushUriValidation = RepositoryUrlValidator.isValidUri(txtPushUri.getText());

            if (Strings.isNullOrEmpty(txtName.getText())) {
                setErrorMessage(Messages.WIZARD_GIT_REPOSITORY_ERROR_EMPTY_NAME);
            } else if (!fetchUriValidation.isOK()) {
                setErrorMessage(MessageFormat.format(Messages.WIZARD_GIT_REPOSITORY_ERROR_INVALID_FETCH_URI,
                        fetchUriValidation.getMessage()));
            } else if (!pushUriValidation.isOK()) {
                setErrorMessage(MessageFormat.format(Messages.WIZARD_GIT_REPOSITORY_ERROR_INVALID_PUSH_URI,
                        pushUriValidation.getMessage()));
            } else if (cmbPushBranchRepository.getSelectionIndex() == -1) {
                setErrorMessage(Messages.WIZARD_GIT_REPOSITORY_ERROR_EMPTY_BRANCH_PREFIX_REPOSITORY);
            } else if (Strings.isNullOrEmpty(txtPushBranchPrefix.getText())) {
                setErrorMessage(Messages.WIZARD_GIT_REPOSITORY_ERROR_EMPTY_BRANCH_PREFIX);
            } else if (pushBranchPrefixValid != null) {
                setErrorMessage(pushBranchPrefixValid);
            }

            setPageComplete(getErrorMessage() == null);
        }

        public boolean canFinish() {
            return isPageComplete();
        }
    }
}
