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

import static com.google.common.base.Optional.absent;
import static org.eclipse.recommenders.internal.models.rcp.ModelsRcpModule.IDENTIFIED_PROJECT_COORDINATES;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.recommenders.models.DependencyInfo;
import org.eclipse.recommenders.models.IProjectCoordinateAdvisor;
import org.eclipse.recommenders.models.IProjectCoordinateAdvisorService;
import org.eclipse.recommenders.models.ProjectCoordinate;
import org.eclipse.recommenders.models.advisors.ProjectCoordinateAdvisorService;
import org.eclipse.recommenders.models.rcp.ModelEvents.AdvisorConfigurationChangedEvent;
import org.eclipse.recommenders.models.rcp.ModelEvents.ModelIndexOpenedEvent;
import org.eclipse.recommenders.models.rcp.ModelEvents.ProjectCoordinateChangeEvent;
import org.eclipse.recommenders.rcp.IRcpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.io.Files;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class EclipseProjectCoordinateAdvisorService implements IProjectCoordinateAdvisorService, IRcpService {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    private final ProjectCoordinateAdvisorService delgate;
    private final ModelsRcpPreferences prefs;
    private final LoadingCache<DependencyInfo, Optional<ProjectCoordinate>> projectCoordianteCache;

    private final File persistenceFile;
    private final Gson cacheGson;

    @SuppressWarnings("serial")
    private final Type cacheType = new TypeToken<Map<DependencyInfo, Optional<ProjectCoordinate>>>() {
    }.getType();

    private Map<IProjectCoordinateAdvisor, AdvisorDescriptor> descriptors = Maps.newHashMap();

    @Inject
    public EclipseProjectCoordinateAdvisorService(@Named(IDENTIFIED_PROJECT_COORDINATES) File persistenceFile,
            EventBus bus, ModelsRcpPreferences prefs) {
        bus.register(this);
        this.prefs = prefs;
        this.delgate = new ProjectCoordinateAdvisorService();
        this.persistenceFile = persistenceFile;
        this.cacheGson = new GsonBuilder()
                .registerTypeAdapter(ProjectCoordinate.class, new ProjectCoordinateJsonTypeAdapter())
                .registerTypeAdapter(DependencyInfo.class, new DependencyInfoJsonTypeAdapter())
                .registerTypeAdapter(Optional.class, new OptionalJsonTypeAdapter<ProjectCoordinate>())
                .enableComplexMapKeySerialization().serializeNulls().create();
        projectCoordianteCache = createCache();
        configureAdvisorList(prefs.advisorConfiguration);
    }

    private LoadingCache<DependencyInfo, Optional<ProjectCoordinate>> createCache() {
        return CacheBuilder.newBuilder().maximumSize(200)
                .build(new CacheLoader<DependencyInfo, Optional<ProjectCoordinate>>() {

                    @Override
                    public Optional<ProjectCoordinate> load(DependencyInfo info) {
                        return delgate.suggest(info);
                    }
                });
    }

    private void configureAdvisorList(String advisorConfiguration) {
        setAdvisors(provideAdvisors(advisorConfiguration));
    }

    private List<IProjectCoordinateAdvisor> provideAdvisors(String advisorConfiguration) {
        Map<IProjectCoordinateAdvisor, AdvisorDescriptor> newDescriptors = Maps.newHashMap();
        List<AdvisorDescriptor> registeredAdvisors = AdvisorDescriptors.getRegisteredAdvisors();
        List<AdvisorDescriptor> loadedDescriptors = AdvisorDescriptors.load(advisorConfiguration, registeredAdvisors);
        List<IProjectCoordinateAdvisor> advisors = Lists.newArrayListWithCapacity(loadedDescriptors.size());
        for (AdvisorDescriptor descriptor : loadedDescriptors) {
            try {
                if (descriptor.isEnabled()) {
                    IProjectCoordinateAdvisor advisor = descriptor.createAdvisor();
                    advisors.add(advisor);
                    newDescriptors.put(advisor, descriptor);
                }
            } catch (CoreException e) {
                LOG.error("Exception during creation of Advisor with id: " + descriptor.getId(), e);
            }
        }
        descriptors = newDescriptors;
        return advisors;
    }

    public AdvisorDescriptor getDescriptor(IProjectCoordinateAdvisor advisor) {
        return descriptors.get(advisor);
    }

    @Override
    public Optional<ProjectCoordinate> suggest(DependencyInfo dependencyInfo) {
        try {
            return projectCoordianteCache.get(dependencyInfo);
        } catch (ExecutionException e) {
            LOG.error("Exception occured while accessing project coordinates cache", e);
            return absent();
        }
    }

    @Override
    public ImmutableList<IProjectCoordinateAdvisor> getAdvisors() {
        return delgate.getAdvisors();
    }

    @Override
    public void addAdvisor(IProjectCoordinateAdvisor advisor) {
        delgate.addAdvisor(advisor);
    }

    @Override
    public void setAdvisors(List<IProjectCoordinateAdvisor> advisors) {
        delgate.setAdvisors(advisors);
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

    @PreDestroy
    public void close() throws IOException {
        String json = cacheGson.toJson(projectCoordianteCache.asMap(), cacheType);
        Files.write(json, persistenceFile, Charsets.UTF_8);
    }

    @Subscribe
    public void onEvent(ProjectCoordinateChangeEvent e) {
        projectCoordianteCache.invalidate(e.dependencyInfo);
    }

    @Subscribe
    public void onEvent(AdvisorConfigurationChangedEvent e) throws IOException {
        projectCoordianteCache.invalidateAll();
        configureAdvisorList(prefs.advisorConfiguration);
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
            try {
                monitor.beginTask("Refreshing", dependencyInfos.size());
                for (DependencyInfo di : dependencyInfos) {
                    monitor.subTask(di.toString());
                    projectCoordianteCache.refresh(di);
                    monitor.worked(1);
                }
            } finally {
                monitor.done();
            }
            return Status.OK_STATUS;
        }
    }

    public void clearCache() {
        projectCoordianteCache.invalidateAll();
    }

}
