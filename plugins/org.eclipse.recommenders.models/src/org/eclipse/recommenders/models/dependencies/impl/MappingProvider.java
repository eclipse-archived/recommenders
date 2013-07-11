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
package org.eclipse.recommenders.models.dependencies.impl;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.fromNullable;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import org.eclipse.recommenders.models.ProjectCoordinate;
import org.eclipse.recommenders.models.dependencies.DependencyInfo;
import org.eclipse.recommenders.models.dependencies.DependencyType;
import org.eclipse.recommenders.models.dependencies.IMappingProvider;
import org.eclipse.recommenders.models.dependencies.IProjectCoordinateResolver;
import org.eclipse.recommenders.utils.annotations.Testing;

import com.google.common.base.Optional;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class MappingProvider implements IMappingProvider {

    private List<IProjectCoordinateResolver> strategies = Lists.newArrayList();
    private final Cache<DependencyInfo, Optional<ProjectCoordinate>> cache;
    private final Map<DependencyInfo, ProjectCoordinate> manualMappings = Maps.newHashMap();

    public MappingProvider() {
        cache = CacheBuilder.newBuilder().maximumSize(200).recordStats().build();
    }

    public MappingProvider(Map<DependencyInfo, ProjectCoordinate> manualMappings) {
        this();
        setManualMappings(manualMappings);
    }

    @Override
    public List<IProjectCoordinateResolver> getStrategies() {
        return ImmutableList.copyOf(strategies);
    }

    @Override
    public void addStrategy(IProjectCoordinateResolver strategy) {
        strategies.add(strategy);
    }

    @Override
    public void setStrategies(List<IProjectCoordinateResolver> strategies) {
        this.strategies = strategies;
    }

    @Override
    public Optional<ProjectCoordinate> searchForProjectCoordinate(final DependencyInfo dependencyInfo) {
        try {
            return cache.get(dependencyInfo, new Callable<Optional<ProjectCoordinate>>() {

                @Override
                public Optional<ProjectCoordinate> call() throws Exception {
                    return extractProjectCoordinate(dependencyInfo);
                }
            });
        } catch (Exception e) {
            return absent();
        }
    }

    private Optional<ProjectCoordinate> extractProjectCoordinate(DependencyInfo dependencyInfo) {
        for (IProjectCoordinateResolver strategy : strategies) {
            Optional<ProjectCoordinate> optionalProjectCoordinate = strategy.searchForProjectCoordinate(dependencyInfo);
            if (optionalProjectCoordinate.isPresent()) {
                return optionalProjectCoordinate;
            }
        }
        return absent();
    }

    @Override
    public boolean isApplicable(DependencyType dependencyTyp) {
        for (IProjectCoordinateResolver mappingStrategy : strategies) {
            if (mappingStrategy.isApplicable(dependencyTyp)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void setManualMappings(Map<DependencyInfo, ProjectCoordinate> manualMappings) {
        this.manualMappings.clear();
        for (Entry<DependencyInfo, ProjectCoordinate> entry : manualMappings.entrySet()) {
            setManualMapping(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public Map<DependencyInfo, ProjectCoordinate> getManualMappings() {
        return ImmutableMap.copyOf(manualMappings);
    }

    @Testing
    public long getMissCount() {
        return cache.stats().missCount();
    }

    @Testing
    public long getHitCount() {
        return cache.stats().hitCount();
    }

    @Override
    public void setManualMapping(DependencyInfo dependencyInfo, ProjectCoordinate projectCoordinate) {
        manualMappings.put(dependencyInfo, projectCoordinate);
        cache.put(dependencyInfo, fromNullable(projectCoordinate));
    }

    @Override
    public void removeManualMapping(DependencyInfo dependencyInfo) {
        manualMappings.remove(dependencyInfo);
        cache.invalidate(dependencyInfo);
    }

    @Override
    public boolean isManualMapping(DependencyInfo dependencyInfo) {
        return manualMappings.containsKey(dependencyInfo);
    }

}
