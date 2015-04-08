/**
 * Copyright (c) 2015 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.types.rcp;

import static org.eclipse.jdt.core.IJavaElement.*;
import static org.eclipse.jdt.core.IJavaElementDelta.*;

import java.io.File;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

public class TypesIndexService implements IElementChangedListener, IStartup {

    private static TypesIndexService INSTANCE;

    private Map<IJavaProject, BuildTypeHierarchyIndexJob> jobs = Maps.newHashMap();

    public TypesIndexService() {
        INSTANCE = this;
    }

    public static TypesIndexService getInstance() {
        return INSTANCE;
    }

    private Map<IJavaProject, ProjectTypesIndex> _indexes = Maps.newHashMap();

    @Override
    public void earlyStartup() {
        JavaCore.addElementChangedListener(this, ElementChangedEvent.POST_CHANGE);
        PlatformUI.getWorkbench().addWorkbenchListener(new ShutdownListener());
    }

    @Override
    public void elementChanged(ElementChangedEvent event) {
        IJavaElementDelta delta = event.getDelta();
        process(delta);
    }

    private void process(IJavaElementDelta delta) {
        if (isChildAffectedByChange(delta)) {
            for (IJavaElementDelta child : delta.getAffectedChildren()) {
                process(child);
            }
            return;
        }

        IJavaElement element = delta.getElement();
        IJavaProject project = element.getJavaProject();

        switch (delta.getKind()) {

        case IJavaElementDelta.ADDED:
            switch (element.getElementType()) {
            case COMPILATION_UNIT:
                indexCompilationUnit((ICompilationUnit) element);
                break;
            case PACKAGE_FRAGMENT:
                break;
            case PACKAGE_FRAGMENT_ROOT:
                rebuildIndex(project);
                break;
            case JAVA_PROJECT:
                rebuildIndex(project);
                break;
            }
            break;
        case CHANGED:
            switch (element.getElementType()) {
            case COMPILATION_UNIT:
                removeCompilationUnit((ICompilationUnit) element);
                indexCompilationUnit((ICompilationUnit) element);
                commit(project);
                break;
            case PACKAGE_FRAGMENT_ROOT:
                rebuildIndex(project);
                break;
            case PACKAGE_FRAGMENT:
                break;
            case JAVA_PROJECT:
                break;
            }
            break;
        case REMOVED:
            switch (element.getElementType()) {
            case COMPILATION_UNIT:
                removeCompilationUnit((ICompilationUnit) element);
                break;
            case PACKAGE_FRAGMENT_ROOT:
                rebuildIndex(project);
                break;
            case PACKAGE_FRAGMENT:
            case JAVA_PROJECT:
                break;
            }
            break;
        }
    }

    private void rebuildIndex(IJavaProject project) {
        ProjectTypesIndex index = findOrCreateIndex(project);
        triggerFullBuild(index);
    }

    private void commit(IJavaProject project) {
        findOrCreateIndex(project).commit();
    }

    private void removeCompilationUnit(ICompilationUnit cu) {
        findOrCreateIndex(cu.getJavaProject()).removeCompilationUnit(cu);
    }

    private void indexCompilationUnit(ICompilationUnit cu) {
        findOrCreateIndex(cu.getJavaProject()).indexCompilationUnit(cu);
    }

    private synchronized ProjectTypesIndex findOrCreateIndex(IJavaProject project) {
        ProjectTypesIndex index = _indexes.get(project);
        if (index == null) {
            index = new ProjectTypesIndex(project, computeIndexDir(project));
            _indexes.put(project, index);
            index.startAsync();
            index.awaitRunning();
            if (index.isEmpty()) {
                triggerFullBuild(index);
            }
        }
        return index;
    }

    private void triggerFullBuild(final ProjectTypesIndex index) {
        IJavaProject project = index.getProject();
        BuildTypeHierarchyIndexJob prev = jobs.get(project);
        boolean isNotYetRun = prev != null && prev.getResult() == null && prev.getState() != Job.RUNNING;
        if (isNotYetRun) {
            // we already have a job that will pick up this change.
            return;
        }

        BuildTypeHierarchyIndexJob job = new BuildTypeHierarchyIndexJob(index);
        // TODO this is bit naive but works for now..
        // if there is another job running, we should stop that one...
        jobs.put(project, job);
        job.setRule(MutexRule.INSTANCE);
        // we don't re-index immediately. the old model won't be completely broken...
        job.schedule(TimeUnit.SECONDS.toMillis(3));
    }

    private boolean isChildAffectedByChange(IJavaElementDelta delta) {
        return (delta.getFlags() & IJavaElementDelta.F_CHILDREN) != 0;
    }

    public void open(IJavaProject project) {
        ProjectTypesIndex index = findOrCreateIndex(project);
        if (index.isEmpty()) {
            triggerFullBuild(index);
        }
    }

    private File computeIndexDir(IJavaProject project) {
        Bundle bundle = FrameworkUtil.getBundle(getClass());
        File location = Platform.getStateLocation(bundle).toFile();
        File indexDir = new File(location, "indexes/" + project.getElementName().replaceAll("\\W", "_"));
        return indexDir;
    }

    public void close(IJavaProject project) {
    }

    public ImmutableSet<String> subtypes(IType expected) {
        return findOrCreateIndex(expected.getJavaProject()).subtypes(expected);
    }

    public ImmutableSet<String> subtypes(ITypeName expected, IJavaProject project) {
        return findOrCreateIndex(project).subtypes(expected);
    }

    public ImmutableSet<String> subtypes(String type, IJavaProject project) {
        return findOrCreateIndex(project).subtypes(type);
    }

    private final class BuildTypeHierarchyIndexJob extends Job {
        private final ProjectTypesIndex index;

        private BuildTypeHierarchyIndexJob(ProjectTypesIndex index) {
            super(String.format("Indexing type hierarchy of '%s'", index.getProject().getElementName()));
            this.index = index;
        }

        @Override
        protected IStatus run(IProgressMonitor monitor) {
            Thread thread = Thread.currentThread();
            int priority = thread.getPriority();
            try {
                thread.setPriority(Thread.MIN_PRIORITY);
                index.clear();
                index.rebuild(monitor);
                index.commit();
                // index.compact();
            } finally {
                thread.setPriority(priority);
            }
            return Status.OK_STATUS;
        }

        @Override
        public boolean shouldRun() {
            return jobs.get(index.getProject()) == this;
        }
    }

    private final class ShutdownListener implements IWorkbenchListener {
        @Override
        public boolean preShutdown(IWorkbench workbench, boolean forced) {
            return true;
        }

        @Override
        public void postShutdown(IWorkbench workbench) {
            for (ProjectTypesIndex index : _indexes.values()) {
                index.stopAsync();
            }
        }
    }

    public static class MutexRule implements ISchedulingRule {

        public static final MutexRule INSTANCE = new MutexRule();

        @Override
        public boolean isConflicting(ISchedulingRule rule) {
            return rule == this;
        }

        @Override
        public boolean contains(ISchedulingRule rule) {
            return rule == this;
        }
    }
}
