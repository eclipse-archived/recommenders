/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 *    Olav Lenz - Added caching functionality.
 */
package org.eclipse.recommenders.internal.models.rcp;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;
import static org.eclipse.jdt.core.IJavaElement.PACKAGE_FRAGMENT_ROOT;
import static org.eclipse.recommenders.coordinates.DependencyInfo.PROJECT_NAME;
import static org.eclipse.recommenders.coordinates.DependencyType.*;
import static org.eclipse.recommenders.internal.models.rcp.Dependencies.createDependencyInfoForJre;
import static org.eclipse.recommenders.rcp.utils.JdtUtils.getLocation;
import static org.eclipse.recommenders.utils.Checks.cast;
import static org.eclipse.recommenders.utils.Constants.REASON_NOT_IN_CACHE;
import static org.eclipse.recommenders.utils.Result.*;

import java.io.File;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.recommenders.coordinates.DependencyInfo;
import org.eclipse.recommenders.coordinates.IProjectCoordinateAdvisorService;
import org.eclipse.recommenders.coordinates.ProjectCoordinate;
import org.eclipse.recommenders.models.UniqueMethodName;
import org.eclipse.recommenders.models.UniqueTypeName;
import org.eclipse.recommenders.models.rcp.IProjectCoordinateProvider;
import org.eclipse.recommenders.rcp.IRcpService;
import org.eclipse.recommenders.rcp.JavaElementResolver;
import org.eclipse.recommenders.rcp.utils.JdtUtils;
import org.eclipse.recommenders.utils.Result;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;

