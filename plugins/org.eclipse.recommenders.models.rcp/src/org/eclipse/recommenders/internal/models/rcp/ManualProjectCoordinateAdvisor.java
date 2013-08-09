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

import static com.google.common.base.Optional.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.eclipse.recommenders.models.DependencyInfo;
import org.eclipse.recommenders.models.IProjectCoordinateAdvisor;
import org.eclipse.recommenders.models.ProjectCoordinate;
import org.eclipse.recommenders.rcp.IRcpService;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class ManualProjectCoordinateAdvisor implements IProjectCoordinateAdvisor, IRcpService {

    private Map<DependencyInfo, ProjectCoordinate> manualMappings = Maps.newHashMap();
    private final File persistenceFile;
    private final Gson gson;

    @SuppressWarnings("serial")
    private final Type type = new TypeToken<Map<DependencyInfo, ProjectCoordinate>>() {
    }.getType();

    @Inject
    public ManualProjectCoordinateAdvisor(@Named(ModelsRcpModule.MANUAL_MAPPINGS) File persistenceFile) {
        this.persistenceFile = persistenceFile;
        gson = new GsonBuilder().registerTypeAdapter(ProjectCoordinate.class, new ProjectCoordinateJsonTypeAdapter())
                .enableComplexMapKeySerialization().serializeNulls().create();
    }

    @Override
    public Optional<ProjectCoordinate> suggest(DependencyInfo dependencyInfo) {
        ProjectCoordinate pc = manualMappings.get(dependencyInfo);
        if (pc != null) {
            return of(pc);
        } else {
            return absent();
        }
    }

    public void setManualMapping(DependencyInfo dependencyInfo, ProjectCoordinate pc) {
        manualMappings.put(dependencyInfo, pc);
    }

    public void removeManualMapping(DependencyInfo dependencyInfo) {
        manualMappings.remove(dependencyInfo);
    }

    @PreDestroy
    public void close() throws IOException {
        String json = gson.toJson(manualMappings, type);
        Files.write(json, persistenceFile, Charsets.UTF_8);
    }

    @PostConstruct
    public void open() throws IOException {
        if (!persistenceFile.exists()) {
            return;
        }
        String json = Files.toString(persistenceFile, Charsets.UTF_8);
        Map<DependencyInfo, ProjectCoordinate> deserialized = gson.fromJson(json, type);
        if (deserialized != null) {
            manualMappings = deserialized;
        }
    }

}
