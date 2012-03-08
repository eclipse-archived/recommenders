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

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static junit.framework.Assert.assertTrue;
import static org.eclipse.recommenders.tests.jdt.AstUtils.MARKER;
import static org.eclipse.recommenders.tests.jdt.AstUtils.MARKER_ESCAPE;
import static org.eclipse.recommenders.utils.Checks.cast;
import static org.eclipse.recommenders.utils.Checks.ensureIsTrue;
import static org.eclipse.recommenders.utils.Throws.throwUnhandledException;
import static org.eclipse.recommenders.utils.Tuple.newTuple;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
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

    public static String findClassName(final CharSequence source) {
        Pattern p = Pattern.compile(".*?class\\s+(\\w+).*", Pattern.DOTALL);
        Matcher matcher = p.matcher(source);
        if (!matcher.matches()) {
            p = Pattern.compile(".*interface\\s+(\\w+).*", Pattern.DOTALL);
            matcher = p.matcher(source);
        }
        assertTrue(matcher.matches());
        final String group = matcher.group(1);
        return group;
    }

    public static List<String> findInnerClassNames(final CharSequence source) {
        String declaringType = findClassName(source);
        List<String> names = newArrayList();

        Pattern p = Pattern.compile("(class|interface)\\s+(\\w+)", Pattern.DOTALL);
        Matcher matcher = p.matcher(source);
        while (matcher.find()) {
            final String name = matcher.group(2);
            if (!name.equals(declaringType)) {
                names.add(declaringType + "$" + name);
            }
        }
        return names;
    }

    public static List<String> findAnonymousClassNames(final CharSequence source) {
        String declaringType = findClassName(source);
        int num = 1;
        List<String> names = newArrayList();

        Pattern p = Pattern.compile("new\\s+(\\w+).*?\\)\\s+\\{", Pattern.DOTALL);
        Matcher matcher = p.matcher(source);
        while (matcher.find()) {
            final String name = matcher.group(1);
            if (!name.equals(declaringType)) {
                names.add(declaringType + "$" + num++);
            }
        }
        return names;
    }

    private IJavaProject javaProject;
    private ASTParser parser;

    public JavaProjectFixture(final IWorkspace workspace, final String projectName) {
        createJavaProject(workspace, projectName);
        createParser();
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

    private void createParser() {
        parser = ASTParser.newParser(AST.JLS3);
        // parser.setEnvironment(...) enables bindings resolving
        parser.setProject(javaProject); // enables bindings and IJavaElement
                                        // resolving
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setResolveBindings(true);
    }

    public Tuple<CompilationUnit, Set<Integer>> parseWithMarkers(final String content) {
        final Tuple<String, Set<Integer>> contentMarkersPair = findMarkers(content);
        final String contentWoMarkers = contentMarkersPair.getFirst();
        final Set<Integer> markers = contentMarkersPair.getSecond();
        final CompilationUnit cu = parse(contentWoMarkers);
        return newTuple(cu, markers);
    }

    public Tuple<String, Set<Integer>> findMarkers(final CharSequence content) {
        final Set<Integer> markers = Sets.newTreeSet();
        int pos = 0;
        final StringBuilder sb = new StringBuilder(content);
        while ((pos = sb.indexOf(MARKER, pos)) != -1) {
            sb.deleteCharAt(pos);
            markers.add(pos);
            ensureIsTrue(pos < sb.length());
            pos--;
        }
        return newTuple(sb.toString(), markers);
    }

    public CompilationUnit parse(final String content) {

        parser.setSource(content.toCharArray());
        parser.setUnitName(findClassName(content) + ".java");
        final CompilationUnit cu = cast(parser.createAST(null));
        return cu;
    }

    public Tuple<ICompilationUnit, Set<Integer>> createFileAndParseWithMarkers(final CharSequence contentWithMarkers)
            throws CoreException {
        final Tuple<String, Set<Integer>> content = findMarkers(contentWithMarkers);
        final String fileName = findClassName(content.getFirst()) + ".java";

        final IProject project = javaProject.getProject();
        final IPath path = new Path(fileName);
        final IFile file = project.getFile(fileName);
        if (file.exists()) {
            file.delete(true, null);
        }
        final ByteArrayInputStream is = new ByteArrayInputStream(content.getFirst().getBytes());
        file.create(is, true, null);
        final ICompilationUnit cu = (ICompilationUnit) javaProject.findElement(path);
        project.refreshLocal(IResource.DEPTH_INFINITE, null);
        project.build(IncrementalProjectBuilder.FULL_BUILD, null);
        return Tuple.newTuple(cu, content.getSecond());
    }

    public void clear() throws CoreException {

        final IProject project = javaProject.getProject();
        project.accept(new IResourceVisitor() {
            @Override
            public boolean visit(final IResource resource) throws CoreException {
                switch (resource.getType()) {
                case IResource.FILE:
                    if (resource.getName().endsWith(".class") || resource.getName().endsWith(".java")) {
                        resource.delete(true, null);
                    }
                }
                return true;
            }
        });
    }

    public String removeMarkers(final String content) {
        return content.replaceAll(MARKER_ESCAPE, "");
    }

}
