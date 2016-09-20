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

import javax.inject.Inject;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.recommenders.internal.snipmatch.rcp.l10n.Messages;
import org.eclipse.recommenders.internal.snipmatch.rcp.util.RepositoryUrlValidator;
import org.eclipse.recommenders.snipmatch.ISnippetRepository;
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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

public class GitBasedRepositoryConfigurationWizard extends Wizard implements ISnippetRepositoryWizard {

    private static final List<String> REPOSITORY_OPTIONS = ImmutableList.of(
            Messages.WIZARD_GIT_REPOSITORY_OPTION_GIT_PUSH_BRANCH_PREFIX,
            Messages.WIZARD_GIT_REPOSITORY_OPTION_GERRIT_PUSH_BRANCH_PREFIX,
            Messages.WIZARD_GIT_REPOSITORY_OPTION_OTHER_PUSH_BRANCH_PREFIX);

    @VisibleForTesting
    static final String PUSH_BRANCH_PREFIX_TEXT_KEY = "push-branch-prefix-key";
    @VisibleForTesting
    static final String PUSH_BRANCH_PREFIX_TEXT_VALUE = "push-branch-prefix-value";

    private final BranchInputValidator branchInputValidator = new BranchInputValidator();
    private final Repositories repositories;

    private GitBasedRepositoryConfigurationWizardPage page = new GitBasedRepositoryConfigurationWizardPage(
            Messages.WIZARD_GIT_REPOSITORY_PAGE_NAME);
    private EclipseGitSnippetRepositoryConfiguration configuration;

    private static final List<String> PUSH_BRANCH_PREFIXES = ImmutableList.of("refs/heads", //$NON-NLS-1$
            "refs/for"); //$NON-NLS-1$

    @Inject
    public GitBasedRepositoryConfigurationWizard(Repositories repositories) {
        this.repositories = repositories;

        setWindowTitle(Messages.WIZARD_GIT_REPOSITORY_WINDOW_TITLE);
        page.setWizard(this);
        page.setImageDescriptor(imageDescriptorFromPlugin(BUNDLE_ID, WIZBAN_ADD_GIT_REPOSITORY));
    }

