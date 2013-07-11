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
package org.eclipse.recommenders.models.rcp.maybenotneeded;

import static com.google.common.base.Optional.fromNullable;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

import org.eclipse.recommenders.models.ProjectCoordinate;
import org.eclipse.recommenders.utils.Openable;
import org.eclipse.recommenders.utils.gson.GsonUtil;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;

public class ProjectCoordinateRegistry implements Closeable, Openable {

    private final File store;
    private final Map<File, ProjectCoordinateStatus> coords = Maps.newTreeMap();

    public ProjectCoordinateRegistry(File store) {
        this.store = store;
    }

    public void set(File location, ProjectCoordinate coord) {
        coords.put(location, new ProjectCoordinateStatus(coord, location, location.lastModified()));
    }

    public Optional<ProjectCoordinateStatus> get(File location) {
        return fromNullable(coords.get(location));
    }

    public boolean contains(File location) {
        return coords.containsKey(location);
    }

    @Override
    public void close() throws IOException {
        GsonUtil.serialize(coords.values(), store);
    }

    @Override
    public void open() throws IOException {
        Type type = new TypeToken<Set<ProjectCoordinateStatus>>() {
        }.getType();
        Set<ProjectCoordinateStatus> values = GsonUtil.deserialize(store, type);
        for (ProjectCoordinateStatus s : values) {
            if (s.location.lastModified() == s.lastModified) {
                coords.put(s.location, s);
            }
        }
    }

    public static class ProjectCoordinateStatus {

        public final ProjectCoordinate project;
        private final File location;
        private final long lastModified;

        public ProjectCoordinateStatus(ProjectCoordinate project, File location, long lastModified) {
            this.location = location;
            this.lastModified = lastModified;
            this.project = project;
        }

        public ProjectCoordinate getProject() {
            return project;
        }

        public File getLocation() {
            return location;
        }

        public long getLastModified() {
            return lastModified;
        }
    }
}
