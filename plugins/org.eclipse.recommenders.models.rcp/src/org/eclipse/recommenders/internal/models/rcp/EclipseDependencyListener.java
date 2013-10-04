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

import static com.google.common.base.Optional.fromNullable;
import static org.eclipse.jdt.core.IJavaElement.JAVA_PROJECT;
import static org.eclipse.recommenders.internal.models.rcp.Dependencies.*;
import static org.eclipse.recommenders.rcp.utils.JdtUtils.getLocation;
import static org.eclipse.recommenders.utils.Checks.ensureIsNotNull;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
import org.eclipse.recommenders.models.DependencyInfo;
import org.eclipse.recommenders.models.DependencyType;
import org.eclipse.recommenders.models.IDependencyListener;
import org.eclipse.recommenders.rcp.JavaModelEvents.JarPackageFragmentRootAdded;
import org.eclipse.recommenders.rcp.JavaModelEvents.JarPackageFragmentRootRemoved;
import org.eclipse.recommenders.rcp.JavaModelEvents.JavaProjectClosed;
import org.eclipse.recommenders.rcp.JavaModelEvents.JavaProjectOpened;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

@SuppressWarnings("restriction")
public class EclipseDependencyListener implements IDependencyListener {

    private static final Logger LOG = LoggerFactory.getLogger(EclipseDependencyListener.class);

    private final HashMultimap<DependencyInfo, DependencyInfo> workspaceDependenciesByProject = HashMultimap.create();
    private final HashMultimap<DependencyInfo, IPackageFragmentRoot> jrePackageFragmentRoots = HashMultimap.create();

    public EclipseDependencyListener(final EventBus bus) {
        bus.register(this);
        parseWorkspaceForDependencies();
    }

    private void parseWorkspaceForDependencies() {
        IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        for (IProject project : projects) {
            try {
                if (project.isOpen() && project.hasNature(JavaCore.NATURE_ID)) {
                    IJavaProject javaProject = JavaCore.create(project);
                    registerDependenciesForJavaProject(javaProject);
                }
            } catch (CoreException e) {
                LOG.error("failed to register dependencies for project " + project, e);
            }
        }
    }

    @Subscribe
    public void onEvent(final JavaProjectOpened e) {
        registerDependenciesForJavaProject(e.project);
    }

    @Subscribe
    public void onEvent(final JavaProjectClosed e) {
        deregisterDependenciesForJavaProject(e.project);
    }

    @Subscribe
    public void onEvent(final JarPackageFragmentRootAdded e) {
        registerDependencyForJAR(e.root);
    }

    @Subscribe
    public void onEvent(final JarPackageFragmentRootRemoved e) {
        deregisterDependencyForJAR(e.root);
    }

    private void registerDependenciesForJavaProject(final IJavaProject javaProject) {
        DependencyInfo dependencyInfoForProject = createDependencyInfoForProject(javaProject);

        Optional<DependencyInfo> optionalJREDependencyInfo = createJREDependencyInfo(javaProject);
        if (optionalJREDependencyInfo.isPresent()) {
            workspaceDependenciesByProject.put(dependencyInfoForProject, optionalJREDependencyInfo.get());
            jrePackageFragmentRoots.putAll(dependencyInfoForProject, detectJREPackageFragementRoots(javaProject));
        }

        workspaceDependenciesByProject.put(dependencyInfoForProject, dependencyInfoForProject);
        workspaceDependenciesByProject.putAll(dependencyInfoForProject, searchForAllDependenciesOfProject(javaProject));
    }

    private Set<DependencyInfo> searchForAllDependenciesOfProject(final IJavaProject javaProject) {
        Set<DependencyInfo> dependencies = Sets.newHashSet();
        Set<IPackageFragmentRoot> jreRoots = jrePackageFragmentRoots.get(createDependencyInfoForProject(javaProject));
        try {
            for (final IPackageFragmentRoot packageFragmetRoot : javaProject.getAllPackageFragmentRoots()) {
                if (!jreRoots.contains(packageFragmetRoot) && packageFragmetRoot instanceof JarPackageFragmentRoot) {
                    DependencyInfo dependencyInfo = createDependencyInfoForJAR((JarPackageFragmentRoot) packageFragmetRoot);
                    dependencies.add(dependencyInfo);
                }
            }
        } catch (JavaModelException e1) {
            LOG.error("failed to search dependencies of project " + javaProject, e1);

        }
        return dependencies;
    }

