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
package org.eclipse.recommenders.internal.rcp.views.recommendations;

import java.util.Set;

import javax.inject.Inject;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.recommenders.commons.utils.annotations.Nullable;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.CompilationUnit;
import org.eclipse.recommenders.internal.rcp.IDs;
import org.eclipse.recommenders.internal.rcp.RecommenderAdapter;
import org.eclipse.recommenders.rcp.IArtifactStore;
import org.eclipse.recommenders.rcp.IEditorDashboard;
import org.eclipse.recommenders.rcp.IRecommendation;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.WorkbenchJob;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class RecommendationsViewPublisher extends RecommenderAdapter {

    private final Set<IRecommendationsViewContentProvider> publishers;

    private final IArtifactStore store;

    @Inject
    public RecommendationsViewPublisher(final Set<IRecommendationsViewContentProvider> publishers,
            final IArtifactStore store) {
        this.publishers = publishers;
        this.store = store;
    }

    @Override
    public synchronized void editorActivated(final IEditorDashboard dashboard, final IProgressMonitor monitor)
            throws CoreException {
        publishViewIfOpen(dashboard.getCompilationUnit());
    }

    @Override
    public void unitChanged(final ICompilationUnit cu, final IProgressMonitor monitor) throws CoreException {
        publishViewIfOpen(cu);
    }

    private void publishViewIfOpen(final @Nullable ICompilationUnit jdtCu) {
        final WorkbenchJob job = new WorkbenchJob("") {

            @Override
            public IStatus runInUIThread(final IProgressMonitor monitor) {
                final RecommendationsView view = findView();
                if (view == null) {
                    return Status.CANCEL_STATUS;
                }
                final Multimap<Object, IRecommendation> recommendations = HashMultimap.create();
                if (!store.hasArtifact(jdtCu, CompilationUnit.class)) {
                    return Status.CANCEL_STATUS;
                }
                final CompilationUnit recCu = store.loadArtifact(jdtCu, CompilationUnit.class);
                for (final IRecommendationsViewContentProvider provider : publishers) {
                    provider.attachRecommendations(jdtCu, recCu, recommendations);
                }
                populateRecommendationsView(view, recCu, recommendations);
                return Status.OK_STATUS;
            }
        };
        job.schedule();
    }

    private RecommendationsView findView() {
        final IWorkbench workbench = PlatformUI.getWorkbench();
        if (workbench.isClosing()) {
            return null;
        }
        final IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
        final IWorkbenchPage page = window.getActivePage();
        final RecommendationsView view = (RecommendationsView) page.findView(IDs.RECOMMENDATIONS_VIEW_ID);
        return view;
    }

    private void populateRecommendationsView(final RecommendationsView view, final CompilationUnit recCu,
            final Multimap<Object, IRecommendation> recommendations) {
        view.setInput(recCu, recommendations);
    }
}
