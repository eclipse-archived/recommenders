/**
 * Copyright (c) 2011 Andreas Frankenberger.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API and implementation.
 */
package org.eclipse.recommenders.internal.udc.ui.preferences;

/**
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API and implementation.
 */
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.recommenders.internal.udc.Activator;
import org.eclipse.recommenders.internal.udc.PreferenceUtil;
import org.eclipse.recommenders.internal.udc.ui.packageselection.PackageSelectionController;
import org.eclipse.recommenders.internal.udc.ui.packageselection.RegExpTableController;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class PackagePreferencePage extends ProjectDependentPreferencePage implements IWorkbenchPreferencePage {
    PackageSelectionController packageSelectionController;
    PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {

        @Override
        public void propertyChange(final PropertyChangeEvent evt) {
            if (evt.getNewValue() == null) {
                setErrorMessage(null);
                setValid(true);
            } else {
                setErrorMessage("Page contains invalid expressions");
                setValid(false);
            }
        }
    };

    /**
     * @wbp.parser.constructor
     */
    public PackagePreferencePage() {
    }

    public PackagePreferencePage(final String title) {
        super(title);
    }

    public PackagePreferencePage(final String title, final ImageDescriptor image) {
        super(title, image);
    }

    @Override
    public void init(final IWorkbench workbench) {
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
    }

    @Override
    protected Control createContents(final Composite parent) {
        final Composite composite = createPreferenceComposite(parent);
        createHeaderLabel(composite);
        createExportedProjectsSection(composite);
        createPackageSelectionSection(composite);
        return composite;
    }

    private void createHeaderLabel(final Composite composite) {
        final Label label = new Label(composite, SWT.WRAP);
        label.setText("Enter the packages you want to include or exclude in the usage data upload.");
    }

    private Composite createPreferenceComposite(final Composite parent) {
        final Composite composite = new Composite(parent, SWT.NONE);
        final GridLayout gl_composite = new GridLayout();
        composite.setLayout(gl_composite);
        return composite;
    }

    private void createPackageSelectionSection(final Composite parent) {
        final Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        composite.setLayout(new FillLayout());
        packageSelectionController = new PackageSelectionController();
        packageSelectionController.createControls(composite);
        packageSelectionController.addPropertyChangeListener(RegExpTableController.validationState,
                propertyChangeListener);
        updateEnabledProjects();
    }

    @Override
    protected void performApply() {
        super.performApply();
        storePreferences();
    }

    private void storePreferences() {
        PreferenceUtil.setExpressions(packageSelectionController.getExcludeExpressions(),
                PackagePreferences.excludExpressions);
        PreferenceUtil.setExpressions(packageSelectionController.getIncludeExpressions(),
                PackagePreferences.includExpressions);
    }

    @Override
    protected void performDefaults() {
        super.performDefaults();
        packageSelectionController.setIncludeExpressions(new String[] { ".*" });
        packageSelectionController.setExcludeExpressions(new String[0]);
    }

    @Override
    public boolean performOk() {
        storePreferences();
        return super.performOk();
    }

    @Override
    protected void setProjects(final IProject[] projects) {
        packageSelectionController.setProjects(projects);
    }
}
