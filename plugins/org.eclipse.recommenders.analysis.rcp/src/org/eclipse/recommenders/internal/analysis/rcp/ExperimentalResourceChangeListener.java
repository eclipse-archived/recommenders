/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Marcel Bruch - Initial API and Implementation
 */
package org.eclipse.recommenders.internal.analysis.rcp;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.runtime.IPath;

public class ExperimentalResourceChangeListener implements IResourceChangeListener {

    @Override
    public void resourceChanged(final IResourceChangeEvent event) {
        // final IResourceDelta delta = event.getDelta();
        // switch (delta.getKind()) {
        // case IResourceDelta.OPEN:
        // fireProjectOpenEvent((IProject) delta.getResource());
        // }

        switch (event.getType()) {
        case IResourceChangeEvent.PRE_CLOSE:
            handlePreProjectCloseEvent(event);
            break;
        case IResourceChangeEvent.PRE_DELETE:
            handlePreProjectDeleteEvent(event);
            break;
        case IResourceChangeEvent.POST_BUILD:
            handlePostBuildSingleResourceChange(event);
        default:
            break;
        }
    }

    private void handlePreProjectDeleteEvent(final IResourceChangeEvent event) {
        System.out.println(event + " project delete");
    }

    private void handlePostBuildSingleResourceChange(final IResourceChangeEvent event) {
        final IPath fullPath = event.getDelta().getFullPath();
        final IResource resource = event.getResource();
        // ensureIsNotNull(resource);
        // ensureIsNotInstanceOf(resource, IWorkspace.class);
        if (resource == null) {
            return;
        }
        System.out.println(event + "resource changed");

    }

    private void handlePreProjectCloseEvent(final IResourceChangeEvent event) {
        System.out.println(event + " project close");
    }
}
