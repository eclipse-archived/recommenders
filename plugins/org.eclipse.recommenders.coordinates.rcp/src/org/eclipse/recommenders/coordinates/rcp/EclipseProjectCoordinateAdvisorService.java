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
package org.eclipse.recommenders.coordinates.rcp;

import static com.google.common.base.Optional.absent;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.eclipse.recommenders.internal.coordinates.rcp.CoordinatesRcpModule.IDENTIFIED_PROJECT_COORDINATES;
import static org.eclipse.recommenders.internal.coordinates.rcp.l10n.LogMessages.ERROR_IN_ADVISOR_SERVICE_SUGGEST;
import static org.eclipse.recommenders.utils.Constants.REASON_NOT_IN_CACHE;
import static org.eclipse.recommenders.utils.Logs.log;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.recommenders.coordinates.DependencyInfo;
import org.eclipse.recommenders.coordinates.IProjectCoordinateAdvisor;
import org.eclipse.recommenders.coordinates.IProjectCoordinateAdvisorService;
import org.eclipse.recommenders.coordinates.ProjectCoordinate;
import org.eclipse.recommenders.coordinates.ProjectCoordinateAdvisorService;
import org.eclipse.recommenders.coordinates.rcp.CoordinateEvents.AdvisorConfigurationChangedEvent;
import org.eclipse.recommenders.coordinates.rcp.CoordinateEvents.ProjectCoordinateChangeEvent;
import org.eclipse.recommenders.internal.coordinates.rcp.AdvisorDescriptor;
import org.eclipse.recommenders.internal.coordinates.rcp.AdvisorDescriptors;
import org.eclipse.recommenders.internal.coordinates.rcp.CoordinatesRcpPreferences;
import org.eclipse.recommenders.internal.coordinates.rcp.DependencyInfoJsonTypeAdapter;
import org.eclipse.recommenders.internal.coordinates.rcp.ProjectCoordinateJsonTypeAdapter;
import org.eclipse.recommenders.rcp.IRcpService;
import org.eclipse.recommenders.utils.Result;
import org.eclipse.recommenders.utils.gson.OptionalJsonTypeAdapter;
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
import com.google.common.util.concurrent.AbstractIdleService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class EclipseProjectCoordinateAdvisorService extends AbstractIdleService
        implements IProjectCoordinateAdvisorService, IRcpService {

    private static final Logger LOG = LoggerFactory.getLogger(EclipseProjectCoordinateAdvisorService.class);

    private final ProjectCoordinateAdvisorService delegate;
    private final CoordinatesRcpPreferences prefs;
    private final LoadingCache<DependencyInfo, Optional<ProjectCoordinate>> projectCoordinateCache;

    private final File persistenceFile;
    private final Gson cacheGson;

    @SuppressWarnings("serial")
    private final Type cacheType = new TypeToken<Map<DependencyInfo, Optional<ProjectCoordinate>>>() {
    }.getType();

    private Map<IProjectCoordinateAdvisor, AdvisorDescriptor> descriptors = Maps.newHashMap();

    @Inject
    public EclipseProjectCoordinateAdvisorService(@Named(IDENTIFIED_PROJECT_COORDINATES) File persistenceFile,
            EventBus bus, CoordinatesRcpPreferences prefs) {
        bus.register(this);
        this.prefs = prefs;
        this.delegate = new ProjectCoordinateAdvisorService();
        this.persistenceFile = persistenceFile;
        this.cacheGson = new GsonBuilder()
                .registerTypeAdapter(ProjectCoordinate.class, new ProjectCoordinateJsonTypeAdapter())
                .registerTypeAdapter(DependencyInfo.class, new DependencyInfoJsonTypeAdapter())
                .registerTypeAdapter(Optional.class, new OptionalJsonTypeAdapter<ProjectCoordinate>())
                .enableComplexMapKeySerialization().serializeNulls().create();
        projectCoordinateCache = createCache();
    }

    private LoadingCache<DependencyInfo, Optional<ProjectCoordinate>> createCache() {
        return CacheBuilder.newBuilder().expireAfterAccess(30, MINUTES)
                .build(new CacheLoader<DependencyInfo, Optional<ProjectCoordinate>>() {

                    @Override
                    public Optional<ProjectCoordinate> load(DependencyInfo info) {
                        return delegate.suggest(info);
                    }
                });
    }

    @Override
    public ImmutableList<IProjectCoordinateAdvisor> getAdvisors() {
        return delegate.getAdvisors();
    }

    @Override
    public void addAdvisor(IProjectCoordinateAdvisor advisor) {
        delegate.addAdvisor(advisor);
    }

    @Override
    public void setAdvisors(List<IProjectCoordinateAdvisor> advisors) {
        delegate.setAdvisors(advisors);
    }

    public AdvisorDescriptor getDescriptor(IProjectCoordinateAdvisor advisor) {
        return descriptors.get(advisor);
    }

    /**
     * Looks up the ProjectCoordinate and resolves if necessary. This method blocks until the service is started and may
     * be long-running.
     */
    @Override
    public Optional<ProjectCoordinate> suggest(DependencyInfo dependencyInfo) {
        try {
            awaitRunning();
            return projectCoordinateCache.get(dependencyInfo);
        } catch (Exception e) {
            log(ERROR_IN_ADVISOR_SERVICE_SUGGEST, e, dependencyInfo.toString());
            return absent();
        }
    }

    @Override
    public Result<ProjectCoordinate> trySuggest(DependencyInfo dependencyInfo) {
        Optional<ProjectCoordinate> pc = projectCoordinateCache.getIfPresent(dependencyInfo);
        if (pc == null) {
            return Result.absent(REASON_NOT_IN_CACHE);
        } else if (pc.isPresent()) {
            return Result.of(pc.get());
        } else {
            return Result.absent();
        }
    }

    @PostConstruct
    public void open() throws IOException {
        startAsync();
    }

    @Override
    protected void startUp() throws Exception {
        configureAdvisorList(prefs.advisorConfiguration);

        if (!persistenceFile.exists()) {
            return;
        }
        String json = Files.toString(persistenceFile, Charsets.UTF_8);
        Map<DependencyInfo, Optional<ProjectCoordinate>> deserializedCache = cacheGson.fromJson(json, cacheType);

        for (Entry<DependencyInfo, Optional<ProjectCoordinate>> entry : deserializedCache.entrySet()) {
            projectCoordinateCache.put(entry.getKey(), entry.getValue());
        }
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
                LOG.error("Exception during creation of advisor {}.", descriptor.getId(), e); //$NON-NLS-1$
            }
        }
        descriptors = newDescriptors;
        return advisors;
    }

    @PreDestroy
    public void close() throws IOException {
        stopAsync();
    }

    @Override
    protected void shutDown() throws Exception {
        String json = cacheGson.toJson(projectCoordinateCache.asMap(), cacheType);
        Files.write(json, persistenceFile, Charsets.UTF_8);
    }

    @Subscribe
    public void onEvent(ProjectCoordinateChangeEvent e) {
        projectCoordinateCache.invalidate(e.dependencyInfo);
    }

    @Subscribe
    public void onEvent(AdvisorConfigurationChangedEvent e) throws IOException {
        clearCache();
        configureAdvisorList(prefs.advisorConfiguration);
    }

    public void clearCache() {
        projectCoordinateCache.invalidateAll();
    }
}
