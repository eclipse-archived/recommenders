/**
 * Copyright (c) 2010 Darmstadt University of Technology. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.tests.rcp.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.junit.Test;

public class WorkspaceAndFixturesSetupSmokeTest {

    private static File fixturesBasedir = new File("../org.eclipse.recommenders.tests.fixtures/projects/");
    private static String fixtureProjectName = "org.eclipse.recommenders.tests.fixtures.empty-project";

    @Test
    public void testIsWorkspaceUp() throws Exception {
        final IWorkspaceRoot root = getWorkspaceRoot();
        assertNotNull(root);
    }

    @Test
    public void testIsFixturesBasedirAvailable() throws Exception {
        assertTrue(fixturesBasedir.exists());
    }

    @Test
    public void testIsWorkspaceEmpty() throws Exception {
        final IWorkspaceRoot root = getWorkspaceRoot();
        final IProject[] projects = root.getProjects();
        assertEquals(0, projects.length);

    }

    @Test
    public void testImportEmptyProject() throws Exception {
        final IWorkspaceRoot root = getWorkspaceRoot();
        final File wsDir = root.getLocation().toFile();
        final File destDir = new File(wsDir, fixtureProjectName);
        assertFalse(destDir.exists());
        final File srcDir = new File(fixturesBasedir, fixtureProjectName);
        FileUtils.copyDirectory(srcDir, destDir);
        final IProject project = root.getProject(fixtureProjectName);
        project.create(null);
        assertTrue(project.exists());
    }

    private IWorkspaceRoot getWorkspaceRoot() {
        final IWorkspace workspace = ResourcesPlugin.getWorkspace();
        final IWorkspaceRoot root = workspace.getRoot();
        return root;
    }

}
