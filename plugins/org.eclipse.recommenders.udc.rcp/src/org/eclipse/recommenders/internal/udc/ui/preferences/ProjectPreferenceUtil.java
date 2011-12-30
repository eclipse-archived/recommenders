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
package org.eclipse.recommenders.internal.udc.ui.preferences;

/**
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API and implementation.
 */
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.recommenders.internal.udc.Activator;
import org.eclipse.recommenders.internal.udc.PreferenceKeys;
import org.eclipse.recommenders.internal.udc.ProjectProvider;

public class ProjectPreferenceUtil {
    public static enum NewProjectHandling {
        ask, enable, ignore
    }

    public static void setExportEnabled(final IProject project, final boolean enabled) {
        final QualifiedName key = new QualifiedName(Activator.PLUGIN_ID, "exportproject");
        try {
            project.setPersistentProperty(key, "" + enabled);
        } catch (final CoreException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param project
     * @return true, false or null if property is not set on this project
     */
    public static Boolean isExportEnabled(final IProject project) {
        final QualifiedName key = new QualifiedName(Activator.PLUGIN_ID, "exportproject");
        String property;
        try {
            property = project.getPersistentProperty(key);
        } catch (final CoreException e) {
            throw new RuntimeException(e);
        }
        if (property == null) {
            return null;
        }
        return Boolean.valueOf(property);
    }

    public static void setProjectsEnabledForExport(final IProject[] enabledProjects) {
        final Collection<IProject> selectedProjects = Arrays.asList(enabledProjects);
        saveProjects(selectedProjects, true);

        final HashSet<IProject> notSelectedProjects = new HashSet<IProject>(Arrays.asList(getAllProjects()));
        notSelectedProjects.removeAll(selectedProjects);
        saveProjects(notSelectedProjects, false);
    }

    private static IProject[] getAllProjects() {
        return new ProjectProvider().getAllRecommenderProjectsInWorkspace();
    }

    private static void saveProjects(final Collection<IProject> projects, final boolean b) {
        for (final IProject project : projects) {
            setExportEnabled(project, b);
        }
    }

    public static NewProjectHandling getNewProjectHandling() {
        return NewProjectHandling.valueOf(new InstanceScope().getNode(Activator.PLUGIN_ID).get(
                PreferenceKeys.newProjectHandling, NewProjectHandling.ask.toString()));
    }

    public static void setNewProjectHandling(final NewProjectHandling newProjectHandling) {
        new InstanceScope().getNode(Activator.PLUGIN_ID).put(PreferenceKeys.newProjectHandling,
                newProjectHandling.toString());
    }

}