    @Override
    public boolean performFinish() {
        configuration = SnipmatchRcpModelFactory.eINSTANCE.createEclipseGitSnippetRepositoryConfiguration();
        configuration.setName(page.nameText.getText());
        configuration.setUrl(page.fetchUriText.getText());
        configuration.setPushUrl(page.pushUriText.getText());
        configuration.setPushBranchPrefix(page.pushBranchPrefixText.getText());
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

        private Text nameText;
        private Text fetchUriText;
        private Text pushUriText;
        private Text pushBranchPrefixText;
        private Combo pushBranchPrefixCombo;

        private String initialFetchUri;

        protected GitBasedRepositoryConfigurationWizardPage(String pageName) {
            super(pageName);
            setTitle(Messages.WIZARD_GIT_REPOSITORY_TITLE);
            setDescription(Messages.WIZARD_GIT_REPOSITORY_ADD_DESCRIPTION);
        }

        @Override
        public void createControl(Composite parent) {
            initializeDialogUnits(parent);

            Composite container = new Composite(parent, SWT.NONE);
            GridLayoutFactory.swtDefaults().numColumns(2).applyTo(container);

            Label nameLabel = new Label(container, SWT.NONE);
            nameLabel.setText(Messages.WIZARD_GIT_REPOSITORY_LABEL_NAME);

            nameText = new Text(container, SWT.BORDER | SWT.SINGLE);
            GridDataFactory.fillDefaults().grab(true, false).applyTo(nameText);
            nameText.addModifyListener(new ModifyListener() {

                @Override
                public void modifyText(ModifyEvent e) {
                    updatePageComplete();
                }
            });

            addFetchGroup(container);
            addPushGroup(container);

            if (configuration != null) {
                initialFetchUri = configuration.getUrl();
                nameText.setText(configuration.getName());
                fetchUriText.setText(initialFetchUri);
                pushUriText.setText(configuration.getPushUrl());

                String pushBranchPrefix = configuration.getPushBranchPrefix();
                if (PUSH_BRANCH_PREFIXES.contains(pushBranchPrefix)) {
                    pushBranchPrefixCombo.select(PUSH_BRANCH_PREFIXES.indexOf(pushBranchPrefix));
                    pushBranchPrefixText.setText(pushBranchPrefix);
                    pushBranchPrefixText.setEnabled(false);
                } else {
                    pushBranchPrefixCombo.select(
                            REPOSITORY_OPTIONS.indexOf(Messages.WIZARD_GIT_REPOSITORY_OPTION_OTHER_PUSH_BRANCH_PREFIX));
                    pushBranchPrefixText.setText(pushBranchPrefix);
                    pushBranchPrefixText.setEnabled(true);
                }
            } else {
                pushBranchPrefixCombo.select(0);
                pushBranchPrefixText.setText(PUSH_BRANCH_PREFIXES.get(0));
                pushBranchPrefixText.setEnabled(false);
            }

            nameText.forceFocus();

            setControl(container);
            Dialog.applyDialogFont(container);

            updatePageComplete();
        }

        private void addFetchGroup(Composite parent) {
            Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
            group.setText(Messages.WIZARD_GIT_REPOSITORY_GROUP_FETCH_SETTINGS);
            GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(group);
            GridLayoutFactory.swtDefaults().margins(5, 5).numColumns(2).applyTo(group);

            Label fetchUriLabel = new Label(group, SWT.NONE);
            fetchUriLabel.setText(Messages.WIZARD_GIT_REPOSITORY_LABEL_FETCH_URL);

            fetchUriText = new Text(group, SWT.BORDER | SWT.SINGLE);
            GridDataFactory.fillDefaults().grab(true, false).applyTo(fetchUriText);
            fetchUriText.addModifyListener(new ModifyListener() {

                @Override
                public void modifyText(ModifyEvent e) {
                    updatePageComplete();
                }
            });
        }

        private void addPushGroup(Composite parent) {
            Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
            group.setText(Messages.WIZARD_GIT_REPOSITORY_GROUP_PUSH_SETTINGS);
            GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(group);
            GridLayoutFactory.swtDefaults().margins(5, 5).numColumns(4).applyTo(group);

            Label pushUriLabel = new Label(group, SWT.NONE);
            pushUriLabel.setText(Messages.WIZARD_GIT_REPOSITORY_LABEL_PUSH_URL);

            pushUriText = new Text(group, SWT.BORDER | SWT.SINGLE);
            GridDataFactory.fillDefaults().grab(true, false).span(3, 1).applyTo(pushUriText);
            pushUriText.addModifyListener(new ModifyListener() {

                @Override
                public void modifyText(ModifyEvent e) {
                    updatePageComplete();
                }
            });

            Label pushSettingsDescriptionLabel = new Label(group, SWT.NONE | SWT.WRAP);
            GridDataFactory.swtDefaults().span(4, 1).align(SWT.FILL, SWT.BEGINNING).grab(true, false)
                    .hint(convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH), SWT.DEFAULT)
                    .applyTo(pushSettingsDescriptionLabel);
            pushSettingsDescriptionLabel.setText(MessageFormat
                    .format(Messages.WIZARD_GIT_REPOSITORY_PUSH_SETTINGS_DESCRIPTION, Snippet.FORMAT_VERSION));

            Label pushBranchPrefixLabel = new Label(group, SWT.NONE);
            pushBranchPrefixLabel.setText(Messages.WIZARD_GIT_REPOSITORY_LABEL_PUSH_BRANCH_PREFIX);

            pushBranchPrefixCombo = new Combo(group, SWT.DROP_DOWN | SWT.READ_ONLY);
            pushBranchPrefixCombo.setItems(REPOSITORY_OPTIONS.toArray(new String[REPOSITORY_OPTIONS.size()]));
            GridDataFactory.fillDefaults().grab(true, false).applyTo(pushBranchPrefixCombo);

            pushBranchPrefixCombo.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (Messages.WIZARD_GIT_REPOSITORY_OPTION_OTHER_PUSH_BRANCH_PREFIX
                            .equals(pushBranchPrefixCombo.getText())) {
                        pushBranchPrefixText.setText(""); //$NON-NLS-1$
                        pushBranchPrefixText.setEnabled(true);
                    } else {
                        pushBranchPrefixText
                                .setText(PUSH_BRANCH_PREFIXES.get(pushBranchPrefixCombo.getSelectionIndex()));
                        pushBranchPrefixText.setEnabled(false);
                    }
                }
            });

            pushBranchPrefixText = new Text(group, SWT.BORDER | SWT.SINGLE);
            pushBranchPrefixText.setData(PUSH_BRANCH_PREFIX_TEXT_KEY, PUSH_BRANCH_PREFIX_TEXT_VALUE);
            GridDataFactory.fillDefaults().grab(true, false).applyTo(pushBranchPrefixText);
            pushBranchPrefixText.addModifyListener(new ModifyListener() {

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

            String fetchUri = fetchUriText.getText();
            String pushUri = pushUriText.getText();
            String pushBranchPrefix = pushBranchPrefixText.getText();

            IStatus fetchUriValidation = RepositoryUrlValidator.isValidUri(fetchUri);
            IStatus pushUriValidation = RepositoryUrlValidator.isValidUri(pushUri);
            String pushBranchPrefixValid = branchInputValidator.isValid(pushBranchPrefix);

            if (Strings.isNullOrEmpty(nameText.getText())) {
                setErrorMessage(Messages.WIZARD_GIT_REPOSITORY_ERROR_EMPTY_NAME);
            } else if (!fetchUriValidation.isOK()) {
                setErrorMessage(MessageFormat.format(Messages.WIZARD_GIT_REPOSITORY_ERROR_INVALID_FETCH_URI,
                        fetchUriValidation.getMessage()));
            } else if (!fetchUri.equals(initialFetchUri) && isUriAlreadyAdded(fetchUri)) {
                setErrorMessage(Messages.WIZARD_GIT_REPOSITORY_FETCH_URI_ALREADY_ADDED);
            } else if (!pushUriValidation.isOK()) {
                setErrorMessage(MessageFormat.format(Messages.WIZARD_GIT_REPOSITORY_ERROR_INVALID_PUSH_URI,
                        pushUriValidation.getMessage()));
            } else if (pushBranchPrefixCombo.getSelectionIndex() == -1) {
                setErrorMessage(Messages.WIZARD_GIT_REPOSITORY_ERROR_EMPTY_BRANCH_PREFIX_REPOSITORY);
            } else if (Strings.isNullOrEmpty(pushBranchPrefix)) {
                setErrorMessage(Messages.WIZARD_GIT_REPOSITORY_ERROR_EMPTY_BRANCH_PREFIX);
            } else if (pushBranchPrefixValid != null) {
                setErrorMessage(pushBranchPrefixValid);
            }

            setPageComplete(getErrorMessage() == null);
        }

        private boolean isUriAlreadyAdded(String newText) {
            String mangledNewText = Urls.mangle(newText);

            for (ISnippetRepository repository : repositories.getRepositories()) {
                String fetchUrl = repository.getRepositoryLocation();

                if (Urls.mangle(fetchUrl).equals(mangledNewText)) {
                    return true;
                }
            }

            return false;
        }

        public boolean canFinish() {
            return isPageComplete();
        }
    }
}
