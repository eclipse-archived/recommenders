/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Olav Lenz - initial API and implementation
 */
package org.eclipse.recommenders.internal.models.rcp;

import static com.google.common.collect.ObjectArrays.concat;
import static org.eclipse.jdt.core.IJavaElement.JAVA_PROJECT;
import static org.eclipse.recommenders.internal.models.rcp.Dependencies.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import java.io.File;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.recommenders.models.DependencyInfo;
import org.eclipse.recommenders.models.DependencyType;
import org.eclipse.recommenders.rcp.JavaModelEvents.JarPackageFragmentRootAdded;
import org.eclipse.recommenders.rcp.JavaModelEvents.JarPackageFragmentRootRemoved;
import org.eclipse.recommenders.rcp.JavaModelEvents.JavaProjectClosed;
import org.eclipse.recommenders.rcp.JavaModelEvents.JavaProjectOpened;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.eventbus.EventBus;

@SuppressWarnings("restriction")
public class EclipseDependencyListenerTest {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    @Test
    public void testInitialWorkspaceParsing() throws Exception {
        IJavaProject javaProject = createProject("initialWorkspaceParsing");

        EventBus eventBus = mock(EventBus.class);
        EclipseDependencyListener sut = new EclipseDependencyListener(eventBus);

        DependencyInfo project = Dependencies.createDependencyInfoForProject(javaProject);
        assertThat(sut.getProjects(), hasItem(project));
        assertThat(sut.getDependencies(), hasItem(project));
        assertThat(sut.getDependenciesForProject(project), hasItem(project));
    }

    @Test
    public void testProjectAddedOnJavaProjectOpened() throws Exception {
        EventBus eventBus = new EventBus();
        EclipseDependencyListener sut = new EclipseDependencyListener(eventBus);

        IJavaProject javaProject = createProject("ProjectAddedOnJavaProjectOpened");

        DependencyInfo project = Dependencies.createDependencyInfoForProject(javaProject);
        assertThat(sut.getDependencies(), not(hasItem(project)));
        assertThat(sut.getProjects(), not(hasItem(project)));
        assertThat(sut.getDependenciesForProject(project), not(hasItem(project)));

        eventBus.post(new JavaProjectOpened(javaProject));

        assertThat(sut.getProjects(), hasItem(project));
        assertThat(sut.getDependencies(), hasItem(project));
        assertThat(sut.getDependenciesForProject(project), hasItem(project));
    }

    @Test
    public void testProjectRemovedOnJavaProjectClosed() throws Exception {
        EventBus eventBus = new EventBus();
        EclipseDependencyListener sut = new EclipseDependencyListener(eventBus);

        IJavaProject javaProject = createProject("ProjectRemovedOnJavaProjectClosed");
        eventBus.post(new JavaProjectOpened(javaProject));
        eventBus.post(new JavaProjectClosed(javaProject));

        DependencyInfo project = Dependencies.createDependencyInfoForProject(javaProject);
        assertThat(sut.getDependencies(), not(hasItem(project)));
        assertThat(sut.getProjects(), not(hasItem(project)));
        assertThat(sut.getDependenciesForProject(project), not(hasItem(project)));
    }

    @Test
    public void testJreAddedOnJavaProjectOpened() throws Exception {
        EventBus eventBus = new EventBus();
        EclipseDependencyListener sut = new EclipseDependencyListener(eventBus);

        IJavaProject javaProject = createProject("JreAddedOnJavaProjectOpened");
        appendJreToClasspath(javaProject);
        eventBus.post(new JavaProjectOpened(javaProject));

        DependencyInfo project = createDependencyInfoForProject(javaProject);
        DependencyInfo jre = createDependencyInfoForJre(javaProject).get();
        assertThat(sut.getDependencies(), hasItem(jre));
        assertThat(sut.getProjects(), not(hasItem(jre)));
        assertThat(sut.getDependenciesForProject(project), hasItem(jre));
    }

    @Test
    public void testJrePresentUntilLastJavaProjectClosed() throws Exception {
        EventBus eventBus = new EventBus();
        EclipseDependencyListener sut = new EclipseDependencyListener(eventBus);

        IJavaProject javaProject1 = createProject("JrePresentUntilLastJavaProjectClosed_1");
        appendJreToClasspath(javaProject1);
        eventBus.post(new JavaProjectOpened(javaProject1));
        IJavaProject javaProject2 = createProject("JrePresentUntilLastJavaProjectClosed_2");
        appendJreToClasspath(javaProject2);
        eventBus.post(new JavaProjectOpened(javaProject2));

        eventBus.post(new JavaProjectClosed(javaProject1));

        DependencyInfo project2 = createDependencyInfoForProject(javaProject2);
        DependencyInfo jre = createDependencyInfoForJre(javaProject2).get();
        assertThat(sut.getDependencies(), hasItem(jre));
        assertThat(sut.getDependenciesForProject(project2), hasItem(jre));
    }

    @Test
    public void testJreDependenciesAreDeletedWhenOneJarFromJreIsRemoved() throws Exception {
        EventBus eventBus = new EventBus();
        EclipseDependencyListener sut = new EclipseDependencyListener(eventBus);

        IJavaProject javaProject = createProject("JreDependenciesAreDeletedWhenOneJarFromJreIsRemoved");
        appendJreToClasspath(javaProject);
        eventBus.post(new JavaProjectOpened(javaProject));

        Set<IPackageFragmentRoot> detectJREPackageFragementRoots = EclipseDependencyListener
                .detectJREPackageFragementRoots(javaProject);
        for (IPackageFragmentRoot packageFragmentRoot : detectJREPackageFragementRoots) {
            if (packageFragmentRoot instanceof JarPackageFragmentRoot) {
                eventBus.post(new JarPackageFragmentRootRemoved((JarPackageFragmentRoot) packageFragmentRoot));
                break;
            }
        }

        DependencyInfo project = createDependencyInfoForProject(javaProject);
        DependencyInfo jre = createDependencyInfoForJre(javaProject).get();
        assertThat(sut.getDependenciesForProject(project), not(hasItem(jre)));
    }

