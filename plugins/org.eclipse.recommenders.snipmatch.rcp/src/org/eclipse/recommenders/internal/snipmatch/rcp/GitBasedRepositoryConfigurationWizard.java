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

import java.net.URI;
import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.recommenders.internal.snipmatch.rcp.l10n.Messages;
import org.eclipse.recommenders.snipmatch.Snippet;
import org.eclipse.recommenders.snipmatch.model.SnippetRepositoryConfiguration;
import org.eclipse.recommenders.snipmatch.rcp.ISnippetRepositoryWizard;
import org.eclipse.recommenders.snipmatch.rcp.model.EclipseGitSnippetRepositoryConfiguration;
import org.eclipse.recommenders.snipmatch.rcp.model.SnipmatchRcpModelFactory;
import org.eclipse.recommenders.utils.Checks;
import org.eclipse.recommenders.utils.Urls;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

public class GitBasedRepositoryConfigurationWizard extends Wizard implements ISnippetRepositoryWizard {

    private static final List<String> ACCEPTED_PROTOCOLS = ImmutableList.of("file", "git", "http", "https", "ssh"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

    private GitBasedRepositoryConfigurationWizardPage page = new GitBasedRepositoryConfigurationWizardPage(
            Messages.WIZARD_GIT_REPOSITORY_PAGE_NAME);

    private EclipseGitSnippetRepositoryConfiguration configuration;
    private final BranchInputValidator branchInputValidator = new BranchInputValidator();

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
            URI fetchUri = Urls.parseURI(txtFetchUri.getText()).orNull();
            URI pushUri = Urls.parseURI(txtPushUri.getText()).orNull();

            if (Strings.isNullOrEmpty(txtName.getText())) {
                setErrorMessage(Messages.WIZARD_GIT_REPOSITORY_ERROR_EMPTY_NAME);
            } else if (Strings.isNullOrEmpty(txtFetchUri.getText())) {
                setErrorMessage(Messages.WIZARD_GIT_REPOSITORY_ERROR_EMPTY_FETCH_URL);
            } else if (fetchUri == null) {
                setErrorMessage(MessageFormat.format(Messages.WIZARD_GIT_REPOSITORY_ERROR_INVALID_URL,
                        txtFetchUri.getText()));
            } else if (!fetchUri.isAbsolute()) {
                setErrorMessage(MessageFormat.format(Messages.WIZARD_GIT_REPOSITORY_ERROR_ABSOLUTE_URL_REQUIRED,
                        txtFetchUri.getText()));
            } else if (!Urls.isUriProtocolSupported(fetchUri, ACCEPTED_PROTOCOLS)) {
                setErrorMessage(MessageFormat.format(Messages.WIZARD_GIT_REPOSITORY_ERROR_URL_PROTOCOL_UNSUPPORTED,
                        txtFetchUri.getText(), StringUtils.join(ACCEPTED_PROTOCOLS, Messages.LIST_SEPARATOR)));
            } else if (Strings.isNullOrEmpty(txtPushUri.getText())) {
                setErrorMessage(Messages.WIZARD_GIT_REPOSITORY_ERROR_EMPTY_PUSH_URL);
            } else if (pushUri == null) {
                setErrorMessage(MessageFormat.format(Messages.WIZARD_GIT_REPOSITORY_ERROR_INVALID_URL,
                        txtPushUri.getText()));
            } else if (!pushUri.isAbsolute()) {
                setErrorMessage(MessageFormat.format(Messages.WIZARD_GIT_REPOSITORY_ERROR_ABSOLUTE_URL_REQUIRED,
                        txtPushUri.getText()));
            } else if (!Urls.isUriProtocolSupported(pushUri, ACCEPTED_PROTOCOLS)) {
                setErrorMessage(MessageFormat.format(Messages.WIZARD_GIT_REPOSITORY_ERROR_URL_PROTOCOL_UNSUPPORTED,
                        txtPushUri.getText(), StringUtils.join(ACCEPTED_PROTOCOLS, Messages.LIST_SEPARATOR)));
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
