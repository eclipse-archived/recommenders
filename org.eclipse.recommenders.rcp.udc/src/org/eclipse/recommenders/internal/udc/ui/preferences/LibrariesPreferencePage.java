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

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.recommenders.internal.udc.PreferenceUtil;
import org.eclipse.recommenders.internal.udc.ui.depersonalisation.ControllerFactory;
import org.eclipse.recommenders.internal.udc.ui.depersonalisation.DependencySelectionController;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class LibrariesPreferencePage extends ProjectDependentPreferencePage implements IWorkbenchPreferencePage {
    DependencySelectionController controller;

    /**
     * @wbp.parser.constructor
     */
    public LibrariesPreferencePage() {
    }

    public LibrariesPreferencePage(final String title) {
        super(title);
    }

    public LibrariesPreferencePage(final String title, final ImageDescriptor image) {
        super(title, image);
    }

    @Override
    public void init(final IWorkbench workbench) {
    }

    @Override
    protected void setProjects(final IProject[] projects) {
        controller.setProjects(projects);
    }

    @Override
    protected Control createContents(final Composite parent) {
        final Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout());
        createHeaderLabel(composite);
        createExportedProjectsSection(composite);
        createLibrariesSection(composite);
        return composite;
    }

    private void createHeaderLabel(final Composite composite) {
        final Label label = new Label(composite, SWT.WRAP);
        label.setText("The usage data being uploaded is filtered to only contain informations "
                + "using the selected libraries in list below.");
    }

    private void createLibrariesSection(final Composite parent) {
        controller = ControllerFactory.instance().createDependencySelectionController();
        final Composite composite = controller.createControls(parent);
        final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.verticalIndent = 5;
        composite.setLayoutData(gd);
        updateEnabledProjects();
        controller.selectDependenciesFromPreferences();
    }

    @Override
    protected void performApply() {
        super.performApply();
        storePreferences();
    }

    @Override
    public boolean performOk() {
        storePreferences();
        return super.performOk();
    }

    private void storePreferences() {
        PreferenceUtil.setEnabledLibraries(controller.getDependencies());
    }

}
