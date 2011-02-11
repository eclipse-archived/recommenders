/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.views.cu;

import javax.inject.Inject;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.CompilationUnit;
import org.eclipse.recommenders.internal.rcp.IDs;
import org.eclipse.recommenders.internal.rcp.RecommenderAdapter;
import org.eclipse.recommenders.rcp.IArtifactStore;
import org.eclipse.recommenders.rcp.IEditorDashboard;
import org.eclipse.recommenders.rcp.NotificationPriority;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.WorkbenchJob;

public class CompilationUnitViewPublisher extends RecommenderAdapter {
    private final IArtifactStore store;

    @Inject
    public CompilationUnitViewPublisher(final IArtifactStore store) {
        this.store = store;
    }

    @Override
    public NotificationPriority getNotifcationPriority() {
        return NotificationPriority.LATEST;
    }

    @Override
    public void unitChanged(final ICompilationUnit cu, final IProgressMonitor monitor) throws CoreException {
        super.unitChanged(cu, monitor);
        publishViewIfOpen(cu);
    }

    @Override
    public void editorActivated(final IEditorDashboard dashboard, final IProgressMonitor monitor) throws CoreException {
        super.editorActivated(dashboard, monitor);
        final ICompilationUnit compilationUnit = dashboard.getCompilationUnit();
        if (compilationUnit != null) {
            publishViewIfOpen(compilationUnit);
        }
    }

    private void publishViewIfOpen(final ICompilationUnit compilationUnit) {
        final WorkbenchJob job = new WorkbenchJob("") {
            @Override
            public IStatus runInUIThread(final IProgressMonitor monitor) {
                final CompilationUnitView view = findCompilationUnitView();
                if (view == null) {
                    return Status.CANCEL_STATUS;
                }
                if (!store.hasArtifact(compilationUnit, CompilationUnit.class)) {
                    return Status.CANCEL_STATUS;
                }
                final CompilationUnit artifact = store.loadArtifact(compilationUnit, CompilationUnit.class);
                view.setInput(artifact);
                return Status.OK_STATUS;
            }

            private CompilationUnitView findCompilationUnitView() {
                final IWorkbench workbench = PlatformUI.getWorkbench();
                if (workbench.isClosing()) {
                    return null;
                }
                final IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
                final IWorkbenchPage page = window.getActivePage();
                final CompilationUnitView view = (CompilationUnitView) page.findView(IDs.COMPILATIONUNIT_VIEW_ID);
                return view;
            }
        };
        job.schedule();
    }
}
