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
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.recommenders.internal.udc.ui.preferences.ProjectPreferenceUtil.NewProjectHandling;
import org.eclipse.recommenders.internal.udc.ui.projectselection.ProjectSelectionController;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class ProjectPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
    public static final String pageID = "org.eclipse.recommenders.udc.export.preferences.projectselection";

    private ProjectSelectionController controller;
    private Button btnEnableForExport;
    private Button btnDisableForExport;
    private Button btnAsk;

    /**
     * @wbp.parser.constructor
     */
    public ProjectPreferencePage() {
    }

    public ProjectPreferencePage(final String title) {
        super(title);
    }

    public ProjectPreferencePage(final String title, final ImageDescriptor image) {
        super(title, image);
    }

    @Override
    public void init(final IWorkbench workbench) {

    }

    @Override
    protected Control createContents(final Composite parent) {
        final Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));

        createNewProjectsGroup(composite);

        createProjectSelectionSection(composite);

        return composite;
    }

    private void createNewProjectsGroup(final Composite composite) {
        final Group grpHowToHandle = new Group(composite, SWT.NONE);
        grpHowToHandle.setLayout(new GridLayout(1, false));
        grpHowToHandle.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
        grpHowToHandle.setText("Default for new Projects");

        btnAsk = new Button(grpHowToHandle, SWT.RADIO);
        btnAsk.setText("Ask before upload");

        btnEnableForExport = new Button(grpHowToHandle, SWT.RADIO);
        btnEnableForExport.setText("Always upload");

        btnDisableForExport = new Button(grpHowToHandle, SWT.RADIO);
        btnDisableForExport.setText("Don't upload");

        setNewProjectHandling(ProjectPreferenceUtil.getNewProjectHandling());
    }

    private void createProjectSelectionSection(final Composite composite) {
        final Group enabledProjects = new Group(composite, SWT.NONE);
        enabledProjects.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        enabledProjects.setText("Shared Projects");
        enabledProjects.setLayout(new FillLayout());
        controller = new ProjectSelectionController();
        controller.createControls(enabledProjects);
        controller.selectProjectsFromProperties();
    }

    private void setNewProjectHandling(final NewProjectHandling handling) {
        btnAsk.setSelection(false);
        btnEnableForExport.setSelection(false);
        btnDisableForExport.setSelection(false);
        switch (handling) {
        case ask:
            btnAsk.setSelection(true);
            break;
        case enable:
            btnEnableForExport.setSelection(true);
            break;
        case ignore:
            btnDisableForExport.setSelection(true);
            break;
        }
    }

    private NewProjectHandling getNewProjectHandling() {
        if (btnAsk.getSelection()) {
            return NewProjectHandling.ask;
        }
        if (btnDisableForExport.getSelection()) {
            return NewProjectHandling.ignore;
        }

        return NewProjectHandling.enable;
    }

    private void savePreference() {
        ProjectPreferenceUtil.setProjectsEnabledForExport(controller.getSelectedProjects());
        ProjectPreferenceUtil.setNewProjectHandling(getNewProjectHandling());
    }

    @Override
    public boolean performOk() {
        savePreference();
        return super.performOk();
    }

    @Override
    protected void performApply() {
        savePreference();
        super.performApply();
    }
}
