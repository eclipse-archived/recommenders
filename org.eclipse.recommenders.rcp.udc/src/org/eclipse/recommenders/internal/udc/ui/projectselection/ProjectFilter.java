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

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

public class ProjectFilter extends ViewerFilter {
    public ProjectFilter(final String filterText) {
        super();
        this.filterText = filterText;
    }

    private final String filterText;

    @Override
    public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
        final IProject project = (IProject) element;
        return project.getName().contains(filterText);
    }
}
