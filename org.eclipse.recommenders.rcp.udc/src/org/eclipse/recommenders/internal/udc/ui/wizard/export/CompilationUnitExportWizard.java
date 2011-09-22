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
package org.eclipse.recommenders.internal.udc.ui.wizard.export;

/**
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API and implementation.
 */
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.IPageChangeProvider;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.recommenders.internal.udc.CompilationUnitExportJob;
import org.eclipse.recommenders.internal.udc.depersonalizer.DelegatingDepersonalizerProvider;
import org.eclipse.recommenders.internal.udc.depersonalizer.IDepersonalisationProvider;
import org.eclipse.recommenders.internal.udc.ui.packageselection.PackageTester;
import org.eclipse.recommenders.internal.udc.ui.wizard.DepersonalisationPage;
import org.eclipse.recommenders.internal.udc.ui.wizard.LibrariesPage;
import org.eclipse.recommenders.internal.udc.ui.wizard.PackageSelectionPage;
import org.eclipse.recommenders.internal.udc.ui.wizard.ProjectSelectionPage;
import org.eclipse.recommenders.internal.udc.ui.wizard.WizardWithPageChangeProvider;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;

public class CompilationUnitExportWizard extends WizardWithPageChangeProvider implements IExportWizard {
    IStructuredSelection initialSelection;
    ProjectSelectionPage projectSelectionPage = new ProjectSelectionPage();
    DepersonalisationPage depersonalisationPage = new DepersonalisationPage();
    private final ExportDestinationPage destinationPage = new ExportDestinationPage();
    private final PackageSelectionPage packageSelectionPage = new PackageSelectionPage();
    private final LibrariesPage librariesPage = new LibrariesPage();
    private final IDepersonalisationProvider depersonalisationProvider;

    public CompilationUnitExportWizard() {
        depersonalisationProvider = new DelegatingDepersonalizerProvider(librariesPage, depersonalisationPage);

    }

    @Override
    public void init(final IWorkbench workbench, final IStructuredSelection selection) {
        initialSelection = selection;
        this.setWindowTitle("Export Recommender data");

        preselectProjectsByCurrentSelection(selection);
    }

    @Override
    protected void initPageChangedListener(final IPageChangeProvider pageChangeProvider) {
        pageChangeProvider.addPageChangedListener(new IPageChangedListener() {
            @Override
            public void pageChanged(final PageChangedEvent event) {
                if (event.getSelectedPage().equals(packageSelectionPage)) {
                    initPackageSelectionPage();
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

    private IProject[] getProjects() {
        return projectSelectionPage.getCheckedProjects();
    }

    protected void initPackageSelectionPage() {
        packageSelectionPage.setProjects(getProjects());
    }

    private void preselectProjectsByCurrentSelection(final IStructuredSelection selection) {
        if (selection.getFirstElement() instanceof IResource) {
            final Set<IProject> selectedProjects = new HashSet<IProject>();
            for (final Object o : selection.toArray()) {
                selectedProjects.add(((IResource) o).getProject());
            }
            projectSelectionPage.setInitialSelection(selectedProjects.toArray(new IProject[selectedProjects.size()]));
        }
    }

    @Override
    protected boolean canFinishWithPage(final IWizardPage page) {
        if (page == depersonalisationPage) {
            return true;
        }
        if (page == destinationPage) {
            return true;
        }
        return false;
    }

    @Override
    public boolean performFinish() {
        final PackageTester packageTester = new PackageTester(packageSelectionPage.getIncludeExpressions(),
                packageSelectionPage.getExcludeExpressions());
        final ExecutorService service = Executors.newFixedThreadPool(2);
        for (final IProject project : getProjects()) {
            final CompilationUnitExportJob job = new CompilationUnitExportJob(new IProject[] { project },
                    depersonalisationProvider.getDepersonalizers(), destinationPage.getExporter(), packageTester);
            service.submit(new Runnable() {

                @Override
                public void run() {
                    job.schedule();
                    try {
                        job.join();
                    } catch (final InterruptedException e) {

                    }
                }
            });
        }
        service.shutdown();
        return true;
    }

    @Override
    public void addPages() {
        super.addPages();
        addPage(projectSelectionPage);
        addPage(librariesPage);
        addPage(packageSelectionPage);
        addPage(depersonalisationPage);
        addPage(destinationPage);
    }

}
