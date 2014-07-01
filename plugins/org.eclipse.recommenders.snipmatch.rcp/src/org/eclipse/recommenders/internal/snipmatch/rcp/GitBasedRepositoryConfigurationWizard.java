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

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.recommenders.snipmatch.model.snipmatchmodel.EclipseGitSnippetRepositoryConfiguration;
import org.eclipse.recommenders.snipmatch.model.snipmatchmodel.SnipmatchFactory;
import org.eclipse.recommenders.snipmatch.model.snipmatchmodel.SnippetRepositoryConfiguration;
import org.eclipse.recommenders.utils.Checks;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.google.common.base.Strings;

public class GitBasedRepositoryConfigurationWizard extends AbstractSnippetRepositoryWizard {

    private GitBasedRepositoryConfigurationWizardPage page = new GitBasedRepositoryConfigurationWizardPage(
            Messages.WIZARD_GIT_REPOSITORY_PAGE_NAME);

    private EclipseGitSnippetRepositoryConfiguration configuration;

    public GitBasedRepositoryConfigurationWizard() {
        setWindowTitle(Messages.WIZARD_GIT_REPOSITORY_WINDOW_TITLE);
        page.setWizard(this);
    }

    @Override
    public boolean performFinish() {
        configuration = SnipmatchFactory.eINSTANCE.createEclipseGitSnippetRepositoryConfiguration();
        configuration.setName(page.txtName.getText());
        configuration.setUrl(page.txtUrl.getText());
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

        private Composite container;
        private Text txtName;
        private Text txtUrl;

        protected GitBasedRepositoryConfigurationWizardPage(String pageName) {
            super(pageName);
            setTitle(Messages.WIZARD_GIT_REPOSITORY_TITLE);
            setDescription(Messages.WIZARD_GIT_REPOSITORY_DESCRIPTION);
        }

        @Override
        public void createControl(Composite parent) {
            container = new Composite(parent, SWT.NONE);
            GridLayout layout = new GridLayout();
            layout.numColumns = 2;
            container.setLayout(layout);

            GridData gd = new GridData(GridData.FILL_HORIZONTAL);

            Label lblName = new Label(container, SWT.NONE);
            lblName.setText(Messages.WIZARD_GIT_REPOSITORY_LABEL_NAME);
            txtName = new Text(container, SWT.BORDER | SWT.SINGLE);
            txtName.setLayoutData(gd);
            txtName.addModifyListener(new ModifyListener() {

                @Override
                public void modifyText(ModifyEvent e) {
                    updatePageComplete();
                }
            });

            Label lblUrl = new Label(container, SWT.NONE);
            lblUrl.setText(Messages.WIZARD_GIT_REPOSITORY_LABEL_URL);
            txtUrl = new Text(container, SWT.BORDER | SWT.SINGLE);
            txtUrl.setLayoutData(gd);
            txtUrl.addModifyListener(new ModifyListener() {

                @Override
                public void modifyText(ModifyEvent e) {
                    updatePageComplete();
                }
            });

            if (configuration != null) {
                txtName.setText(configuration.getName());
                txtUrl.setText(configuration.getUrl());
            }

            txtName.forceFocus();

            setControl(container);
            updatePageComplete();
        }

        public void updatePageComplete() {
            setErrorMessage(null);
            boolean nameNotEmpty = !Strings.isNullOrEmpty(txtName.getText());
            boolean urlValid = !Strings.isNullOrEmpty(txtUrl.getText());

            try {
                new URI(txtUrl.getText());
            } catch (URISyntaxException e) {
                setErrorMessage(Messages.WARNING_INVALID_URL_FORMAT);
                urlValid = false;
            }
            setPageComplete(nameNotEmpty && urlValid);
        }

        public boolean canFinish() {
            return isPageComplete();
        }

    }

}
