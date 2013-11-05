/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 *    Olav Lenz - addes hints.
 */
package org.eclipse.recommenders.models;

import static org.eclipse.recommenders.models.Coordinates.isValidId;
import static org.eclipse.recommenders.utils.Checks.ensureIsTrue;
import static org.eclipse.recommenders.utils.Versions.isValidVersion;

import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.eclipse.recommenders.utils.Throws;
import org.eclipse.recommenders.utils.Versions;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

/**
 * Represents a Maven-like artifact coordinate which consists of "group-id:artifact-id:classifier:extension:version".
 * This class is used instead of Aether's Artifact class (i) to hide the usage of the Aether from clients, and (ii) to
 * make clear that it's only a resource identifier; it does not locate a (resolved) resource.
 */
public final class ModelCoordinate {

    public static final ModelCoordinate UNKNOWN = new ModelCoordinate("unknown", "unknown", "unknown", "unknown",
            "0.0.0");

    /**
     * Hint key for repository urls
     */
    public static final String HINT_REPOSITORY_URL = "repository.url";

    private final String groupId;
    private final String artifactId;
    private final String version;
    private final String classifier;
    private final String extension;

    private Map<String, String> hints = Maps.newHashMap();

    /**
     * Creates a new coordinate.
     * 
     * @throws IllegalArgumentException
     *             If the entered strings have an invalid format.
     * 
     * @see Versions#isValidVersion(String)
     * @see Coordinates#isValidId(String)
     */
    public ModelCoordinate(String groupId, String artifactId, String classifier, String extension, String version) {
        ensureIsTrue(isValidId(artifactId));
        ensureIsTrue(isValidId(extension));
        ensureIsTrue(isValidVersion(version));
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.classifier = classifier;
        this.extension = extension;
        this.version = version;
    }

    /**
     * Creates a new coordinate.
     * 
     * @throws IllegalArgumentException
     *             If the entered strings have an invalid format.
     * 
     * @see Versions#isValidVersion(String)
     * @see Coordinates#isValidId(String)
     */
    public ModelCoordinate(String groupId, String artifactId, String classifier, String extension, String version,
            Map<String, String> hints) {
        this(groupId, artifactId, classifier, extension, version);
        this.hints = hints;
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

    public String getClassifier() {
        return classifier;
    }

    public String getExtension() {
        return extension;
    }

    public Optional<String> getHint(String key) {
        return Optional.fromNullable(hints.get(key));
    }

    public ImmutableMap<String, String> getHints() {
        return ImmutableMap.copyOf(hints);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public String toString() {
        return Joiner.on(':').useForNull("")
                .join(getGroupId(), getArtifactId(), getClassifier(), getExtension(), getVersion());
    }

    public static ModelCoordinate valueOf(String coord) {
        String[] split = coord.split(":");
        String gid = null, aid = null, ext = null, clss = null, vers = null;
        switch (split.length) {
        case 1:
        case 2:
            throw Throws.throwIllegalArgumentException("Invalid coordinate format. It has only %d segments: %s",
                    split.length, coord);
        case 3:
            vers = split[2];
            break;
        case 4:
            clss = split[2];
            vers = split[3];
            break;
        case 5:
            clss = split[2];
            ext = split[3];
            vers = split[4];
            break;
        default:
            Throws.throwIllegalArgumentException("Invalid coordinate format. It has only %d segments: %s",
                    split.length, coord);
        }
        gid = split[0];
        aid = split[1];
        return new ModelCoordinate(gid, aid, clss, ext, vers);
    }

}
