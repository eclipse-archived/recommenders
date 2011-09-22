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
package org.eclipse.recommenders.internal.udc;

/**
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API and implementation.
 */
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.recommenders.internal.udc.ui.preferences.ProjectPreferenceUtil;
import org.eclipse.recommenders.rcp.analysis.RecommendersNature;

public class ProjectProvider {

    public IProject[] getAllRecommenderProjectsInWorkspace() {
        IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        projects = filterProjects(projects);
        return projects;
    }

    /**
     * Returns an array of projects with recommenders nature
     * 
     * @param projects
     * @return
     */
    private IProject[] filterProjects(final IProject[] projects) {
        if (projects == null) {
            throw new IllegalArgumentException("projects must not be null");
        }
        final ArrayList<IProject> result = new ArrayList<IProject>();
        for (final IProject project : projects) {
            if (!project.isAccessible()) {
                continue;
            }
            if (RecommendersNature.hasNature(project)) {
                result.add(project);
            }
        }
        return result.toArray(new IProject[result.size()]);
    }

    public IProject[] getProjectsEnabledForExport() {
        return getProjectsEnabledForExport(getAllRecommenderProjectsInWorkspace());
    }

    public IProject[] getProjectsEnabledForExport(final IProject[] projects) {
        final List<IProject> result = new ArrayList<IProject>();
        for (final IProject project : projects) {
            final Boolean exportEnabled = ProjectPreferenceUtil.isExportEnabled(project);
            if (exportEnabled != null && exportEnabled) {
                result.add(project);
            }
        }
        return result.toArray(new IProject[result.size()]);
    }

    public IProject[] getNewProjects(final IProject[] allProjects) {
        final List<IProject> result = new ArrayList<IProject>();
        for (final IProject project : allProjects) {
            if (ProjectPreferenceUtil.isExportEnabled(project) == null) {
                result.add(project);
            }
        }
        return result.toArray(new IProject[result.size()]);
    }
}
