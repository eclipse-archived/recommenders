/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 *    Olav Lenz - Added caching caching and storage functionality.
 */
package org.eclipse.recommenders.internal.models.rcp;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;
import static org.eclipse.jdt.core.IJavaElement.PACKAGE_FRAGMENT_ROOT;
import static org.eclipse.recommenders.internal.models.rcp.Dependencies.*;
import static org.eclipse.recommenders.internal.models.rcp.ModelsRcpModule.IDENTIFIED_PACKAGE_FRAGMENT_ROOTS;
import static org.eclipse.recommenders.models.DependencyType.JAR;
import static org.eclipse.recommenders.rcp.utils.JdtUtils.getLocation;
import static org.eclipse.recommenders.utils.Checks.cast;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.recommenders.models.DependencyInfo;
import org.eclipse.recommenders.models.DependencyType;
import org.eclipse.recommenders.models.ProjectCoordinate;
import org.eclipse.recommenders.models.UniqueMethodName;
import org.eclipse.recommenders.models.UniqueTypeName;
import org.eclipse.recommenders.models.advisors.ProjectCoordinateAdvisorService;
import org.eclipse.recommenders.models.rcp.IProjectCoordinateProvider;
import org.eclipse.recommenders.models.rcp.ModelEvents.AdvisorConfigurationChangedEvent;
import org.eclipse.recommenders.models.rcp.ModelEvents.ModelIndexOpenedEvent;
import org.eclipse.recommenders.models.rcp.ModelEvents.ProjectCoordinateChangeEvent;
import org.eclipse.recommenders.rcp.IRcpService;
import org.eclipse.recommenders.rcp.JavaElementResolver;
import org.eclipse.recommenders.rcp.utils.JdtUtils;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.eventbus.Subscribe;
import com.google.common.io.Files;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ProjectCoordinateProvider implements IProjectCoordinateProvider, IRcpService {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectCoordinateProvider.class);

    private final JavaElementResolver javaElementResolver;
    private final ProjectCoordinateAdvisorService pcService;
    private final File persistenceFile;
    private final Gson cacheGson;

    @SuppressWarnings("serial")
    private final Type cacheType = new TypeToken<Map<DependencyInfo, Optional<ProjectCoordinate>>>() {
    }.getType();

    private LoadingCache<IPackageFragmentRoot, Optional<DependencyInfo>> dependencyInfoCache;

    private LoadingCache<DependencyInfo, Optional<ProjectCoordinate>> projectCoordianteCache;

    private void initializeCaches() {

        dependencyInfoCache = CacheBuilder.newBuilder().maximumSize(200)
                .build(new CacheLoader<IPackageFragmentRoot, Optional<DependencyInfo>>() {

                    @Override
                    public Optional<DependencyInfo> load(IPackageFragmentRoot pfr) {
                        return extractDependencyInfo(pfr);
                    }
                });

        projectCoordianteCache = CacheBuilder.newBuilder().maximumSize(200)
                .build(new CacheLoader<DependencyInfo, Optional<ProjectCoordinate>>() {

                    @Override
                    public Optional<ProjectCoordinate> load(DependencyInfo info) {
                        return pcService.suggest(info);
                    }
                });
    }

    @Inject
    public ProjectCoordinateProvider(@Named(IDENTIFIED_PACKAGE_FRAGMENT_ROOTS) File persistenceFile,
            ProjectCoordinateAdvisorService mappingProvider, JavaElementResolver javaElementResolver) {
        this.persistenceFile = persistenceFile;
        pcService = mappingProvider;
        this.javaElementResolver = javaElementResolver;
        cacheGson = new GsonBuilder()
                .registerTypeAdapter(ProjectCoordinate.class, new ProjectCoordinateJsonTypeAdapter())
                .registerTypeAdapter(DependencyInfo.class, new DependencyInfoJsonTypeAdapter())
                .registerTypeAdapter(Optional.class, new OptionalJsonTypeAdapter<ProjectCoordinate>())
                .enableComplexMapKeySerialization().serializeNulls().create();
        initializeCaches();
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
                return projectCoordianteCache.get(dependencyInfo.get());
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
            return createJREDependencyInfo(javaProject);
        } else {
            DependencyInfo request = new DependencyInfo(location, JAR, createSurroundingProjectHint(javaProject));
            return of(request);
        }
    }

    private static boolean isPartOfJRE(IPackageFragmentRoot root, IJavaProject javaProject) {
        try {
            for (IClasspathEntry entry : javaProject.getRawClasspath()) {
                if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
                    if (entry.getPath().toString().contains("org.eclipse.jdt.launching.JRE_CONTAINER")) {
                        for (IPackageFragmentRoot packageFragmentRoot : javaProject.findPackageFragmentRoots(entry)) {
                            if (!packageFragmentRoot.getPath().toFile().getParentFile().getName().equals("ext")) {
                                if (packageFragmentRoot.equals(root)) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        } catch (JavaModelException e) {
            LOG.error("Failed to traverse project dependencies of " + javaProject, e);

        }
        return false;
    }

    @Override
    public Optional<ProjectCoordinate> resolve(IJavaProject javaProject) {
        return resolve(extractDependencyInfo(javaProject).get());
    }

    private Optional<DependencyInfo> extractDependencyInfo(IJavaProject javaProject) {
        File location = getLocation(javaProject).orNull();
        DependencyInfo request = new DependencyInfo(location, DependencyType.PROJECT);
        return of(request);
    }

    @Override
    public Optional<ProjectCoordinate> resolve(DependencyInfo info) {
        try {
            return projectCoordianteCache.get(info);
        } catch (ExecutionException e) {
            return absent();
        }
    }

    @PreDestroy
    public void close() throws IOException {
        String json = cacheGson.toJson(projectCoordianteCache.asMap(), cacheType);
        Files.write(json, persistenceFile, Charsets.UTF_8);
    }

    @PostConstruct
    public void open() throws IOException {
        if (!persistenceFile.exists()) {
            return;
        }
        String json = Files.toString(persistenceFile, Charsets.UTF_8);
        Map<DependencyInfo, Optional<ProjectCoordinate>> deserializedCache = cacheGson.fromJson(json, cacheType);

        for (Entry<DependencyInfo, Optional<ProjectCoordinate>> entry : deserializedCache.entrySet()) {
            projectCoordianteCache.put(entry.getKey(), entry.getValue());
        }
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

    @Subscribe
    public void onEvent(ProjectCoordinateChangeEvent e) {
        projectCoordianteCache.invalidate(e.dependencyInfo);
    }

    @Subscribe
    public void onEvent(AdvisorConfigurationChangedEvent e) throws IOException {
        projectCoordianteCache.invalidateAll();
    }

    @Subscribe
    public void onEvent(ModelIndexOpenedEvent e) {
        // the fingerprint strategy uses the model index to determine missing project coordinates. Thus we have to
        // invalidate at least all absent values but to be honest, all values need to be refreshed!
        new RefreshProjectCoordinatesJob("Refreshing cached project coordinates").schedule();
    }

    private final class RefreshProjectCoordinatesJob extends Job {

        private RefreshProjectCoordinatesJob(String name) {
            super(name);
        }

        @Override
        protected IStatus run(IProgressMonitor monitor) {
            Set<DependencyInfo> dependencyInfos = projectCoordianteCache.asMap().keySet();
            monitor.beginTask("Refreshing", dependencyInfos.size());
            for (DependencyInfo di : dependencyInfos) {
                monitor.subTask(di.toString());
                projectCoordianteCache.refresh(di);
                monitor.worked(1);
            }
            monitor.done();
            return Status.OK_STATUS;
        }
    }

}
