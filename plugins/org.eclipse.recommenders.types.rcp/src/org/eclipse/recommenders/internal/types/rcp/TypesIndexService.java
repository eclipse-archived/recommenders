/**
 * Copyright (c) 2015 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 *    Johannes Dorn - Refactoring
 */
package org.eclipse.recommenders.internal.types.rcp;

import static org.eclipse.jdt.core.IJavaElement.*;
import static org.eclipse.jdt.core.IJavaElementDelta.*;

import java.io.IOException;
import java.util.Set;

import javax.inject.Inject;

import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.recommenders.internal.types.rcp.l10n.LogMessages;
import org.eclipse.recommenders.utils.Logs;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.PlatformUI;

public class TypesIndexService implements ITypesIndexService, IElementChangedListener {

    private final IIndexProvider indexProvider;

    @Inject
    public TypesIndexService(IIndexProvider indexProvider) {
        this.indexProvider = indexProvider;
        JavaCore.addElementChangedListener(this, ElementChangedEvent.POST_CHANGE);
        PlatformUI.getWorkbench().addWorkbenchListener(new ShutdownListener());
    };

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
            processElementAdded(element, project);
            break;
        case CHANGED:
            processElementChanged(delta, element, project);
            break;
        case REMOVED:
            processElementRemoved(element, project);
            break;
        }
    }

    private boolean isChildAffectedByChange(IJavaElementDelta delta) {
        return (delta.getFlags() & IJavaElementDelta.F_CHILDREN) != 0;
    }

    private void processElementAdded(IJavaElement element, IJavaProject project) {
        switch (element.getElementType()) {
        case JAVA_PROJECT:
        case PACKAGE_FRAGMENT_ROOT:
            rebuildIndex(project);
            break;
        }
    }

    private void processElementChanged(IJavaElementDelta delta, IJavaElement element, IJavaProject project) {
        if (PACKAGE_FRAGMENT_ROOT == element.getElementType()) {
            boolean removed = (delta.getFlags() & IJavaElementDelta.F_REMOVED_FROM_CLASSPATH) != 0;
            boolean content = (delta.getFlags() & IJavaElementDelta.F_ARCHIVE_CONTENT_CHANGED) != 0;
            if (removed || content) {
                rebuildIndex(project);
            }
        }
    }

    private void processElementRemoved(IJavaElement element, IJavaProject project) {
        switch (element.getElementType()) {
        case PACKAGE_FRAGMENT_ROOT:
            rebuildIndex(project);
            break;
        case JAVA_PROJECT:
            removeProjectIndex(project);
            break;
        }
    }

    private void rebuildIndex(IJavaProject project) {
        IProjectTypesIndex index = indexProvider.findIndex(project).orNull();
        if (index == null) {
            return;
        }
        index.suggestRebuild();
    }

    private void removeProjectIndex(IJavaProject project) {
        indexProvider.deleteIndex(project);
    }

    @Override
    public Set<String> subtypes(ITypeName expected, IJavaProject project) {
        IProjectTypesIndex index = indexProvider.findOrCreateIndex(project);
        return index.subtypes(expected);
    }

    private final class ShutdownListener implements IWorkbenchListener {
        @Override
        public boolean preShutdown(IWorkbench workbench, boolean forced) {
            return true;
        }

        @Override
        public void postShutdown(IWorkbench workbench) {
            try {
                indexProvider.close();
            } catch (IOException e) {
                Logs.log(LogMessages.ERROR_CLOSING_PROJECT_TYPES_INDEXES, e);
            }
        }
    }
}
