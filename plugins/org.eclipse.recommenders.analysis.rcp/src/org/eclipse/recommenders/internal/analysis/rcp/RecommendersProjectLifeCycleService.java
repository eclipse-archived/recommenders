/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.analysis.rcp;

import java.util.Set;

import javax.inject.Inject;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.recommenders.utils.rcp.RCPUtils;

public class RecommendersProjectLifeCycleService implements IElementChangedListener {

    private final Set<IRecommendersProjectLifeCycleListener> listeners;

    @Inject
    public RecommendersProjectLifeCycleService(final Set<IRecommendersProjectLifeCycleListener> listeners) {
        this.listeners = listeners;
        initialize();
    }

    protected void initialize() {
        registerElementChangedListener();
        simulateProjectOpenEvents();
    }

    private void registerElementChangedListener() {
        JavaCore.addElementChangedListener(this);
    }

    @SuppressWarnings("restriction")
    private void simulateProjectOpenEvents() {
        new WorkspaceJob("Initializing projects with recommenders nature") {
            @Override
            public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
                final Set<IProject> openProjects = getAllOpenProjects();
                monitor.beginTask("", openProjects.size());
                for (final IProject project : openProjects) {
                    if (JavaProject.hasJavaNature(project)) {
                        final IJavaProject javaProject = toJavaProject(project);
                        fireOpenEvent(javaProject);
                    }
                    monitor.worked(1);
                }
                monitor.done();
                return Status.OK_STATUS;
            }
        }.schedule();
    }

    protected Set<IProject> getAllOpenProjects() {
        return RCPUtils.getAllOpenProjects();
    }

    @Override
    public void elementChanged(final ElementChangedEvent event) {
        final IJavaElementDelta delta = event.getDelta();
        process(delta);
    }

    private void process(final IJavaElementDelta delta) {
        if ((delta.getFlags() & IJavaElementDelta.F_CHILDREN) != 0) {
            for (final IJavaElementDelta child : delta.getAffectedChildren()) {
                process(child);
            }
        }

        final IJavaElement changedElement = delta.getElement();
        final IJavaProject javaProject = delta.getElement().getJavaProject();
        if (javaProject == null || changedElement != javaProject) {
            return;
        }

        if (isOpenEvent(delta)) {
            fireOpenEvent(javaProject);
        } else if (isCloseEvent(delta)) {
            fireCloseEvent(javaProject);
        }
    }

    private boolean isCloseEvent(final IJavaElementDelta delta) {
        final boolean removed = (delta.getKind() & IJavaElementDelta.REMOVED) != 0;
        final boolean closed = (delta.getFlags() & IJavaElementDelta.F_CLOSED) != 0;
        return removed || closed;
    }

    private boolean isOpenEvent(final IJavaElementDelta delta) {
        final boolean added = (delta.getKind() & IJavaElementDelta.ADDED) != 0;
        final boolean opened = (delta.getFlags() & IJavaElementDelta.F_OPENED) != 0;
        return added || opened;
    }

    public IJavaProject toJavaProject(final IProject project) {
        return JavaCore.create(project);
    }

    public void fireOpenEvent(final IJavaProject javaProject) {
        for (final IRecommendersProjectLifeCycleListener listener : listeners) {
            listener.projectOpened(javaProject);
        }
    }

    public void fireCloseEvent(final IJavaProject javaProject) {
        for (final IRecommendersProjectLifeCycleListener listener : listeners) {
            listener.projectClosed(javaProject);
        }
    }

}
