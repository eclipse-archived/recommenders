/**
 * Copyright (c) 2010, 2014 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andreas Sewe - initial API and implementation
 */
package org.eclipse.recommenders.models.advisors;

import static com.google.common.base.Optional.absent;
import static java.lang.Integer.parseInt;
import static org.apache.commons.lang3.StringUtils.removeStart;

import java.io.File;

import org.eclipse.recommenders.models.Coordinates;
import org.eclipse.recommenders.models.DependencyInfo;
import org.eclipse.recommenders.models.DependencyType;
import org.eclipse.recommenders.models.ProjectCoordinate;
import org.eclipse.recommenders.utils.Versions;

import com.google.common.base.Optional;

public class AndroidDirectoryNameAdvisor extends AbstractProjectCoordinateAdvisor {

    private static final String GROUP_ID = "com.google.android";
    private static final String ARTIFACT_ID = "android";

    private static final String ANDROID_TARGET_PREFIX = "android-";

    /**
     * @see <a href="developer.android.com/reference/android/os/Build.VERSION_CODES.html">Android Version Codes</a>
     */
    private static final String[] VERSION_CODES = new String[] { null, // No API Version 0
            "1.0", //
            "1.1", //
            "1.5", // Cupcake
            "1.6", // Donut
            "2.0", // Eclair
            "2.0.1", // Eclair
            "2.1", // Eclair
            "2.2", // Froyo
            "2.3", // Gingerbread
            "2.3.3", // Gingerbread
            "3.0", // Honeycomb
            "3.1", // Honeycomb
            "3.2", // Honeycomb
            "4.0", // Ice Cream Sandwich
            "4.0.3", // Ice Cream Sandwich
            "4.1", // Jelly Bean
            "4.2", // Jelly Bean
            "4.3", // Jelly Bean
            "4.4", // Kit Kat
    };

    @Override
    protected boolean isApplicable(DependencyType type) {
        return type == DependencyType.JAR;
    }

    @Override
    protected Optional<ProjectCoordinate> doSuggest(DependencyInfo dependencyInfo) {
        File jar = dependencyInfo.getFile();

        if (!"android.jar".equals(jar.getName())) {
            return absent();
        }

        File directory = jar.getParentFile();

        if (directory == null) {
            return absent();
        }

        return asProjectCoordinate(directory.getName());
    }

    private Optional<ProjectCoordinate> asProjectCoordinate(String target) {
        try {
            int apiVersion = extractApiVersion(target);
            if (apiVersion < 1 || apiVersion >= VERSION_CODES.length) {
                return absent();
            }
            String version = Versions.canonicalizeVersion(VERSION_CODES[apiVersion]);
            return Coordinates.tryNewProjectCoordinate(GROUP_ID, ARTIFACT_ID, version);
        } catch (IllegalArgumentException e) {
            return absent();
        }
    }

    private int extractApiVersion(String target) {
        if (target.startsWith(ANDROID_TARGET_PREFIX)) {
            return parseInt(removeStart(target, ANDROID_TARGET_PREFIX));
        } else {
            throw new IllegalArgumentException("Cannot extract API version: " + target);
        }
    }
}
