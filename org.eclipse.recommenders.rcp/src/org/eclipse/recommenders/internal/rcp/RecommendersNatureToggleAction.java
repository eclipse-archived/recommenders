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

import static org.eclipse.recommenders.commons.utils.Throws.throwUnreachable;

import java.util.LinkedList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.recommenders.rcp.utils.RCPUtils;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.google.common.collect.Lists;

public class RecommendersNatureToggleAction implements IObjectActionDelegate {

    private LinkedList<IProject> projectsWithNature;
    private LinkedList<IProject> projectsWithoutNature;

    @Override
    public void run(final IAction action) {
        for (final IProject project : projectsWithNature) {
            RecommendersNature.removeNature(project);
        }
        for (final IProject project : projectsWithoutNature) {
            RecommendersNature.addNature(project);
        }
    }

    private boolean canToggleNature(final IProject project) {
        return project != null && project.isAccessible();
    }

    @Override
    public void selectionChanged(final IAction action, final ISelection selection) {
        computeAccesibleProjectsFromCurrentSelection(selection);
        computeNewActionText(action);
    }

    private void computeNewActionText(final IAction action) {
        if (projectsWithNature.isEmpty() && projectsWithoutNature.isEmpty()) {
            throwUnreachable("no project selected! Configuration mistake in plugin.xml.");
        } else if (!projectsWithNature.isEmpty() && !projectsWithoutNature.isEmpty()) {
            action.setText("Flip Recommenders Natures");
        } else if (!projectsWithNature.isEmpty()) {
            action.setText("Remove Recommenders Nature");
        } else {
            action.setText("Add Recommenders Nature");
        }
    }

    private void computeAccesibleProjectsFromCurrentSelection(final ISelection selection) {
        projectsWithNature = Lists.newLinkedList();
        projectsWithoutNature = Lists.newLinkedList();

        final StructuredSelection s = RCPUtils.asStructuredSelection(selection);
        for (final Object element : s.toList()) {
            IProject project = null;
            if (element instanceof IProject) {
                project = (IProject) element;
            } else if (element instanceof IAdaptable) {
                project = (IProject) ((IAdaptable) element).getAdapter(IProject.class);
            }
            if (!canToggleNature(project)) {
                continue;
            }
            if (RecommendersNature.hasNature(project)) {
                projectsWithNature.add(project);
            } else {
                projectsWithoutNature.add(project);
            }
        }
    }

    @Override
    public void setActivePart(final IAction action, final IWorkbenchPart targetPart) {
    }

}