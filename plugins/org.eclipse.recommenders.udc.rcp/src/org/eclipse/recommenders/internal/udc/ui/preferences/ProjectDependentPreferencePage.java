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
import org.eclipse.jface.dialogs.IPageChangeProvider;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.recommenders.internal.udc.ProjectProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.dialogs.PreferencesUtil;

public abstract class ProjectDependentPreferencePage extends PreferencePage {

    private Link exportedProjectsLabel;

    public ProjectDependentPreferencePage() {
        super();
    }

    public ProjectDependentPreferencePage(final String title) {
        super(title);
    }

    public ProjectDependentPreferencePage(final String title, final ImageDescriptor image) {
        super(title, image);
    }

    protected void createExportedProjectsSection(final Composite composite) {
        exportedProjectsLabel = new Link(composite, SWT.WRAP);
        updateExportedProjectsText();
        exportedProjectsLabel.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                openProjectPreferences();
            }
        });

        addPageChangedListener();
    }

    private void openProjectPreferences() {
        PreferencesUtil.createPreferenceDialogOn(getShell(), ProjectPreferencePage.pageID, null, null);
    }

    protected void updateEnabledProjects() {
        setProjects(new ProjectProvider().getProjectsEnabledForExport());
    }

    private void updateExportedProjectsText() {
        final ProjectProvider provider = new ProjectProvider();
        final StringBuilder builder = new StringBuilder();
        builder.append("This page depends on the projects you want to share. <a>Shared Projects</a>: ");
        builder.append(provider.getProjectsEnabledForExport().length);
        builder.append("/");
        builder.append(provider.getAllRecommenderProjectsInWorkspace().length);
        builder.append(".");
        exportedProjectsLabel.setText(builder.toString());
    }

    protected abstract void setProjects(IProject[] projects);

    private void addPageChangedListener() {
        if (!(this.getContainer() instanceof IPageChangeProvider)) {
            return;
        }
        final IPageChangeProvider service = (IPageChangeProvider) this.getContainer();
        service.addPageChangedListener(new IPageChangedListener() {

            @Override
            public void pageChanged(final PageChangedEvent event) {
                if (event.getSelectedPage().equals(ProjectDependentPreferencePage.this)) {
                    updateExportedProjectsText();
                    updateEnabledProjects();
                }
            }
        });
    }

}