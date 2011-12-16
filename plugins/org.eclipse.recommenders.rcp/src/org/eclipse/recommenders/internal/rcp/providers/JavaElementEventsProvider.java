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
package org.eclipse.recommenders.internal.rcp.providers;

import static org.eclipse.recommenders.utils.Checks.cast;

import java.util.Set;

import javax.inject.Inject;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.recommenders.rcp.Events.CompilationUnitAddedEvent;
import org.eclipse.recommenders.rcp.Events.CompilationUnitRemovedEvent;
import org.eclipse.recommenders.rcp.Events.CompilationUnitSavedEvent;
import org.eclipse.recommenders.rcp.Events.JavaProjectClosedEvent;
import org.eclipse.recommenders.rcp.Events.JavaProjectOpenedEvent;
import org.eclipse.recommenders.rcp.utils.RCPUtils;
import org.eclipse.recommenders.utils.annotations.Testing;

import com.google.common.eventbus.EventBus;

@SuppressWarnings("restriction")
public class JavaElementEventsProvider implements IElementChangedListener {

    private final EventBus bus;

    @Inject
    public JavaElementEventsProvider(final EventBus bus) {
        this.bus = bus;
        simulateProjectOpenEvents();
    }

    private void simulateProjectOpenEvents() {
        new SimulateOpenJavaProjectsJob("Initializing projects with recommenders nature").schedule();
    }

    @Override
    public void elementChanged(final ElementChangedEvent event) {
        final IJavaElementDelta delta = event.getDelta();
        process(delta);
    }

    private void process(final IJavaElementDelta delta) {
        if (isChildAffectedByChange(delta)) {
            for (final IJavaElementDelta child : delta.getAffectedChildren()) {
                process(child);
            }
        }

        if (isProjectChangedEvent(delta)) {
            final IJavaProject javaProject = cast(delta.getElement());
            if (isProjectOpenedEvent(delta)) {
                fireProjectOpenedEvent(javaProject);
            } else if (isProjectClosedEvent(delta)) {
                fireProjectClosedEvent(javaProject);
            }
        } else if (isCompilationUnitChangedEvent(delta)) {
            final ICompilationUnit cu = cast(delta.getElement());
            switch (delta.getKind()) {
            case IJavaElementDelta.ADDED:
                fireCompilationUnitAddedEvent(cu);
                break;
            case IJavaElementDelta.CHANGED:
                if (isCompilationUnitSavedEvent(delta)) {
                    fireCompilationUnitSavedEvent(cu);
                }
                break;
            case IJavaElementDelta.REMOVED:
                fireCompilationUnitRemovedEvent(cu);
                break;
            }
        }
    }

    private boolean isCompilationUnitSavedEvent(final IJavaElementDelta delta) {
        final int flags = delta.getFlags();
        return (flags & IJavaElementDelta.F_PRIMARY_RESOURCE) > 0;
    }

    private boolean isChildAffectedByChange(final IJavaElementDelta delta) {
        return (delta.getFlags() & IJavaElementDelta.F_CHILDREN) != 0;
    }

    private boolean isProjectChangedEvent(final IJavaElementDelta delta) {
        final IJavaElement changedElement = delta.getElement();
        final IJavaProject javaProject = delta.getElement().getJavaProject();
        return javaProject != null && changedElement == javaProject;
    }

    private boolean isProjectOpenedEvent(final IJavaElementDelta delta) {
        final boolean added = (delta.getKind() & IJavaElementDelta.ADDED) != 0;
        final boolean opened = (delta.getFlags() & IJavaElementDelta.F_OPENED) != 0;
        return added || opened;
    }

    public void fireProjectOpenedEvent(final IJavaProject javaProject) {
        bus.post(new JavaProjectOpenedEvent(javaProject));
    }

    private boolean isProjectClosedEvent(final IJavaElementDelta delta) {
        final boolean removed = (delta.getKind() & IJavaElementDelta.REMOVED) != 0;
        final boolean closed = (delta.getFlags() & IJavaElementDelta.F_CLOSED) != 0;
        return removed || closed;
    }

    public void fireProjectClosedEvent(final IJavaProject javaProject) {
        bus.post(new JavaProjectClosedEvent(javaProject));
    }

    private boolean isCompilationUnitChangedEvent(final IJavaElementDelta delta) {
        final IJavaElement element = delta.getElement();
        return element instanceof ICompilationUnit;
    }

    private void fireCompilationUnitAddedEvent(final ICompilationUnit cu) {
        bus.post(new CompilationUnitAddedEvent(cu));
    }

    private void fireCompilationUnitSavedEvent(final ICompilationUnit cu) {
        bus.post(new CompilationUnitSavedEvent(cu));
    }

    private void fireCompilationUnitRemovedEvent(final ICompilationUnit cu) {
        bus.post(new CompilationUnitRemovedEvent(cu));
    }

    @Testing("override this method for testing")
    protected Set<IProject> getAllOpenProjects() {
        return RCPUtils.getAllOpenProjects();
    }

    IJavaProject toJavaProject(final IProject project) {
        return JavaCore.create(project);
    }

    private final class SimulateOpenJavaProjectsJob extends WorkspaceJob {
        private SimulateOpenJavaProjectsJob(final String name) {
            super(name);
        }

        @Override
        public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
            final Set<IProject> openProjects = getAllOpenProjects();
            monitor.beginTask("", openProjects.size());
            for (final IProject project : openProjects) {
                if (JavaProject.hasJavaNature(project)) {
                    final IJavaProject javaProject = toJavaProject(project);
                    fireProjectOpenedEvent(javaProject);
                }
                monitor.worked(1);
            }
            monitor.done();
            return Status.OK_STATUS;
        }
    }
}