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

import static com.google.common.base.Optional.*;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.regex.Pattern;

import com.google.common.base.Optional;

public class Coordinates {

    public static ModelCoordinate toModelCoordinate(ProjectCoordinate pc, String classifier, String extension) {
        return new ModelCoordinate(pc.getGroupId(), pc.getArtifactId(), classifier, extension, pc.getVersion());
    }

    public static ProjectCoordinate toProjectCoordinate(ModelCoordinate mc) {
        return new ProjectCoordinate(mc.getGroupId(), mc.getArtifactId(), mc.getVersion());
    }

    private static final Pattern idPattern = Pattern.compile("[A-Za-z0-9_\\-.]+");

    /**
     * Check if a (non version) part of a coordinate like artifactId or groupId is not empty and contain no whitespace
     * character.
     */
    public static boolean isValidId(String id) {
        if (isBlank(id)) {
            return false;
        }
        return idPattern.matcher(id).matches();
    }

    /**
     * Creates a new coordinate. If one of the entered strings have an invalid format <code>absent()</code> is returned.
     * 
     * @see VersionStrings#isValidVersion(String)
     * @see Coordinates#isValidId(String)
     */
    public static Optional<ProjectCoordinate> tryNewProjectCoordinate(String groupId, String artifactId, String version) {
        try {
            return of(new ProjectCoordinate(groupId, artifactId, version));
        } catch (IllegalArgumentException e) {
            return absent();
        }
    }

    private Coordinates() {
    }
}
