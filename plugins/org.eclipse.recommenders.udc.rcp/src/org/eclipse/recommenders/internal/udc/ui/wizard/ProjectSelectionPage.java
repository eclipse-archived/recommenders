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
package org.eclipse.recommenders.internal.udc.ui.wizard;

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
import org.eclipse.recommenders.internal.udc.ui.projectselection.ProjectSelectionController;
import org.eclipse.swt.widgets.Composite;

public class ProjectSelectionPage extends WizardPageTemplate {
    public static final String PAGE_NAME = "Project Selection Page";
    private IProject[] initialSelection;
    ProjectSelectionController controller;

    @Override
    public void createControl(final Composite parent) {
        controller = new ProjectSelectionController();
        setControl(controller.createControls(parent));

        this.setDescription("Project Selection");
        this.setMessage("Select the Projects you want to export");

        if (initialSelection != null && initialSelection.length > 0) {
            controller.selectProjects(initialSelection);
        } else {
            controller.selectProjectsFromProperties();
        }

        setPageComplete(getCheckedProjects().length > 0);

        addSelectedProjectsChangedListener();
    }

    private void addSelectedProjectsChangedListener() {
        controller.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(final PropertyChangeEvent evt) {
                updatePage();
            }
        });
    }

    public IProject[] getCheckedProjects() {
        return controller.getSelectedProjects();
    }

    private void updatePage() {
        final boolean completed = getCheckedProjects().length > 0;
        setPageComplete(completed);
        if (completed) {
            setErrorMessage(null);
        } else {
            setErrorMessage("Select at least one Project");
        }

    }

    public void setInitialSelection(final IProject[] projects) {
        initialSelection = projects;
    }

    public void setOnlyNewProjectsVisible(final boolean visible) {
        controller.setOnlyNewProjectsVisible(visible);
    }

    @Override
    public String getPageTitle() {
        return "Project Selection";
    }

}
