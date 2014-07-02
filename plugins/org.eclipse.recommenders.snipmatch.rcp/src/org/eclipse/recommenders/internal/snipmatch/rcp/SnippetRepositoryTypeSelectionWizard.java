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

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.recommenders.snipmatch.model.snipmatchmodel.SnippetRepositoryConfiguration;
import org.eclipse.recommenders.utils.Checks;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SnippetRepositoryTypeSelectionWizard extends AbstractSnippetRepositoryWizard {

    private static Logger LOG = LoggerFactory.getLogger(SnippetRepositoryTypeSelectionWizard.class);

    private java.util.List<WizardDescriptor> availableWizards;
    private AbstractSnippetRepositoryWizard selectedWizard;
    private SnippetRepositoryConfiguration configurationToEdit;
    private SnippetRepositoryTypeSelectionWizardPage page;

    public SnippetRepositoryTypeSelectionWizard() {
        setWindowTitle(Messages.WIZARD_TYPE_SELECTION_WINDOW_TITLE);
        page = new SnippetRepositoryTypeSelectionWizardPage();
        availableWizards = WizardDescriptors.loadAvailableWizards();
    }

    public SnippetRepositoryTypeSelectionWizard(SnippetRepositoryConfiguration configuration) {
        super();
        this.configurationToEdit = configuration;
        availableWizards = WizardDescriptors.filterApplicableWizardDescriptors(availableWizards, configuration);
    }

    @Override
    public boolean performFinish() {
        return false;
    }

    @Override
    public void addPages() {
        addPage(page);
        // Add a second page is necessary since otherwise no next Button is displace
        addPage(new SnippetRepositoryTypeSelectionWizardPage());
    }

    @Override
    public IWizardPage getStartingPage() {
        return page;
    }

    @Override
    public IWizardPage getNextPage(IWizardPage page) {
        selectedWizard.setConfiguration(configurationToEdit);
        return selectedWizard.getStartingPage();
    }

    public void updateSelectedWizard(IWizard wizard) {
        AbstractSnippetRepositoryWizard cast = Checks.cast(wizard);
        if (wizard != null) {
            for (IWizardPage page : wizard.getPages()) {
                addPage(page);
            }
        }
        selectedWizard = cast;
    }

    @Override
    public boolean isApplicable(SnippetRepositoryConfiguration configuration) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setConfiguration(SnippetRepositoryConfiguration configuration) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SnippetRepositoryConfiguration getConfiguration() {
        return selectedWizard.getConfiguration();
    }

    class SnippetRepositoryTypeSelectionWizardPage extends WizardPage {

        private Composite container;
        private List lstWizards;

        protected SnippetRepositoryTypeSelectionWizardPage() {
            super(Messages.WIZARD_TYPE_SELECTION_NAME);
            setTitle(Messages.WIZARD_TYPE_SELECTION_TITLE);
            setDescription(Messages.WIZARD_TYPE_SELECTION_DESCRIPTION);
        }

        @Override
        public boolean canFlipToNextPage() {
            if (lstWizards.getSelectionIndex() != -1) {
                return true;
            }
            return false;
        }

        @Override
        public void createControl(Composite parent) {
            container = new Composite(parent, SWT.NONE);
            GridLayout layout = new GridLayout();
            layout.numColumns = 1;
            container.setLayout(layout);

            GridData gd = new GridData(GridData.FILL_HORIZONTAL);

            Label lblName = new Label(container, SWT.NONE);
            lblName.setText(Messages.WIZARD_TYPE_SELECTION_LABEL_WIZARDS);
            lblName.setLayoutData(gd);

            lstWizards = new List(container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE);
            for (WizardDescriptor wizardDescriptor : availableWizards) {
                lstWizards.add(wizardDescriptor.getName());
            }
            gd = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
            lstWizards.setLayoutData(gd);

            lstWizards.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    int selectionIndex = lstWizards.getSelectionIndex();
                    if (selectionIndex == -1) {
                        updateSelectedWizard(null);
                        setPageComplete(false);
                    } else {
                        WizardDescriptor wizardDescriptor = Checks.cast(availableWizards.get(selectionIndex));
                        updateSelectedWizard(wizardDescriptor.getWizard());
                        setPageComplete(true);
                    }
                }

            });

            lstWizards.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseDoubleClick(MouseEvent e) {
                    try {
                        int index = e.y / lstWizards.getItemHeight();
                        if (index < lstWizards.getItemCount()) {
                            if (canFlipToNextPage()) {
                                getContainer().showPage(getNextPage());
                            }
                        }
                    } catch (ArithmeticException ae) {
                        LOG.error("Open wizard on double click failed because height of list items is zero.", ae); //$NON-NLS-1$
                    }
                }

            });

            setControl(container);
            setPageComplete(false);
        }
    }

}