    public static Set<IPackageFragmentRoot> detectJREPackageFragementRoots(final IJavaProject javaProject) {
        // Please notice that this is a heuristic to detect if a Jar is part of
        // the JRE or not.
        // All Jars in the JRE_Container which are not located in the ext folder
        // are defined as part of the JRE
        Set<IPackageFragmentRoot> jreRoots = new HashSet<IPackageFragmentRoot>();
        try {
            for (IClasspathEntry entry : javaProject.getRawClasspath()) {
                if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
                    if (entry.getPath().toString().contains("org.eclipse.jdt.launching.JRE_CONTAINER")) {
                        for (IPackageFragmentRoot packageFragmentRoot : javaProject.findPackageFragmentRoots(entry)) {
                            if (!packageFragmentRoot.getPath().toFile().getParentFile().getName().equals("ext")) {
                                jreRoots.add(packageFragmentRoot);
                            }
                        }
                    }
                }
            }
        } catch (JavaModelException e) {
            LOG.error("Failed to detect jre for project " + javaProject, e);
        }
        return jreRoots;
    }

    private void deregisterDependenciesForJavaProject(final IJavaProject javaProject) {
        DependencyInfo dependencyInfoForProject = createDependencyInfoForProject(javaProject);
        workspaceDependenciesByProject.removeAll(dependencyInfoForProject);
        jrePackageFragmentRoots.removeAll(dependencyInfoForProject);
    }

    private void registerDependencyForJAR(final JarPackageFragmentRoot root) {
        Optional<IJavaProject> optionalJavaProject = getJavaProjectForPackageFragmentRoot(root);
        if (!optionalJavaProject.isPresent()) {
            return;
        }

        IJavaProject javaProject = optionalJavaProject.get();
        DependencyInfo dependencyInfoForProject = createDependencyInfoForProject(javaProject);
        if (!isJREOfProjectIsKnown(dependencyInfoForProject)) {
            workspaceDependenciesByProject.removeAll(dependencyInfoForProject);
            registerDependenciesForJavaProject(javaProject);
        }
        if (!isPartOfTheJRE(root)) {
            DependencyInfo dependencyInfo = createDependencyInfoForJAR(root);
            workspaceDependenciesByProject.put(dependencyInfoForProject, dependencyInfo);
        }
    }

    private boolean isJREOfProjectIsKnown(final DependencyInfo dependencyInfoForProject) {
        for (DependencyInfo dependencyInfo : workspaceDependenciesByProject.get(dependencyInfoForProject)) {
            if (dependencyInfo.getType() == DependencyType.JRE) {
                return true;
            }
        }
        return false;
    }

    private boolean isPartOfTheJRE(final IPackageFragmentRoot pfr) {
        Optional<IJavaProject> optionalJavaProject = getJavaProjectForPackageFragmentRoot(pfr);
        if (optionalJavaProject.isPresent()) {
            if (jrePackageFragmentRoots.containsEntry(createDependencyInfoForProject(optionalJavaProject.get()), pfr)) {
                return true;
            }
        }
        return false;
    }

    private DependencyInfo createDependencyInfoForJAR(final JarPackageFragmentRoot pfr) {
        File file = ensureIsNotNull(getLocation(pfr).orNull(), "Could not determine absolute location of %s.", pfr);
        DependencyInfo dependencyInfo = new DependencyInfo(file, DependencyType.JAR);
        return dependencyInfo;
    }

    private void deregisterDependencyForJAR(final JarPackageFragmentRoot pfr) {
        Optional<IJavaProject> optionalJavaProject = getJavaProjectForPackageFragmentRoot(pfr);
        if (!optionalJavaProject.isPresent()) {
            return;
        }
        IJavaProject javaProject = optionalJavaProject.get();
        if (isPartOfTheJRE(pfr)) {
            deregisterJREDependenciesForProject(javaProject);
        } else {
            DependencyInfo dependencyInfo = createDependencyInfoForJAR(pfr);
            DependencyInfo projectDependencyInfo = createDependencyInfoForProject(javaProject);
            workspaceDependenciesByProject.remove(projectDependencyInfo, dependencyInfo);
            if (!workspaceDependenciesByProject.containsKey(projectDependencyInfo)) {
                jrePackageFragmentRoots.removeAll(projectDependencyInfo);
            }
        }
    }

    private void deregisterJREDependenciesForProject(final IJavaProject javaProject) {
        DependencyInfo projectDependencyInfo = createDependencyInfoForProject(javaProject);

        for (DependencyInfo dependencyInfo : workspaceDependenciesByProject.get(projectDependencyInfo)) {
            if (dependencyInfo.getType() == DependencyType.JRE) {
                workspaceDependenciesByProject.remove(projectDependencyInfo, dependencyInfo);
                return;
            }
        }
    }

    private Optional<IJavaProject> getJavaProjectForPackageFragmentRoot(final IPackageFragmentRoot pfr) {
        IJavaProject parent = (IJavaProject) pfr.getAncestor(JAVA_PROJECT);
        return fromNullable(parent);
    }

    @Override
    public ImmutableSet<DependencyInfo> getDependencies() {
        Builder<DependencyInfo> res = ImmutableSet.builder();
        for (DependencyInfo javaProjects : workspaceDependenciesByProject.keySet()) {
            Set<DependencyInfo> dependenciesForProject = workspaceDependenciesByProject.get(javaProjects);
            res.addAll(dependenciesForProject);
        }
        return res.build();
    }

    @Override
    public ImmutableSet<DependencyInfo> getDependenciesForProject(final DependencyInfo project) {
        Set<DependencyInfo> infos = workspaceDependenciesByProject.get(project);
        return ImmutableSet.copyOf(infos);
    }

    @Override
    public ImmutableSet<DependencyInfo> getProjects() {
        Set<DependencyInfo> infos = workspaceDependenciesByProject.keySet();
        return ImmutableSet.copyOf(infos);
    }

}