public class ProjectCoordinateProvider implements IProjectCoordinateProvider, IRcpService {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectCoordinateProvider.class);

    private final JavaElementResolver javaElementResolver;
    private final IProjectCoordinateAdvisorService pcAdvisorService;

    private final LoadingCache<IPackageFragmentRoot, Optional<DependencyInfo>> dependencyInfoCache;

    @Inject
    public ProjectCoordinateProvider(IProjectCoordinateAdvisorService pcAdvisorService,
            JavaElementResolver javaElementResolver) {
        this.pcAdvisorService = pcAdvisorService;
        this.javaElementResolver = javaElementResolver;
        dependencyInfoCache = createCache();
    }

    private LoadingCache<IPackageFragmentRoot, Optional<DependencyInfo>> createCache() {
        return CacheBuilder.newBuilder().maximumSize(200)
                .build(new CacheLoader<IPackageFragmentRoot, Optional<DependencyInfo>>() {

                    @Override
                    public Optional<DependencyInfo> load(IPackageFragmentRoot pfr) {
                        return extractDependencyInfo(pfr);
                    }
                });

    }

    @Override
    public Optional<ProjectCoordinate> resolve(ITypeBinding binding) {
        if (binding == null) {
            return absent();
        }
        IType type = cast(binding.getJavaElement());
        return resolve(type);
    }

    @Override
    public Optional<ProjectCoordinate> resolve(IType type) {
        if (type == null) {
            return absent();
        }
        IPackageFragmentRoot root = cast(type.getAncestor(PACKAGE_FRAGMENT_ROOT));
        return resolve(root);
    }

    @Override
    public Optional<ProjectCoordinate> resolve(IMethodBinding binding) {
        if (binding == null) {
            return absent();
        }
        IMethod method = cast(binding.getJavaElement());
        return resolve(method);
    }

    @Override
    public Optional<ProjectCoordinate> resolve(IMethod method) {
        if (method == null) {
            return absent();
        }
        IPackageFragmentRoot root = cast(method.getAncestor(PACKAGE_FRAGMENT_ROOT));
        return resolve(root);
    }

    @Override
    public Optional<ProjectCoordinate> resolve(IPackageFragmentRoot root) {
        try {
            Optional<DependencyInfo> dependencyInfo = dependencyInfoCache.get(root);
            if (dependencyInfo.isPresent()) {
                return resolve(dependencyInfo.get());
            }
            return absent();
        } catch (ExecutionException e) {
            return absent();
        }
    }

    private Optional<DependencyInfo> extractDependencyInfo(IPackageFragmentRoot root) {
        if (root == null) {
            return absent();
        }
        if (!root.isArchive()) {
            return extractDependencyInfo(root.getJavaProject());
        }
        File location = JdtUtils.getLocation(root).orNull();
        if (location == null) {
            return absent();
        }

        IJavaProject javaProject = root.getJavaProject();

        if (isPartOfJRE(root, javaProject)) {
            return createDependencyInfoForJre(javaProject);
        } else {
            DependencyInfo request = new DependencyInfo(location, JAR);
            return of(request);
        }
    }

    private static boolean isPartOfJRE(IPackageFragmentRoot root, IJavaProject javaProject) {
        try {
            for (IClasspathEntry entry : javaProject.getRawClasspath()) {
                if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
                    if (entry.getPath().toString().contains("org.eclipse.jdt.launching.JRE_CONTAINER")) { //$NON-NLS-1$
                        for (IPackageFragmentRoot packageFragmentRoot : javaProject.findPackageFragmentRoots(entry)) {
                            if (!packageFragmentRoot.getPath().toFile().getParentFile().getName().equals("ext")) { //$NON-NLS-1$
                                if (packageFragmentRoot.equals(root)) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        } catch (JavaModelException e) {
            LOG.error("Failed to traverse project dependencies of {}.", e); //$NON-NLS-1$
        }
        return false;
    }

    @Override
    public Optional<ProjectCoordinate> resolve(IJavaProject javaProject) {
        return resolve(extractDependencyInfo(javaProject).get());
    }

    private Optional<DependencyInfo> extractDependencyInfo(IJavaProject javaProject) {
        File location = getLocation(javaProject).orNull();
        DependencyInfo request = new DependencyInfo(location, PROJECT,
                ImmutableMap.of(PROJECT_NAME, javaProject.getElementName()));
        return of(request);
    }

    @Override
    public Optional<ProjectCoordinate> resolve(DependencyInfo info) {
        return pcAdvisorService.suggest(info);
    }

    @Override
    public Result<ProjectCoordinate> tryResolve(DependencyInfo info) {
        return pcAdvisorService.trySuggest(info);
    }

    @Override
    public Optional<UniqueTypeName> toUniqueName(IType type) {
        ProjectCoordinate base = resolve(type).orNull();
        if (null == base) {
            return absent();
        }
        return of(new UniqueTypeName(base, toName(type)));
    }

    @Override
    public Optional<UniqueMethodName> toUniqueName(IMethod method) {
        ProjectCoordinate base = resolve(method).orNull();
        if (null == base) {
            return absent();
        }
        IMethodName name = toName(method).orNull();
        if (null == name) {
            return absent();
        }
        return of(new UniqueMethodName(base, name));
    }

    @Override
    public ITypeName toName(IType type) {
        return javaElementResolver.toRecType(type);
    }

    @Override
    public Optional<IMethodName> toName(IMethod method) {
        return javaElementResolver.toRecMethod(method);
    }

    @Override
    public Result<UniqueTypeName> tryToUniqueName(IType type) {
        Result<ProjectCoordinate> pc = tryToProjectCoordinate(type);
        switch (pc.getReason()) {
        case REASON_NOT_IN_CACHE:
            return Result.absent(REASON_NOT_IN_CACHE);
        case OK:
            return Result.of(new UniqueTypeName(pc.get(), toName(type)));
        case ABSENT:
        default:
            return Result.absent();
        }
    }

    @Override
    public Result<UniqueMethodName> tryToUniqueName(IMethod method) {
        Result<ProjectCoordinate> pc = tryToProjectCoordinate(method);
        switch (pc.getReason()) {
        case REASON_NOT_IN_CACHE:
            return Result.absent(REASON_NOT_IN_CACHE);
        case OK:
            Optional<IMethodName> name = toName(method);
            if (name.isPresent()) {
                return Result.of(new UniqueMethodName(pc.get(), name.get()));
            }
        case ABSENT:
        default:
            return Result.absent();
        }
    }

    private Result<ProjectCoordinate> tryToProjectCoordinate(IJavaElement element) {
        IPackageFragmentRoot root = cast(element.getAncestor(PACKAGE_FRAGMENT_ROOT));
        if (root == null) {
            return Result.absent();
        }
        try {
            DependencyInfo info = dependencyInfoCache.get(root).orNull();
            if (info == null) {
                return Result.absent();
            }
            return pcAdvisorService.trySuggest(info); // Pass-through REASON_NOT_IN_CACHE results
        } catch (Exception e) {
            return Result.absent(e);
        }
    }
}
