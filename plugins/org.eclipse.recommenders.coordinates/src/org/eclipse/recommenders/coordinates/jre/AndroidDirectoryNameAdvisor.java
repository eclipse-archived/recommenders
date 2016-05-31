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
package org.eclipse.recommenders.coordinates.jre;

import static com.google.common.base.Optional.absent;
import static java.lang.Integer.parseInt;
import static org.apache.commons.lang3.StringUtils.removeStart;

import java.io.File;

import org.eclipse.recommenders.coordinates.AbstractProjectCoordinateAdvisor;
import org.eclipse.recommenders.coordinates.Coordinates;
import org.eclipse.recommenders.coordinates.DependencyInfo;
import org.eclipse.recommenders.coordinates.DependencyType;
import org.eclipse.recommenders.coordinates.ProjectCoordinate;
import org.eclipse.recommenders.utils.Versions;

import com.google.common.base.Optional;

public class AndroidDirectoryNameAdvisor extends AbstractProjectCoordinateAdvisor {

    private static final String GROUP_ID = "com.google.android";
    private static final String ARTIFACT_ID = "android";

    private static final String ANDROID_TARGET_PREFIX = "android-";

    /**
     * @see <a href="https://developer.android.com/reference/android/os/Build.VERSION_CODES.html">Android Version
     *      Codes</a>
     */
    private static final String[] VERSION_CODES = new String[] { null, // No API 0
            "1.0", // API 1
            "1.1", // API 2
            "1.5", // API 3: Cupcake
            "1.6", // API 4: Donut
            "2.0", // API 5: Eclair
            "2.0.1", // API 6: Eclair
            "2.1", // API 7: Eclair
            "2.2", // API 8: Froyo
            "2.3", // API 9: Gingerbread
            "2.3.3", // API 10: Gingerbread
            "3.0", // API 11: Honeycomb
            "3.1", // API 12: Honeycomb
            "3.2", // API 13: Honeycomb
            "4.0", // API 14: Ice Cream Sandwich
            "4.0.3", // API 15: Ice Cream Sandwich
            "4.1", // API 16: Jelly Bean
            "4.2", // API 17: Jelly Bean
            "4.3", // API 18: Jelly Bean
            "4.4", // API 19: Kit Kat
            "4.5", // API 20: Kit Kat Watch (technically 4.4W)
            "5.0", // API 21: Lollipop
            "5.1", // API 22: Lollipop MR1
            "6.0", // API 23: M (Marshmallow)
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
