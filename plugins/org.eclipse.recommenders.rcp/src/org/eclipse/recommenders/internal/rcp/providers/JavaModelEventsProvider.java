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

import static org.eclipse.jdt.core.IJavaElementDelta.F_ADDED_TO_CLASSPATH;
import static org.eclipse.jdt.core.IJavaElementDelta.F_REMOVED_FROM_CLASSPATH;
import static org.eclipse.recommenders.utils.Checks.cast;

import java.util.Set;

import javax.inject.Inject;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
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
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.recommenders.rcp.events.JavaModelEvents.CompilationUnitAdded;
import org.eclipse.recommenders.rcp.events.JavaModelEvents.CompilationUnitChanged;
import org.eclipse.recommenders.rcp.events.JavaModelEvents.CompilationUnitRemoved;
import org.eclipse.recommenders.rcp.events.JavaModelEvents.CompilationUnitSaved;
import org.eclipse.recommenders.rcp.events.JavaModelEvents.JarPackageFragmentRootAdded;
import org.eclipse.recommenders.rcp.events.JavaModelEvents.JarPackageFragmentRootRemoved;
import org.eclipse.recommenders.rcp.events.JavaModelEvents.JavaProjectClosed;
import org.eclipse.recommenders.rcp.events.JavaModelEvents.JavaProjectOpened;

import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;

@SuppressWarnings("restriction")
public class JavaModelEventsProvider implements IElementChangedListener {

    private final EventBus bus;
    private final IWorkspaceRoot workspace;

    @Inject
    public JavaModelEventsProvider(final EventBus bus, final IWorkspaceRoot workspace) {
        this.bus = bus;
        this.workspace = workspace;
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
            processProjectChangedEvent(delta);
        } else if (isCompilationUnitChangedEvent(delta)) {
            processCompilationUnitChangedEvent(delta);
        } else if (isJarPackageFragementRootChangedEvent(delta)) {
            processJarPackageFragementRootChangedEvent(delta);
        }
    }

    private boolean isChildAffectedByChange(final IJavaElementDelta delta) {
        return (delta.getFlags() & IJavaElementDelta.F_CHILDREN) != 0;
    }

    private boolean isProjectChangedEvent(final IJavaElementDelta delta) {
        final IJavaElement changedElement = delta.getElement();
        final IJavaProject javaProject = delta.getElement().getJavaProject();
        return javaProject != null && changedElement == javaProject;
    }

    private void processProjectChangedEvent(final IJavaElementDelta delta) {
        final IJavaProject javaProject = cast(delta.getElement());
        if (isProjectOpenedEvent(delta)) {
            fireProjectOpenedEvent(javaProject);
        } else if (isProjectClosedEvent(delta)) {
            fireProjectClosedEvent(javaProject);
        }
    }

    private boolean isProjectOpenedEvent(final IJavaElementDelta delta) {
        final boolean added = (delta.getKind() & IJavaElementDelta.ADDED) != 0;
        final boolean opened = (delta.getFlags() & IJavaElementDelta.F_OPENED) != 0;
        return added || opened;
    }

    private void fireProjectOpenedEvent(final IJavaProject javaProject) {
        bus.post(new JavaProjectOpened(javaProject));
    }

    private boolean isProjectClosedEvent(final IJavaElementDelta delta) {
        final boolean removed = (delta.getKind() & IJavaElementDelta.REMOVED) != 0;
        final boolean closed = (delta.getFlags() & IJavaElementDelta.F_CLOSED) != 0;
        return removed || closed;
    }

    private void fireProjectClosedEvent(final IJavaProject javaProject) {
        bus.post(new JavaProjectClosed(javaProject));
    }

    private boolean isCompilationUnitChangedEvent(final IJavaElementDelta delta) {
        final IJavaElement element = delta.getElement();
        return element instanceof ICompilationUnit;
    }

    private void processCompilationUnitChangedEvent(final IJavaElementDelta delta) {
        final ICompilationUnit cu = cast(delta.getElement());
        switch (delta.getKind()) {
        case IJavaElementDelta.ADDED:
            fireCompilationUnitAddedEvent(cu);
            break;
        case IJavaElementDelta.CHANGED:
            if (isCompilationUnitSavedEvent(delta)) {
                fireCompilationUnitSavedEvent(cu);
            } else {
                fireCompilationUnitChangedEvent(cu);
            }
            break;
        case IJavaElementDelta.REMOVED:
            fireCompilationUnitRemovedEvent(cu);
            break;
        }
    }

    private void fireCompilationUnitAddedEvent(final ICompilationUnit cu) {
        bus.post(new CompilationUnitAdded(cu));
    }

    private boolean isCompilationUnitSavedEvent(final IJavaElementDelta delta) {
        final int flags = delta.getFlags() & IJavaElementDelta.F_PRIMARY_RESOURCE;
        return flags > 0;
    }

    private void fireCompilationUnitSavedEvent(final ICompilationUnit cu) {
        bus.post(new CompilationUnitSaved(cu));
    }

    private void fireCompilationUnitChangedEvent(final ICompilationUnit cu) {
        bus.post(new CompilationUnitChanged(cu));
    }

    private void fireCompilationUnitRemovedEvent(final ICompilationUnit cu) {
        bus.post(new CompilationUnitRemoved(cu));
    }

    private boolean isJarPackageFragementRootChangedEvent(final IJavaElementDelta delta) {
        final IJavaElement changedElement = delta.getElement();
        return changedElement instanceof JarPackageFragmentRoot;
    }

    private void processJarPackageFragementRootChangedEvent(final IJavaElementDelta delta) {
        switch (delta.getKind()) {
        case IJavaElementDelta.ADDED:
            fireJarPackageFragementRootAddedEvent(delta);
            return;

        case IJavaElementDelta.REMOVED:
            fireJarPackageFragementRootRemoved(delta);
            return;

        case IJavaElementDelta.CHANGED:
            switch (delta.getFlags()) {
            case F_ADDED_TO_CLASSPATH:
                fireJarPackageFragementRootAddedEvent(delta);
                return;
            case F_REMOVED_FROM_CLASSPATH:
                fireJarPackageFragementRootRemoved(delta);
                return;
            }
        }
    }

    private void fireJarPackageFragementRootAddedEvent(final IJavaElementDelta delta) {
        bus.post(new JarPackageFragmentRootAdded((JarPackageFragmentRoot) (delta.getElement())));
    }

    private void fireJarPackageFragementRootRemoved(final IJavaElementDelta delta) {
        bus.post(new JarPackageFragmentRootRemoved((JarPackageFragmentRoot) (delta.getElement())));
    }

    private Set<IProject> getAllOpenProjects() {
        final Set<IProject> result = Sets.newHashSet();
        final IProject[] projects = workspace.getProjects();
        for (final IProject project : projects) {
            if (project.isAccessible()) {
                result.add(project);
            }
        }
        return result;
    }

    private IJavaProject toJavaProject(final IProject project) {
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
