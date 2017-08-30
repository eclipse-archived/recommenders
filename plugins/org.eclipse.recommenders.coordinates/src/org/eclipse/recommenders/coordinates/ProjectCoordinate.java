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
package org.eclipse.recommenders.coordinates;

import static org.eclipse.recommenders.coordinates.Coordinates.isValidId;
import static org.eclipse.recommenders.utils.Checks.ensureIsTrue;
import static org.eclipse.recommenders.utils.Versions.isValidVersion;

import java.util.Objects;

import org.eclipse.recommenders.utils.Checks;
import org.eclipse.recommenders.utils.Versions;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;

/**
 * Represents a triple "group-id:artifact-id:version".
 */
public final class ProjectCoordinate {

    /**
     * Constant that represents an unknown project coordinate. Use this constant whenever <code>null</code> or
     * {@link Optional#absent()} is not appropriate.
     */
    public static final ProjectCoordinate UNKNOWN = new ProjectCoordinate("UNKNOWN", "UNKNOWN", "0.0.0");

    private final String groupId;
    private final String artifactId;
    private final String version;

    /**
     * Creates a new coordinate. Note that <code>null</code> values are replaced with an empty string.
     *
     * @throws IllegalArgumentException
     *             If the coordinate parts have an invalid format.
     *
     * @see Versions#isValidVersion(String)
     * @see Coordinates#isValidId(String)
     */
    public ProjectCoordinate(String groupId, String artifactId, String version) {
        ensureIsTrue(isValidId(groupId));
        ensureIsTrue(isValidId(artifactId));
        ensureIsTrue(isValidVersion(version));
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, artifactId, version);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (this.getClass() != other.getClass()) {
            return false;
        }
        ProjectCoordinate that = (ProjectCoordinate) other;
        return Objects.equals(this.artifactId, that.artifactId) && Objects.equals(this.groupId, that.groupId)
                && Objects.equals(this.version, that.version);
    }

    @Override
    public String toString() {
        return Joiner.on(':').join(getGroupId(), getArtifactId(), getVersion());
    }

    public static ProjectCoordinate valueOf(String coord) {
        String[] segments = coord.split(":");
        Checks.ensureIsInRange(segments.length, 3, 3, "Coordiante '%s' has invalid number of segments: %d", coord,
                segments.length);
        return new ProjectCoordinate(segments[0], segments[1], segments[2]);
    }
}