    @Test
    public void testDependencyForJarIsAddedCorrectly() throws Exception {
        EventBus eventBus = new EventBus();
        EclipseDependencyListener sut = new EclipseDependencyListener(eventBus);

        IJavaProject javaProject = createProject("DependencyForJarIsAddedCorrectly");
        File dependency = tmp.newFile("dependency.jar");
        eventBus.post(new JarPackageFragmentRootAdded(mockJarPackageFragmentRoot(javaProject, dependency)));

        DependencyInfo project = createDependencyInfoForProject(javaProject);
        DependencyInfo jar = new DependencyInfo(dependency, DependencyType.JAR);
        assertThat(sut.getDependencies(), hasItem(jar));
        assertThat(sut.getDependenciesForProject(project), hasItem(jar));
    }

    @Test
    public void testDependencyForJarIsRemovedCorrectly() throws Exception {
        EventBus eventBus = new EventBus();
        EclipseDependencyListener sut = new EclipseDependencyListener(eventBus);

        IJavaProject javaProject = createProject("DependencyForJarIsRemovedCorrectly");
        File dependency = tmp.newFile("dependency.jar");
        eventBus.post(new JarPackageFragmentRootAdded(mockJarPackageFragmentRoot(javaProject, dependency)));
        eventBus.post(new JarPackageFragmentRootRemoved(mockJarPackageFragmentRoot(javaProject, dependency)));

        DependencyInfo project = createDependencyInfoForProject(javaProject);
        DependencyInfo jar = new DependencyInfo(dependency, DependencyType.JAR);
        assertThat(sut.getDependencies(), not(hasItem(jar)));
        assertThat(sut.getDependenciesForProject(project), not(hasItem(jar)));
    }

    @Test
    public void testDependencyForProjectIsAddedCorrectly() throws Exception {
        EventBus eventBus = new EventBus();
        EclipseDependencyListener sut = new EclipseDependencyListener(eventBus);

        IJavaProject javaProject = createProject("DependencyForProjectIsAddedCorrectly");
        IJavaProject javaDependency = createProject("DependencyForProjectIsAddedCorrectly_dependency");
        appendJavaProjectToClasspath(javaProject, javaDependency);
        eventBus.post(new JavaProjectOpened(javaProject));
        eventBus.post(new JavaProjectOpened(javaDependency));

        DependencyInfo project = createDependencyInfoForProject(javaProject);
        DependencyInfo dependency = createDependencyInfoForProject(javaDependency);
        assertThat(sut.getProjects(), hasItems(project, dependency));
        assertThat(sut.getDependencies(), hasItems(project, dependency));
        assertThat(sut.getDependenciesForProject(project), hasItems(project, dependency));
        assertThat(sut.getDependenciesForProject(dependency), hasItem(dependency));
        assertThat(sut.getDependenciesForProject(dependency), not(hasItem(project)));
    }

    @Test
    public void testDependencyForProjectIsRemovedCorrectly() throws Exception {
        EventBus eventBus = new EventBus();
        EclipseDependencyListener sut = new EclipseDependencyListener(eventBus);

        IJavaProject javaProject = createProject("DependencyForProjectIsRemovedCorrectly");
        IJavaProject javaDependency = createProject("DependencyForProjectIsRemovedCorrectly_dependency");
        appendJavaProjectToClasspath(javaProject, javaDependency);
        eventBus.post(new JavaProjectOpened(javaProject));
        eventBus.post(new JavaProjectOpened(javaDependency));
        eventBus.post(new JavaProjectClosed(javaDependency));

        DependencyInfo project = createDependencyInfoForProject(javaProject);
        assertThat(sut.getProjects(), hasItems(project));
        assertThat(sut.getDependencies(), hasItems(project));
        assertThat(sut.getDependenciesForProject(project), hasItems(project));
    }

    private static IJavaProject createProject(String projectName) throws Exception {
        IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
        IProject project = workspaceRoot.getProject(projectName);
        project.create(null);
        project.open(null);
        IProjectDescription description = project.getDescription();
        description.setNatureIds(new String[] { JavaCore.NATURE_ID });
        project.setDescription(description, null);
        return JavaCore.create(project);
    }

    private static void appendJreToClasspath(IJavaProject javaProject) throws Exception {
        IClasspathEntry jreEntry = JavaRuntime.getDefaultJREContainerEntry();
        javaProject.setRawClasspath(concat(javaProject.getRawClasspath(), jreEntry), new NullProgressMonitor());
    }

    private static void appendJavaProjectToClasspath(IJavaProject javaProject, IJavaProject dependency)
            throws Exception {
        IClasspathEntry sourceEntry = JavaCore.newProjectEntry(dependency.getPath());
        javaProject.setRawClasspath(concat(javaProject.getRawClasspath(), sourceEntry), new NullProgressMonitor());
    }

    private JarPackageFragmentRoot mockJarPackageFragmentRoot(final IJavaProject javaProject, final File file) {
        JarPackageFragmentRoot mock = mock(JarPackageFragmentRoot.class, RETURNS_DEEP_STUBS);
        when(mock.getParent()).thenReturn(javaProject);
        when(mock.getPath().toFile()).thenReturn(file);
        when(mock.getAncestor(JAVA_PROJECT)).thenReturn(javaProject);
        when(mock.isExternal()).thenReturn(true);
        return mock;
    }
}
