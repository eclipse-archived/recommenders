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

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.IPageChangeProvider;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.recommenders.internal.udc.PreferenceUtil;
import org.eclipse.recommenders.internal.udc.ui.preferences.PackagePreferences;
import org.eclipse.recommenders.internal.udc.ui.preferences.ProjectPreferenceUtil;

public class PreferencesForProjectsWizard extends WizardWithPageChangeProvider {
    ProjectSelectionPage projectSelectionPage;
    PackageSelectionPage packageSelectionPage;
    DepersonalisationPage depersonalisationPage;
    LibrariesPage librariesPage;

    @Override
    public boolean performFinish() {
        ProjectPreferenceUtil.setProjectsEnabledForExport(getProjects());
        PreferenceUtil.setExpressions(packageSelectionPage.getExcludeExpressions(),
                PackagePreferences.excludExpressions);
        PreferenceUtil.setExpressions(packageSelectionPage.getIncludeExpressions(),
                PackagePreferences.includExpressions);
        PreferenceUtil.setDepersonalisationRequired(depersonalisationPage.isDepersonalisationRequired());
        PreferenceUtil.setEnabledLibraries(librariesPage.getDependencies());
        return true;
    }

    @Override
    public void addPages() {
        this.addPage(projectSelectionPage = new ProjectSelectionPage());
        this.addPage(librariesPage = new LibrariesPage());
        this.addPage(packageSelectionPage = new PackageSelectionPage());
        this.addPage(depersonalisationPage = new DepersonalisationPage());
        super.addPages();
    }

    @Override
    protected void initPageChangedListener(final IPageChangeProvider pageChangeProvider) {
        pageChangeProvider.addPageChangedListener(new IPageChangedListener() {
            @Override
            public void pageChanged(final PageChangedEvent event) {
                if (event.getSelectedPage().equals(packageSelectionPage)) {
                    initPackageSelectionPage();
                }
                if (event.getSelectedPage().equals(projectSelectionPage)) {
                    initProjectSelectionPage();
                }
                if (event.getSelectedPage().equals(librariesPage)) {
                    initLibrariesPage();
                }

            }

        });
    }

    protected void initLibrariesPage() {
        librariesPage.setProjects(getProjects());
    }

    private void initProjectSelectionPage() {
        projectSelectionPage.setOnlyNewProjectsVisible(true);
    }

    protected void initPackageSelectionPage() {
        packageSelectionPage.setProjects(getProjects());
    }

    private IProject[] getProjects() {
        return projectSelectionPage.getCheckedProjects();
    }

    @Override
    protected boolean canFinishWithPage(final IWizardPage page) {
        return page == depersonalisationPage;
    }

}
