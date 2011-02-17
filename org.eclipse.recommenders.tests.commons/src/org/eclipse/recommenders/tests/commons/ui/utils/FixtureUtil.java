/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 */
package org.eclipse.recommenders.tests.commons.ui.utils;

import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

public class FixtureUtil {

    private static File fixturesBasedir = new File("../org.eclipse.recommenders.tests.fixtures/");

    public static IProject copyProjectToWorkspace(final String projectName) throws IOException, CoreException {
        final IWorkspaceRoot root = getWorkspaceRoot();
        final File wsDir = root.getLocation().toFile();
        final File destDir = new File(wsDir, projectName);
        assertFalse(destDir.exists());
        final File srcDir = new File(new File(fixturesBasedir, "projects"), projectName);
        FileUtils.copyDirectory(srcDir, destDir);
        final IProject project = root.getProject(projectName);
        project.create(null);
        project.open(null);
        project.refreshLocal(IProject.DEPTH_INFINITE, null);
        project.build(IncrementalProjectBuilder.FULL_BUILD, null);
        return project;
    }

    public static IWorkspaceRoot getWorkspaceRoot() {
        final IWorkspace workspace = ResourcesPlugin.getWorkspace();
        final IWorkspaceRoot root = workspace.getRoot();
        return root;
    }
}
