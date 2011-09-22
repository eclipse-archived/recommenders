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
package org.eclipse.recommenders.internal.udc.ui.wizard.uploadrequest;

/**
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API and implementation.
 */
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.IPageChangeProvider;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.recommenders.internal.udc.CompilationUnitExportJob;
import org.eclipse.recommenders.internal.udc.ExporterFactory;
import org.eclipse.recommenders.internal.udc.PreferenceUtil;
import org.eclipse.recommenders.internal.udc.ProjectProvider;
import org.eclipse.recommenders.internal.udc.UploadPreferences;
import org.eclipse.recommenders.internal.udc.depersonalizer.DelegatingDepersonalizerProvider;
import org.eclipse.recommenders.internal.udc.ui.packageselection.PackageTester;
import org.eclipse.recommenders.internal.udc.ui.wizard.DepersonalisationPage;
import org.eclipse.recommenders.internal.udc.ui.wizard.LibrariesPage;
import org.eclipse.recommenders.internal.udc.ui.wizard.PackageSelectionPage;
import org.eclipse.recommenders.internal.udc.ui.wizard.WizardWithPageChangeProvider;

public class Request4UploadWizard extends WizardWithPageChangeProvider {

    IProject[] lazyRecommendersProjects;

    public Request4UploadWizard() {
        super();
        setForcePreviousAndNextButtons(true);
    }

    SelectActionPage selectActionPage;
    PackageSelectionPage lazyPackageSelectionPage;
    DepersonalisationPage lazyDepersonalisationPage;
    LibrariesPage lazyLibrariesPage;

    @Override
    public boolean performFinish() {
        switch (selectActionPage.getAction()) {
        case UploadNever:
            dontAskAgain();
            break;
        case DontUpload:
            updateLastTimeAskedDate();
            break;
        case UploadAlways:
            dontAskAgain();
            enableAutomaticUploadPreferences();
            uploadData();
            break;
        case UploadNow:
            updateLastTimeAskedDate();
            uploadData();
            break;
        default:
            break;
        }
        return true;
    }

    private void uploadData() {
        final PackageTester packageTester = new PackageTester(getIncludeExpressions(), getExcludeExpressions());
        final DelegatingDepersonalizerProvider depersonalizerProvider = new DelegatingDepersonalizerProvider(
                getLibrariesPage(), getDepersonalisationPage());
        final CompilationUnitExportJob job = new CompilationUnitExportJob(getProjects(),
                depersonalizerProvider.getDepersonalizers(), ExporterFactory.createCompilationUnitServerExporter(),
                packageTester);
        job.schedule();
    }

    private String[] getExcludeExpressions() {
        if (lazyPackageSelectionPage != null) {
            return getPackageSelectionPage().getExcludeExpressions();
        }
        return PreferenceUtil.getExcludeExpressions();
    }

    private String[] getIncludeExpressions() {
        if (lazyPackageSelectionPage != null) {
            return getPackageSelectionPage().getIncludeExpressions();
        }
        return PreferenceUtil.getIncludeExpressions();
    }

    private void enableAutomaticUploadPreferences() {
        UploadPreferences.setUploadData(true);
    }

    private void updateLastTimeAskedDate() {
        UploadPreferences.setLastTimeOpenedUploadWizard(System.currentTimeMillis());
    }

    private void dontAskAgain() {
        UploadPreferences.setOpenUploadWizard(false);
    }

    @Override
    public void addPages() {
        addPage(selectActionPage = new SelectActionPage());
    }

    @Override
    public IWizardPage getNextPage(final IWizardPage page) {
        if (page == selectActionPage) {
            switch (selectActionPage.getAction()) {
            case UploadNow:
            case UploadAlways:
                return getLibrariesPage();
            default:
                return null;
            }

        } else if (page == lazyPackageSelectionPage) {
            return getDepersonalisationPage();
        } else if (page == lazyLibrariesPage) {
            return getPackageSelectionPage();
        }
        return null;
    }

    private LibrariesPage getLibrariesPage() {
        if (lazyLibrariesPage == null) {
            lazyLibrariesPage = new LibrariesPage();
            lazyLibrariesPage.setWizard(this);
        }
        return lazyLibrariesPage;
    }

    private DepersonalisationPage getDepersonalisationPage() {
        if (lazyDepersonalisationPage == null) {
            lazyDepersonalisationPage = new DepersonalisationPage();
            lazyDepersonalisationPage.setWizard(this);
        }
        return lazyDepersonalisationPage;
    }

    private PackageSelectionPage getPackageSelectionPage() {
        if (lazyPackageSelectionPage == null) {
            lazyPackageSelectionPage = new PackageSelectionPage();
            lazyPackageSelectionPage.setWizard(this);
        }
        return lazyPackageSelectionPage;
    }

    private IProject[] getProjects() {
        if (lazyRecommendersProjects == null) {
            lazyRecommendersProjects = new ProjectProvider().getAllRecommenderProjectsInWorkspace();
        }
        return lazyRecommendersProjects;
    }

    @Override
    public IWizardPage getPreviousPage(final IWizardPage page) {
        if (page == lazyDepersonalisationPage) {
            return getPackageSelectionPage();
        }
        if (page == lazyPackageSelectionPage) {
            return lazyLibrariesPage;
        }
        if (page == lazyLibrariesPage) {
            return selectActionPage;
        }
        return null;
    }

    @Override
    protected void initPageChangedListener(final IPageChangeProvider pageChangeProvider) {
        pageChangeProvider.addPageChangedListener(new IPageChangedListener() {
            @Override
            public void pageChanged(final PageChangedEvent event) {
                if (event.getSelectedPage().equals(lazyPackageSelectionPage)) {
                    initPackageSelectionPage();
                }
                if (event.getSelectedPage().equals(lazyLibrariesPage)) {
                    initLibrariesPage();
                }
            }
        });
    }

    protected void initLibrariesPage() {
        getLibrariesPage().setProjects(getProjects());
    }

    protected void initPackageSelectionPage() {
        getPackageSelectionPage().setProjects(getProjects());
    }

    @Override
    protected boolean canFinishWithPage(final IWizardPage page) {
        if (page == selectActionPage) {
            switch (selectActionPage.getAction()) {
            case UploadAlways:
            case UploadNow:
                return false;
            default:
                return true;
            }
        }
        return page == lazyDepersonalisationPage;
    }

}
