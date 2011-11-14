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
package org.eclipse.recommenders.internal.rcp;

import static org.eclipse.recommenders.utils.Checks.ensureIsNotNull;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.recommenders.rcp.IArtifactStoreChangedListener;
import org.eclipse.recommenders.rcp.IEditorChangedListener;
import org.eclipse.recommenders.rcp.IEditorDashboard;
import org.eclipse.recommenders.rcp.NotificationPriority;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * Default implementation for {@link IEditorChangedListener} and
 * {@link IArtifactStoreChangedListener}. It only tracks which editors are
 * currently open.
 * 
 */
public class RecommenderAdapter implements IEditorChangedListener, IArtifactStoreChangedListener {

    private final BiMap<IEditorDashboard, ICompilationUnit> openEditors = HashBiMap.create();

    /**
     * Returns {@link NotificationPriority#DEFAULT}
     */
    @Override
    public NotificationPriority getNotifcationPriority() {
        return NotificationPriority.DEFAULT;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This default implementation does nothing.
     * </p>
     */
    @Override
    public void editorActivated(final IEditorDashboard dashboard, final IProgressMonitor monitor) throws CoreException {
    }

    /**
     * {@inheritDoc}
     * <p>
     * This default implementation removes the dashboard from the list of open
     * editors.
     * </p>
     */
    @Override
    public void editorClosed(final IEditorDashboard dashboard, final IProgressMonitor monitor) throws CoreException {
        ensureIsNotNull(dashboard);
        openEditors.remove(dashboard);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This default implementation does nothing.
     * </p>
     */
    @Override
    public void editorDeactivated(final IEditorDashboard dashboard, final IProgressMonitor monitor)
            throws CoreException {
    }

    /**
     * {@inheritDoc}
     * <p>
     * This default implementation does nothing.
     * </p>
     */
    @Override
    public void editorHidden(final IEditorDashboard dashboard, final IProgressMonitor monitor) throws CoreException {
    }

    /**
     * {@inheritDoc}
     * <p>
     * This default implementation add the given dashboard to the list of open
     * editors.
     * </p>
     */
    @Override
    public void editorOpened(final IEditorDashboard dashboard, final IProgressMonitor monitor) throws CoreException {
        ensureIsNotNull(dashboard);
        if (dashboard.hasCompilationUnit()) {
            openEditors.put(dashboard, dashboard.getCompilationUnit());
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * This default implementation does nothing.
     * </p>
     */
    @Override
    public void editorVisble(final IEditorDashboard dashboard, final IProgressMonitor monitor) throws CoreException {
    }

    /**
     * {@inheritDoc}
     * <p>
     * This default implementation does nothing.
     * </p>
     */
    @Override
    public void unitChanged(final IJavaElement cu, final IProgressMonitor monitor) throws CoreException {
    }

    /**
     * Searches the list of open editors to find the corresponding
     * {@link IEditorDashboard} to the given {@link ICompilationUnit}. Returns
     * <code>null</code> if no matching dashboard could be found.
     */
    protected IEditorDashboard findDashboard(final ICompilationUnit cu) {
        final BiMap<ICompilationUnit, IEditorDashboard> openCus = openEditors.inverse();
        final IEditorDashboard dashboard = openCus.get(cu);
        return dashboard;
    }

    /**
     * 
     Returns the map of all open editors tracked by this recommender.
     * 
     */
    protected BiMap<IEditorDashboard, ICompilationUnit> getOpenEditors() {
        return openEditors;
    }
}