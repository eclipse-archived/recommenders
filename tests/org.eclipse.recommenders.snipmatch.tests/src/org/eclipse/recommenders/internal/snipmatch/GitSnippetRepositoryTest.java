/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Dorn - initial API and implementation.
 */
package org.eclipse.recommenders.internal.snipmatch;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jgit.api.Git;
import org.eclipse.recommenders.snipmatch.GitSnippetRepository;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GitSnippetRepositoryTest {

    private static final String FIRST_FILE = "first-file";

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Mock
    private IWorkspaceRoot root;

    @Mock
    private IProject project;

    private Git git;
    private File remotePath;

    private GitSnippetRepository sut;
    private File firstFile;
    private File basedir;

    @Before
    public void init() throws Exception {
        remotePath = tempFolder.newFolder("remote-snipmatch");
        basedir = tempFolder.newFolder("local-snipmatch");

        git = Git.init().setDirectory(remotePath).call();
        addFileToRemote(FIRST_FILE, remotePath, git);

        firstFile = new File(basedir, FIRST_FILE);

        IProjectDescription projectDescription = mock(IProjectDescription.class);
        when(project.getDescription()).thenReturn(projectDescription);
        when(projectDescription.getNatureIds()).thenReturn(new String[0]);

        IPath path = new Path(tempFolder.getRoot().getAbsolutePath());
        when(root.getLocation()).thenReturn(path);
        when(root.getProject(anyString())).thenReturn(project);

        sut = new GitSnippetRepository(basedir, remotePath.getAbsolutePath());
    }

    private void addFileToRemote(String filename, File remote, Git git) throws Exception {
        File file = new File(remote, filename);
        file.createNewFile();
        git.add().addFilepattern(filename).call();
        git.commit().setMessage("commit message").call();
    }

    @Test
    public void testinitialClone() throws Exception {
        sut.open();
        assertTrue(firstFile.exists());
    }

}