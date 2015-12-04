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

import static com.google.common.base.Optional.*;
import static org.eclipse.recommenders.internal.coordinates.rcp.l10n.LogMessages.ERROR_FAILED_TO_READ_MANUAL_MAPPINGS;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.FileUtils;
import org.eclipse.recommenders.coordinates.DependencyInfo;
import org.eclipse.recommenders.coordinates.IProjectCoordinateAdvisor;
import org.eclipse.recommenders.coordinates.ProjectCoordinate;
import org.eclipse.recommenders.internal.coordinates.rcp.l10n.LogMessages;
import org.eclipse.recommenders.rcp.IRcpService;
import org.eclipse.recommenders.utils.Logs;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

public class ManualProjectCoordinateAdvisor implements IProjectCoordinateAdvisor, IRcpService {

    private final File persistenceFile;
    private final Gson gson;

    private Map<DependencyInfo, ProjectCoordinate> manualMappings = Maps.newHashMap();

    @SuppressWarnings("serial")
    private static final Type MANUAL_MAPPINGS_TYPE_TOKEN = new TypeToken<Map<DependencyInfo, ProjectCoordinate>>() {
    }.getType();

    @Inject
    public ManualProjectCoordinateAdvisor(@Named(CoordinatesRcpModule.MANUAL_MAPPINGS) File persistenceFile) {
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

    @PostConstruct
    public void open() throws IOException {
        if (!persistenceFile.exists()) {
            return;
        }
        try {
            String json = Files.toString(persistenceFile, StandardCharsets.UTF_8);
            Map<DependencyInfo, ProjectCoordinate> deserialized = gson.fromJson(json, MANUAL_MAPPINGS_TYPE_TOKEN);
            if (deserialized != null) {
                manualMappings = deserialized;
            }
        } catch (IOException | JsonParseException e) {
            Logs.log(ERROR_FAILED_TO_READ_MANUAL_MAPPINGS, e, persistenceFile);
            return;
        }
    }

    @PreDestroy
    public void close() throws IOException {
        try {
            String json = gson.toJson(manualMappings, MANUAL_MAPPINGS_TYPE_TOKEN);
            Files.write(json, persistenceFile, StandardCharsets.UTF_8);
        } catch (IOException e) {
            Logs.log(LogMessages.ERROR_FAILED_TO_WRITE_MANUAL_MAPPINGS, e, persistenceFile);

            // Delete the file (if it exists at all) so not to leave it in a corrupt state.
            FileUtils.deleteQuietly(persistenceFile);
        }
    }
}
