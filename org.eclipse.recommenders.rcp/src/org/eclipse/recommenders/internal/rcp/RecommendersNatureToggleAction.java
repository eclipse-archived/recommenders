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

import static org.apache.commons.lang3.ArrayUtils.add;
import static org.apache.commons.lang3.ArrayUtils.remove;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.recommenders.rcp.RecommendersPlugin;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

public class RecommendersNatureToggleAction implements IObjectActionDelegate {

    private ISelection selection;

    @Override
    public void run(final IAction action) {
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection s = (IStructuredSelection) selection;
            for (Object element : s.toList()) {

                IProject project = null;
                if (element instanceof IProject) {
                    project = (IProject) element;
                } else if (element instanceof IAdaptable) {
                    project = (IProject) ((IAdaptable) element).getAdapter(IProject.class);
                }
                if (canToggleNature(project)) {
                    toggleNature(project);
                }
            }
        }
    }

    private boolean canToggleNature(IProject project) {
        return project != null && project.isAccessible();
    }

    private void toggleNature(final IProject project) {
        try {
            final IProjectDescription description = project.getDescription();
            String[] natures = description.getNatureIds();
            int index = ArrayUtils.indexOf(natures, IDs.NATURE_ID);
            natures = alreadyHasRecommendersNature(index) ? remove(natures, index) : add(natures, IDs.NATURE_ID);
            description.setNatureIds(natures);
            project.setDescription(description, null);
        } catch (final CoreException e) {
            RecommendersPlugin.logError(e, "Failed to set recommenders nature to project '%s'", project.getName());
        }
    }

    private boolean alreadyHasRecommendersNature(int indexOf) {
        return indexOf > -1;
    }

    @Override
    public void selectionChanged(final IAction action, final ISelection selection) {
        this.selection = selection;
    }

    @Override
    public void setActivePart(final IAction action, final IWorkbenchPart targetPart) {
    }

}