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

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jdt.core.ElementChangedEvent;
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
        IJavaElement element = delta.getElement();
        IJavaProject project = element.getJavaProject();
        boolean resolvedClasspathChanged = (delta.getFlags() & IJavaElementDelta.F_RESOLVED_CLASSPATH_CHANGED) != 0;
        if (element instanceof IJavaProject && resolvedClasspathChanged) {
            rebuildIndex(element.getJavaProject());
            return;
        }

        if (isChildAffectedByChange(delta)) {
            for (IJavaElementDelta child : delta.getAffectedChildren()) {
                process(child);
            }
            return;
        }

        switch (delta.getKind()) {

        case IJavaElementDelta.ADDED:
            switch (element.getElementType()) {
            case COMPILATION_UNIT:
                // indexCompilationUnit((ICompilationUnit) element);
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
                // removeCompilationUnit((ICompilationUnit) element);
                // indexCompilationUnit((ICompilationUnit) element);
                // commit(project);
                break;
            case PACKAGE_FRAGMENT_ROOT:
                boolean reordered = (delta.getFlags() & IJavaElementDelta.F_REORDER) != 0;
                boolean removed = (delta.getFlags() & IJavaElementDelta.F_REMOVED_FROM_CLASSPATH) != 0;
                boolean content = (delta.getFlags() & IJavaElementDelta.F_ARCHIVE_CONTENT_CHANGED) != 0;
                if (reordered)
                    return;
                if (removed || content)
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
                // removeCompilationUnit((ICompilationUnit) element);
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
        ProjectTypesIndex index = _indexes.get(project);
        if (index == null) {
            return;
        }
        if (index.needsRebuild()) {
            index.setRebuildAfterNextAccess(true);
        }
    }

    private synchronized ProjectTypesIndex findOrCreateIndex(IJavaProject project) {
        ProjectTypesIndex index = _indexes.get(project);
        if (index == null) {
            index = new ProjectTypesIndex(project, computeIndexDir(project));
            _indexes.put(project, index);
            index.startAsync();
        }
        return index;
    }

    private boolean isChildAffectedByChange(IJavaElementDelta delta) {
        return (delta.getFlags() & IJavaElementDelta.F_CHILDREN) != 0;
    }

    private File computeIndexDir(IJavaProject project) {
        Bundle bundle = FrameworkUtil.getBundle(getClass());
        File location = Platform.getStateLocation(bundle).toFile();
        File indexDir = new File(location, "indexes/" + project.getElementName().replaceAll("\\W", "_"));
        return indexDir;
    }

    public void close(IJavaProject project) {
    }

    public ImmutableSet<String> subtypes(IType expected, String prefix) {
        return findOrCreateIndex(expected.getJavaProject()).subtypes(expected, prefix);
    }

    public ImmutableSet<String> subtypes(ITypeName expected, String prefix, IJavaProject project) {
        return findOrCreateIndex(project).subtypes(expected, prefix);
    }

    public ImmutableSet<String> subtypes(String type, String prefix, IJavaProject project) {
        return findOrCreateIndex(project).subtypes(type, prefix);
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
