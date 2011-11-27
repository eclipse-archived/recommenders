/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.tests.jdt;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static org.eclipse.recommenders.tests.jdt.AstUtils.MARKER;
import static org.eclipse.recommenders.utils.Checks.cast;
import static org.eclipse.recommenders.utils.Throws.throwUnhandledException;

import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.recommenders.utils.Tuple;

import com.google.common.collect.Sets;

public class JavaProjectFixture {

    private IJavaProject javaProject;
    private ASTParser parser;

    public JavaProjectFixture(final IWorkspace workspace, final String projectName) {
        createJavaProject(workspace, projectName);
        createParser();
    }

    private void createParser() {
        parser = ASTParser.newParser(AST.JLS3);
        // parser.setEnvironment(...) enables bindings resolving
        parser.setProject(javaProject); // enables bindings and IJavaElement resolving
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setResolveBindings(true);
    }

    private void createJavaProject(final IWorkspace workspace, final String projectName) {
        final IProject project = workspace.getRoot().getProject(projectName);
        final IWorkspaceRunnable populate = new IWorkspaceRunnable() {

            @Override
            public void run(final IProgressMonitor monitor) throws CoreException {
                createAndOpenProject(project);

                if (!hasJavaNature(project)) {
                    addJavaNature(project);
                    configureProjectClasspath();
                }
            }

            private void createAndOpenProject(final IProject project) throws CoreException {
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

            private void configureProjectClasspath() throws JavaModelException {
                final Set<IClasspathEntry> entries = newHashSet();
                final IClasspathEntry[] rawClasspath = javaProject.getRawClasspath();
                final IClasspathEntry defaultJREContainerEntry = JavaRuntime.getDefaultJREContainerEntry();
                entries.addAll(asList(rawClasspath));
                entries.add(defaultJREContainerEntry);
                final IClasspathEntry[] entriesArray = entries.toArray(new IClasspathEntry[entries.size()]);
                javaProject.setRawClasspath(entriesArray, null);
            }

            private void addJavaNature(final IProject project) throws CoreException {
                final IProjectDescription description = project.getDescription();
                final String[] natures = description.getNatureIds();
                final String[] newNatures = ArrayUtils.add(natures, JavaCore.NATURE_ID);
                description.setNatureIds(newNatures);
                project.setDescription(description, null);
                javaProject = JavaCore.create(project);

            }
        };
        try {
            workspace.run(populate, null);
        } catch (final Exception e) {
            throwUnhandledException(e);
        }
        javaProject = JavaCore.create(project);
    }

    /**
     * @param fileName
     *            should match the name of the primary type given in the content, i.e., if content = "class X {}" �
     *            unitName = "X.java".
     */
    public CompilationUnit parse(final String content, final String fileName) {

        parser.setSource(content.toCharArray());
        parser.setUnitName(fileName);
        final CompilationUnit cu = cast(parser.createAST(null));
        return cu;
    }

    public Tuple<CompilationUnit, Set<Integer>> parseWithMarkers(final String content, final String fileName) {
        final Set<Integer> markers = Sets.newTreeSet();

        int pos = 0;
        final StringBuilder sb = new StringBuilder(content);
        while ((pos = sb.indexOf(MARKER, pos)) != -1) {
            sb.delete(pos, pos + 1);
            markers.add(pos);
        }

        final CompilationUnit cu = parse(sb.toString(), fileName);
        return Tuple.newTuple(cu, markers);
    }

}
