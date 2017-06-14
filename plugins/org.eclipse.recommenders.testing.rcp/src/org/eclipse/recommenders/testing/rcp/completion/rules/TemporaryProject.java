/**
 * Copyright (c) 2015 Codetrails GmbH. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Simon Laffoy - initial API and implementation.
 */
package org.eclipse.recommenders.testing.rcp.completion.rules;

import static java.io.File.separator;
import static java.util.Arrays.asList;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.recommenders.utils.Constants;
import org.eclipse.recommenders.utils.Nonnull;
import org.eclipse.recommenders.utils.Throws;
import org.eclipse.recommenders.utils.Zips;

public class TemporaryProject {

    static final String BIN_FOLDER_NAME = "bin";
    static final String SRC_FOLDER_NAME = "src";
    static final String JAR_FOLDER_NAME = "jar";

    private final Set<TemporaryFile> temporaryFiles = new HashSet<>();
    private final IWorkspace workspace;
    private final String name;
    private final IProject project;

    private IJavaProject javaProject;

    TemporaryProject(TemporaryWorkspace ws, String name) {
        this.workspace = ws.getWorkspace();
        this.name = name;
        this.project = workspace.getRoot().getProject(name);

        createProject();
    }

    private void createProject() {
        final IWorkspaceRunnable populate = new IWorkspaceRunnable() {

            @Override
            public void run(final IProgressMonitor monitor) throws CoreException {
                createAndOpenProject(project);

                if (!hasJavaNature(project)) {
                    addJavaNature(project);
                    addToClasspath(JavaRuntime.getDefaultJREContainerEntry());
                    addSourcePackageFragmentRoot(project);
                }
            }

            private void createAndOpenProject(IProject project) throws CoreException {
                if (!project.exists()) {
                    project.create(null);
                }
                project.open(null);
            }

            private boolean hasJavaNature(final IProject project) throws CoreException {
                final IProjectDescription description = project.getDescription();
                final String[] natures = description.getNatureIds();
                return ArrayUtils.contains(natures, JavaCore.NATURE_ID);
            }

            private void addJavaNature(final IProject project) throws CoreException {
                final IProjectDescription description = project.getDescription();
                final String[] natures = description.getNatureIds();
                final String[] newNatures = ArrayUtils.add(natures, JavaCore.NATURE_ID);

                description.setNatureIds(newNatures);
                project.setDescription(description, null);
                javaProject = JavaCore.create(project);
            }

            private void addSourcePackageFragmentRoot(IProject project) throws CoreException {
                // create the source folder
                IFolder sourceFolder = project.getFolder(SRC_FOLDER_NAME);
                sourceFolder.create(false, true, null);

                // replace the classpath's project root entry with the src folder
                IPackageFragmentRoot src = javaProject.getPackageFragmentRoot(sourceFolder);
                IClasspathEntry[] entries = javaProject.getRawClasspath();

                for (int i = 0; i < entries.length; i++) {
                    if (entries[i].getPath().toString().equals(separator + name)) {
                        entries[i] = JavaCore.newSourceEntry(src.getPath());
                        break;
                    }
                }

                javaProject.setRawClasspath(entries, null);
            }
        };

        try {
            workspace.run(populate, null);
        } catch (final Exception e) {
            e.printStackTrace();
            Throws.throwUnhandledException(e);
        }
        javaProject = JavaCore.create(project);
    }

    public TemporaryProject withDependencyOn(TemporaryProject dependency) throws JavaModelException {
        addToClasspath(JavaCore.newProjectEntry(dependency.getProject().getFullPath()));
        return this;
    }

    public TemporaryProject withDependencyOnClassesOf(TemporaryProject dependency) throws JavaModelException {
        IFolder classFileFolder = dependency.getProjectClassFileDirectory();
        addToClasspath(JavaCore.newLibraryEntry(classFileFolder.getFullPath(), null, null));
        return this;
    }

    public TemporaryProject withDependencyOnJarOf(TemporaryProject dependency) throws IOException, JavaModelException {
        addToClasspath(JavaCore.newLibraryEntry(dependency.createJar(), null, null));
        return this;
    }

    public TemporaryFile createFile(CharSequence code) throws CoreException {
        TemporaryFile tempFile = new TemporaryFile(this, code);
        temporaryFiles.add(tempFile);
        return tempFile;
    }

    private void addToClasspath(@Nonnull final IClasspathEntry classpathEntry) throws JavaModelException {
        final Set<IClasspathEntry> entries = new HashSet<>();

        entries.addAll(asList(javaProject.getRawClasspath()));
        entries.add(classpathEntry);

        IClasspathEntry[] classpaths = entries.toArray(new IClasspathEntry[entries.size()]);
        javaProject.setRawClasspath(classpaths, null);
    }

    private IFolder getProjectClassFileDirectory() {
        return project.getFolder(BIN_FOLDER_NAME);
    }

    private Path createJar() throws IOException {
        String jarPath = getJarPath();
        String pathToClassDirectory = this.getAbsolutePathString() + separator + BIN_FOLDER_NAME;
        File classFileDirectory = new File(pathToClassDirectory);

        Zips.zip(classFileDirectory, new File(jarPath));

        return new Path(jarPath);
    }

    public String getJarPath() {
        return this.getAbsolutePathString() + separator + this.getName() + Constants.DOT_JAR;
    }

    String getName() {
        return name;
    }

    IProject getProject() {
        return project;
    }

    public IJavaProject getJavaProject() {
        return javaProject;
    }

    String getAbsolutePathString() {
        return project.getLocation().toString();
    }

    String getWorkspaceRelativePathString() {
        return project.getFullPath().toString();
    }

    void refreshAndBuildProject() throws CoreException {
        project.refreshLocal(IResource.DEPTH_INFINITE, null);
        project.build(IncrementalProjectBuilder.FULL_BUILD, null);
    }
}
