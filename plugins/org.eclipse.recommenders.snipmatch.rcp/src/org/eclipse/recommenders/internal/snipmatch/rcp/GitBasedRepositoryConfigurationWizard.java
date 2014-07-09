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

import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.recommenders.snipmatch.Snippet;
import org.eclipse.recommenders.rcp.model.EclipseGitSnippetRepositoryConfiguration;
import org.eclipse.recommenders.rcp.model.SnipmatchRcpModelFactory;
import org.eclipse.recommenders.snipmatch.model.SnippetRepositoryConfiguration;
import org.eclipse.recommenders.utils.Checks;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.google.common.base.Strings;

public class GitBasedRepositoryConfigurationWizard extends AbstractSnippetRepositoryWizard {

    private GitBasedRepositoryConfigurationWizardPage page = new GitBasedRepositoryConfigurationWizardPage(
            Messages.WIZARD_GIT_REPOSITORY_PAGE_NAME);

    private EclipseGitSnippetRepositoryConfiguration configuration;
    private final BranchInputValidator branchInputValidator = new BranchInputValidator();

    public GitBasedRepositoryConfigurationWizard() {
        setWindowTitle(Messages.WIZARD_GIT_REPOSITORY_WINDOW_TITLE);
        page.setWizard(this);
    }

    @Override
    public boolean performFinish() {
        configuration = SnipmatchRcpModelFactory.eINSTANCE.createEclipseGitSnippetRepositoryConfiguration();
        configuration.setName(page.txtName.getText());
        configuration.setUrl(page.txtFetchUri.getText());
        configuration.setPushUrl(page.txtPushUri.getText());
        configuration.setPushBranchPrefix(page.txtPushBranchPrefix.getText());
        configuration.setEnabled(true);
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
    }

    class GitBasedRepositoryConfigurationWizardPage extends WizardPage {

        private Text txtName;
        private Text txtFetchUri;
        private Text txtPushUri;
        private Text txtPushBranchPrefix;

        protected GitBasedRepositoryConfigurationWizardPage(String pageName) {
            super(pageName);
            setTitle(Messages.WIZARD_GIT_REPOSITORY_TITLE);
            setDescription(Messages.WIZARD_GIT_REPOSITORY_DESCRIPTION);
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
                txtPushBranchPrefix.setText(configuration.getPushBranchPrefix());
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
            GridLayoutFactory.swtDefaults().margins(5, 5).numColumns(3).applyTo(group);

            Label lblPushUri = new Label(group, SWT.NONE);
            lblPushUri.setText(Messages.WIZARD_GIT_REPOSITORY_LABEL_PUSH_URL);
            txtPushUri = new Text(group, SWT.BORDER | SWT.SINGLE);
            GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(txtPushUri);
            txtPushUri.addModifyListener(new ModifyListener() {

                @Override
                public void modifyText(ModifyEvent e) {
                    updatePageComplete();
                }
            });

            Label lblPushSettingsDescription = new Label(group, SWT.NONE);
            lblPushSettingsDescription.setText(MessageFormat.format(
                    Messages.WIZARD_GIT_REPOSITORY_PUSH_SETTINGS_DESCRIPTION, Snippet.FORMAT_VERSION));
            GridDataFactory.fillDefaults().grab(true, false).span(3, 1).applyTo(lblPushSettingsDescription);

            Label lblPushBranchPrefix = new Label(group, SWT.NONE);
            lblPushBranchPrefix.setText(Messages.WIZARD_GIT_REPOSITORY_LABEL_PUSH_BRANCH_PREFIX);
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

            if (Strings.isNullOrEmpty(txtName.getText())) {
                setErrorMessage(Messages.WIZARD_GIT_REPOSITORY_ERROR_EMPTY_NAME);
            } else if (Strings.isNullOrEmpty(txtFetchUri.getText())) {
                setErrorMessage(Messages.WIZARD_GIT_REPOSITORY_ERROR_EMPTY_FETCH_URL);
            } else if (!validUriString(txtFetchUri.getText())) {
                setErrorMessage(MessageFormat.format(Messages.WIZARD_GIT_REPOSITORY_ERROR_INVALID_URL,
                        txtFetchUri.getText()));
            } else if (Strings.isNullOrEmpty(txtPushUri.getText())) {
                setErrorMessage(Messages.WIZARD_GIT_REPOSITORY_ERROR_EMPTY_PUSH_URL);
            } else if (!validUriString(txtPushUri.getText())) {
                setErrorMessage(MessageFormat.format(Messages.WIZARD_GIT_REPOSITORY_ERROR_INVALID_URL,
                        txtPushUri.getText()));
            } else if (Strings.isNullOrEmpty(txtPushBranchPrefix.getText())) {
                setErrorMessage(Messages.WIZARD_GIT_REPOSITORY_ERROR_EMPTY_BRANCH_PREFIX);
            } else if (pushBranchPrefixValid != null) {
                setErrorMessage(pushBranchPrefixValid);
            }

            setPageComplete(getErrorMessage() == null);
        }

        private boolean validUriString(String uriString) {
            try {
                new URI(uriString);
                return true;
            } catch (URISyntaxException e) {
                return false;
            }
        }

        public boolean canFinish() {
            return isPageComplete();
        }

    }

}
