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
package org.eclipse.recommenders.internal.udc;

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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.recommenders.internal.udc.depersonalizer.ICompilationUnitDepersonalizer;
import org.eclipse.recommenders.internal.udc.depersonalizer.LineNumberDepersonalizer;
import org.eclipse.recommenders.internal.udc.depersonalizer.NameDepersonalizer;
import org.eclipse.recommenders.internal.udc.depersonalizer.ObjectUsageFilter;
import org.eclipse.recommenders.internal.udc.ui.packageselection.PackageTester;
import org.eclipse.recommenders.internal.udc.ui.preferences.ProjectPreferenceUtil;
import org.eclipse.recommenders.internal.udc.ui.wizard.PreferencesForProjectsWizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;

public class AutomaticUploadJob extends Job {

    private static final String NEW_PROJECTS_QUESTION = "Found %d new project(s)."
            + "\nNo Usage Data will be uploaded for these projects, since there are no preferences available."
            + " Do you want to set preferences for these projects now?";

    public AutomaticUploadJob() {
        super("Compilation Unit Upload");
    }

    @Override
    protected IStatus run(final IProgressMonitor monitor) {
        if (UploadPreferences.doAskBeforeUploading()) {
            if (!userWantsToUploadDataNow()) {
                return Status.CANCEL_STATUS;
            }
        }
        final CompilationUnitExportJob job = createJob();
        job.schedule();
        return Status.OK_STATUS;
    }

    private CompilationUnitExportJob createJob() {
        final IProject[] selectedProjects = getSelectedProjects();

        final PackageTester packageTester = new PackageTester(PreferenceUtil.getIncludeExpressions(),
                PreferenceUtil.getIncludeExpressions());

        final ICompilationUnitDepersonalizer[] depersonalizers = getDepersonalizers();

        final CompilationUnitExportJob job = new CompilationUnitExportJob(selectedProjects, depersonalizers,
                ExporterFactory.createCompilationUnitServerExporter(), packageTester);
        return job;
    }

    private boolean askTheUser(final String question) {
        final boolean[] result = new boolean[1];
        Display.getDefault().syncExec(new Runnable() {

            @Override
            public void run() {
                final MessageBox mb = new MessageBox(Display.getDefault().getActiveShell(), SWT.ICON_QUESTION | SWT.YES
                        | SWT.NO);
                mb.setMessage(question);
                result[0] = mb.open() == SWT.YES;
            }

        });

        return result[0];
    }

    private boolean userWantsToUploadDataNow() {
        return askTheUser("Do you want to upload your Recommenders Usage Data now?");
    }

    private IProject[] getSelectedProjects() {
        final ProjectProvider provider = new ProjectProvider();
        final IProject[] allProjects = provider.getAllRecommenderProjectsInWorkspace();
        final IProject[] newProjects = provider.getNewProjects(allProjects);
        if (newProjects.length > 0) {
            handleNewProjects(newProjects);
        }
        final IProject[] selectedProjects = provider.getProjectsEnabledForExport(allProjects);
        return selectedProjects;
    }

    private void handleNewProjects(final IProject[] newProjects) {
        switch (ProjectPreferenceUtil.getNewProjectHandling()) {
        case ask:
            if (askForSettingPreferences(newProjects)) {
                openSetPreferenceForProjectsWizard(newProjects);
            }
            break;
        case enable:
            ProjectPreferenceUtil.setProjectsEnabledForExport(newProjects);
            break;
        case ignore:
        default:
            break;
        }

    }

    private void openSetPreferenceForProjectsWizard(final IProject[] newProjects) {
        Display.getDefault().syncExec(new Runnable() {

            @Override
            public void run() {
                final WizardDialog dialog = new WizardDialog(Display.getDefault().getActiveShell(),
                        new PreferencesForProjectsWizard());
                dialog.open();
            }
        });
    }

    private boolean askForSettingPreferences(final IProject[] newProjects) {
        final String question = String.format(NEW_PROJECTS_QUESTION, newProjects.length);
        return askTheUser(question);
    }

    private ICompilationUnitDepersonalizer[] getDepersonalizers() {
        ICompilationUnitDepersonalizer[] depersonalizers;
        if (PreferenceUtil.isDepesonalisationRequired()) {
            depersonalizers = new ICompilationUnitDepersonalizer[] { createObjectUsageFilter(),
                    new LineNumberDepersonalizer(), new NameDepersonalizer() };
        } else {
            depersonalizers = new ICompilationUnitDepersonalizer[] { createObjectUsageFilter() };
        }
        return depersonalizers;
    }

    private ObjectUsageFilter createObjectUsageFilter() {
        final FingerprintProvider provider = FingerprintProvider.createInstance();
        return new ObjectUsageFilter(provider.getFingerprints(PreferenceUtil.getEnabledLibraries()));
    }
}
