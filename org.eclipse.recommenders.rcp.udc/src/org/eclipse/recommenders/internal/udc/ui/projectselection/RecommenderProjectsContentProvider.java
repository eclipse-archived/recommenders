/**
 * Copyright (c) 2011 Andreas Frankenberger.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API and implementation.
 */
package org.eclipse.recommenders.internal.udc.ui.projectselection;

/**
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API and implementation.
 */
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.recommenders.internal.udc.ProjectProvider;

public final class RecommenderProjectsContentProvider implements ITreeContentProvider {
    @Override
    public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {

    }

    @Override
    public void dispose() {

    }

    @Override
    public boolean hasChildren(final Object element) {
        return false;
    }

    @Override
    public Object getParent(final Object element) {
        return null;
    }

    @Override
    public IProject[] getElements(final Object inputElement) {
        return new ProjectProvider().getAllRecommenderProjectsInWorkspace();
    }

    @Override
    public Object[] getChildren(final Object parentElement) {
        return null;
    }
}