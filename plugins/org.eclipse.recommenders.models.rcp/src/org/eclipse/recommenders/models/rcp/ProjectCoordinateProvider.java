/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.models.rcp;

import static com.google.common.base.Optional.absent;
import static org.eclipse.jdt.core.IJavaElement.PACKAGE_FRAGMENT_ROOT;
import static org.eclipse.recommenders.models.dependencies.DependencyType.JAR;
import static org.eclipse.recommenders.utils.Checks.cast;
import static org.eclipse.recommenders.utils.rcp.JdtUtils.getLocation;

import java.io.File;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.recommenders.models.ProjectCoordinate;
import org.eclipse.recommenders.models.dependencies.DependencyInfo;
import org.eclipse.recommenders.models.dependencies.DependencyType;
import org.eclipse.recommenders.models.dependencies.IMappingProvider;
import org.eclipse.recommenders.models.dependencies.rcp.EclipseDependencyListener;
import org.eclipse.recommenders.utils.rcp.JdtUtils;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class ProjectCoordinateProvider {

    @Inject
    private IMappingProvider mappingProvider;

    private LoadingCache<IPackageFragmentRoot, Optional<ProjectCoordinate>> cache = null;

    public ProjectCoordinateProvider() {
        initializeCache();
    }

    private void initializeCache() {
        /*
         * At the moment the cache is only used for IPackageFragmentRoots --> ProjectCoordinates (PC). This could be
         * extended to JavaElements --> PC to support cache also information about IJavaProject.
         */
        cache = CacheBuilder.newBuilder().maximumSize(200).recordStats()
                .build(new CacheLoader<IPackageFragmentRoot, Optional<ProjectCoordinate>>() {

                    @Override
                    public Optional<ProjectCoordinate> load(IPackageFragmentRoot arg0) {
                        return extractProjectCoordinate(arg0);
                    }

                });
    }

    public ProjectCoordinateProvider(IMappingProvider mappingProvider) {
        this();
        this.mappingProvider = mappingProvider;
    }

    public Optional<ProjectCoordinate> resolve(ITypeBinding binding) {
        if (binding == null) {
            return absent();
        }
        IType type = cast(binding.getJavaElement());
        return resolve(type);
    }

    public Optional<ProjectCoordinate> resolve(IType type) {
        if (type == null) {
            return absent();
        }
        IPackageFragmentRoot root = cast(type.getAncestor(PACKAGE_FRAGMENT_ROOT));
        return resolve(root);
    }

    public Optional<ProjectCoordinate> resolve(IMethodBinding binding) {
        if (binding == null) {
            return absent();
        }
        IMethod method = cast(binding.getJavaElement());
        return resolve(method);
    }

    public Optional<ProjectCoordinate> resolve(IMethod method) {
        if (method == null) {
            return absent();
        }
        IPackageFragmentRoot root = cast(method.getAncestor(PACKAGE_FRAGMENT_ROOT));
        return resolve(root);
    }

    public Optional<ProjectCoordinate> resolve(IPackageFragmentRoot root) {
        try {
            return cache.get(root);
        } catch (ExecutionException e) {
            return absent();
        }
    }

    private Optional<ProjectCoordinate> extractProjectCoordinate(IPackageFragmentRoot root) {
        if (root == null) {
            return absent();
        }
        if (!root.isArchive()) {
            return resolve(root.getJavaProject());
        }
        File location = JdtUtils.getLocation(root).orNull();
        if (location == null) {
            return absent();
        }

        IJavaProject javaProject = root.getJavaProject();
        try {
            for (IClasspathEntry entry : javaProject.getRawClasspath()) {
                if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
                    if (entry.getPath().toString().contains("org.eclipse.jdt.launching.JRE_CONTAINER")) {
                        for (IPackageFragmentRoot packageFragmentRoot : javaProject.findPackageFragmentRoots(entry)) {
                            if (!packageFragmentRoot.getPath().toFile().getParentFile().getName().equals("ext")) {
                                if (packageFragmentRoot.equals(root)) {
                                    Optional<DependencyInfo> request = EclipseDependencyListener
                                            .createJREDependencyInfo(javaProject);
                                    if (request.isPresent()) {
                                        return resolve(request.get());
                                    } else {
                                        return absent();
                                    }
                                }
                            }
                        }
                    }
                }
            }
            DependencyInfo request = new DependencyInfo(location, JAR);
            return resolve(request);
        } catch (JavaModelException e) {
            e.printStackTrace();
        }
        return absent();
    }

    public Optional<ProjectCoordinate> resolve(IJavaProject javaProject) {
        File location = getLocation(javaProject).orNull();
        DependencyInfo request = new DependencyInfo(location, DependencyType.PROJECT);
        return resolve(request);
    }

    public Optional<ProjectCoordinate> resolve(DependencyInfo info) {
        return mappingProvider.searchForProjectCoordinate(info);
    }

}
