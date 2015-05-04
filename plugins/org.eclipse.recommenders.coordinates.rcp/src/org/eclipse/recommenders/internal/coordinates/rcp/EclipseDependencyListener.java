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
package org.eclipse.recommenders.internal.coordinates.rcp;

import static com.google.common.base.Optional.fromNullable;
import static org.eclipse.recommenders.coordinates.rcp.DependencyInfos.*;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
import org.eclipse.recommenders.coordinates.DependencyInfo;
import org.eclipse.recommenders.coordinates.DependencyType;
import org.eclipse.recommenders.coordinates.IDependencyListener;
import org.eclipse.recommenders.coordinates.rcp.DependencyInfos;
import org.eclipse.recommenders.rcp.JavaModelEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

@SuppressWarnings("restriction")
public class EclipseDependencyListener implements IDependencyListener {

    private static final Logger LOG = LoggerFactory.getLogger(EclipseDependencyListener.class);

    private final HashMultimap<DependencyInfo, DependencyInfo> workspaceDependenciesByProject = HashMultimap.create();
    private final HashMultimap<DependencyInfo, IPackageFragmentRoot> jrePackageFragmentRoots = HashMultimap.create();

    private final Map<IJavaProject, DependencyInfo> projectDependencyInfos = Maps.newHashMap();

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
                LOG.error("Failed to register dependencies for project {}", project, e); //$NON-NLS-1$
            }
        }
    }

    @Subscribe
    public void onEvent(final JavaModelEvents.JavaProjectOpened e) {
        registerDependenciesForJavaProject(e.project);
    }

    @Subscribe
    public void onEvent(final JavaModelEvents.JavaProjectClosed e) {
        deregisterDependenciesForJavaProject(e.project);
    }

    @Subscribe
    public void onEvent(final JavaModelEvents.JarPackageFragmentRootAdded e) {
        registerDependencyForJAR(e.root);
    }

    @Subscribe
    public void onEvent(final JavaModelEvents.JarPackageFragmentRootRemoved e) {
        deregisterDependencyForJAR(e.root);
    }

    private void registerDependenciesForJavaProject(final IJavaProject javaProject) {
        DependencyInfo dependencyInfoForProject = getDependencyInfoForProject(javaProject);

        Optional<DependencyInfo> optionalJREDependencyInfo = DependencyInfos.createDependencyInfoForJre(javaProject);
        if (optionalJREDependencyInfo.isPresent()) {
            workspaceDependenciesByProject.put(dependencyInfoForProject, optionalJREDependencyInfo.get());
            jrePackageFragmentRoots.putAll(dependencyInfoForProject, detectJREPackageFragementRoots(javaProject));
        }

        workspaceDependenciesByProject.putAll(dependencyInfoForProject, searchForAllDependenciesOfProject(javaProject));
    }

    private Set<DependencyInfo> searchForAllDependenciesOfProject(final IJavaProject javaProject) {
        Set<DependencyInfo> dependencies = Sets.newHashSet();
        Set<IPackageFragmentRoot> jreRoots = jrePackageFragmentRoots.get(getDependencyInfoForProject(javaProject));
        try {
            for (final IPackageFragmentRoot packageFragmentRoot : javaProject.getAllPackageFragmentRoots()) {
                if (!jreRoots.contains(packageFragmentRoot) && packageFragmentRoot instanceof JarPackageFragmentRoot) {
                    DependencyInfo dependencyInfo = createDependencyInfoForJar(packageFragmentRoot);
                    dependencies.add(dependencyInfo);
                } else if (packageFragmentRoot.getKind() == IPackageFragmentRoot.K_SOURCE
                        && packageFragmentRoot.getJavaProject() != null) {
                    DependencyInfo dependencyInfo = DependencyInfos.createDependencyInfoForProject(packageFragmentRoot
                            .getJavaProject());
                    dependencies.add(dependencyInfo);
                }
            }
        } catch (JavaModelException e) {
            LOG.error("Failed to search dependencies of project {}", javaProject, e); //$NON-NLS-1$
        }
        return dependencies;
    }

    public static Set<IPackageFragmentRoot> detectJREPackageFragementRoots(final IJavaProject javaProject) {
        // Please note that this is merely a heuristic to detect if a Jar is part of the JRE or not:
        // All Jars in the JRE_Container which are not located in the ext folder are considered part of the JRE.
        Set<IPackageFragmentRoot> jreRoots = new HashSet<IPackageFragmentRoot>();
        try {
            for (IClasspathEntry entry : javaProject.getRawClasspath()) {
                if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
                    if (entry.getPath().toString().contains("org.eclipse.jdt.launching.JRE_CONTAINER")) { //$NON-NLS-1$
                        for (IPackageFragmentRoot packageFragmentRoot : javaProject.findPackageFragmentRoots(entry)) {
                            if (!packageFragmentRoot.getPath().toFile().getParentFile().getName().equals("ext")) { //$NON-NLS-1$
                                jreRoots.add(packageFragmentRoot);
                            }
                        }
                    }
                }
            }
        } catch (JavaModelException e) {
            LOG.error("Failed to detect JRE for project " + javaProject, e); //$NON-NLS-1$
        }
        return jreRoots;
    }

    private void deregisterDependenciesForJavaProject(final IJavaProject javaProject) {
        DependencyInfo dependencyInfoForProject = getDependencyInfoForProject(javaProject);
        workspaceDependenciesByProject.removeAll(dependencyInfoForProject);
        jrePackageFragmentRoots.removeAll(dependencyInfoForProject);
        synchronized (projectDependencyInfos) {
            projectDependencyInfos.remove(javaProject);
        }
    }

    private void registerDependencyForJAR(final JarPackageFragmentRoot root) {
        Optional<IJavaProject> optionalJavaProject = getJavaProjectForPackageFragmentRoot(root);
        if (!optionalJavaProject.isPresent()) {
            return;
        }

        IJavaProject javaProject = optionalJavaProject.get();
        DependencyInfo dependencyInfoForProject = getDependencyInfoForProject(javaProject);
        if (!isJREOfProjectIsKnown(dependencyInfoForProject)) {
            workspaceDependenciesByProject.removeAll(dependencyInfoForProject);
            registerDependenciesForJavaProject(javaProject);
        }
        if (!isPartOfTheJRE(root)) {
            DependencyInfo dependencyInfo = createDependencyInfoForJar(root);
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

    private void deregisterDependencyForJAR(final JarPackageFragmentRoot pfr) {
        Optional<IJavaProject> optionalJavaProject = getJavaProjectForPackageFragmentRoot(pfr);
        if (!optionalJavaProject.isPresent()) {
            return;
        }
        IJavaProject javaProject = optionalJavaProject.get();
        if (isPartOfTheJRE(pfr)) {
            deregisterJREDependenciesForProject(javaProject);
        } else {
            DependencyInfo dependencyInfo = createDependencyInfoForJar(pfr);
            DependencyInfo projectDependencyInfo = getDependencyInfoForProject(javaProject);
            workspaceDependenciesByProject.remove(projectDependencyInfo, dependencyInfo);
            if (!workspaceDependenciesByProject.containsKey(projectDependencyInfo)) {
                jrePackageFragmentRoots.removeAll(projectDependencyInfo);
            }
        }
    }

    private void deregisterJREDependenciesForProject(final IJavaProject javaProject) {
        DependencyInfo projectDependencyInfo = getDependencyInfoForProject(javaProject);

        for (DependencyInfo dependencyInfo : workspaceDependenciesByProject.get(projectDependencyInfo)) {
            if (dependencyInfo.getType() == DependencyType.JRE) {
                workspaceDependenciesByProject.remove(projectDependencyInfo, dependencyInfo);
                return;
            }
        }
    }

    private Optional<IJavaProject> getJavaProjectForPackageFragmentRoot(final IPackageFragmentRoot pfr) {
        IJavaProject parent = (IJavaProject) pfr.getAncestor(IJavaElement.JAVA_PROJECT);
        return fromNullable(parent);
    }

    @Override
    public ImmutableSet<DependencyInfo> getDependencies() {
        ImmutableSet.Builder<DependencyInfo> res = ImmutableSet.builder();
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

    private DependencyInfo getDependencyInfoForProject(final IJavaProject javaProject) {
        synchronized (projectDependencyInfos) {
            DependencyInfo dependencyInfo = projectDependencyInfos.get(javaProject);
            if (dependencyInfo == null) {
                dependencyInfo = createDependencyInfoForProject(javaProject);
                projectDependencyInfos.put(javaProject, dependencyInfo);
            }
            return dependencyInfo;
        }
    }

    @Override
    public ImmutableSet<DependencyInfo> getProjects() {
        Set<DependencyInfo> infos = workspaceDependenciesByProject.keySet();
        return ImmutableSet.copyOf(infos);
    }
}
