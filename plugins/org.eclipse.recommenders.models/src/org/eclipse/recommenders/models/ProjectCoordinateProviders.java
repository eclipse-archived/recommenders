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
package org.eclipse.recommenders.models;

import static com.google.common.base.Optional.of;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;

/**
 * Set of IDE unspecific {@link ProjectCoordinate} providers that may be reused by IDE specific implementations of
 * {@link IProjectCoordinateProvider}.
 */
public class ProjectCoordinateProviders {

    /**
     * Reads the {@link ProjectCoordinate}s of a project from an pom file - if it exists. Returns
     * {@link Optional#absent()} if not found or parsing failed.
     */
    public static final Function<File, Optional<ProjectCoordinate>> POM_PROJECT = new Function<File, Optional<ProjectCoordinate>>() {
        @Override
        public Optional<ProjectCoordinate> apply(File projectRoot) {
            return of(ProjectCoordinate.UNKNOWN);
        };
    };

    /**
     * Reads the {@link ProjectCoordinate}s of a project from <code>META-INF/MANIFEST.MF</code> file - if it exists.
     * Returns {@link Optional#absent()} if not found or parsing failed.
     */
    public static final Function<File, Optional<ProjectCoordinate>> OSGI_PROJECT = new Function<File, Optional<ProjectCoordinate>>() {
        @Override
        public Optional<ProjectCoordinate> apply(File projectRoot) {
            return of(ProjectCoordinate.UNKNOWN);
        };
    };

    /**
     * Reads the {@link ProjectCoordinate}s of a jar file from a pom.properties file - if it exists. Returns
     * {@link Optional#absent()} if not found or parsing failed.
     */
    public static final Function<File, Optional<ProjectCoordinate>> POM_JAR = new Function<File, Optional<ProjectCoordinate>>() {
        @Override
        public Optional<ProjectCoordinate> apply(File jarFile) {
            return of(ProjectCoordinate.UNKNOWN);
        };
    };

    /**
     * Reads the {@link ProjectCoordinate}s of a jar file's manifest - if it exists. Returns {@link Optional#absent()}
     * if not found or parsing failed.
     */
    public static final Function<File, Optional<ProjectCoordinate>> OSGI_JAR = new Function<File, Optional<ProjectCoordinate>>() {
        @Override
        public Optional<ProjectCoordinate> apply(File jarFile) {
            return of(ProjectCoordinate.UNKNOWN);
        };
    };

    /**
     * Uses a web service such as mvn central to find a {@link ProjectCoordinate}s based on the fingerprint of the given
     * jar file. Returns {@link Optional#absent()} if the given jar is not known to the web service.
     */
    public static final Function<File, Optional<ProjectCoordinate>> FINGERPRINT_JAR = new Function<File, Optional<ProjectCoordinate>>() {
        @Override
        public Optional<ProjectCoordinate> apply(File jarFile) {
            return of(ProjectCoordinate.UNKNOWN);
        };
    };

    public static class ManualMappings implements Function<File, Optional<ProjectCoordinate>>, Closeable {

        private File store;
        private final Map<File, ProjectCoordinate> mappings;

        public ManualMappings(File store) {
            this.store = store;
            // load data...
            mappings = Maps.newHashMap();
        }

        public ManualMappings(Map<File, ProjectCoordinate> mappings) {
            this.mappings = mappings;
        }

        @Override
        public Optional<ProjectCoordinate> apply(File location) {
            return of(ProjectCoordinate.UNKNOWN);
        }

        public void put(File location, ProjectCoordinate coord) {
            mappings.put(location, coord);
        }

        @Override
        public void close() throws IOException {
            if (store == null) {
                return;
            }

        }
    }
}
