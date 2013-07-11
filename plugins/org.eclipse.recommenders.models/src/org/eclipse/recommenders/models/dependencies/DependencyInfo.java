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
package org.eclipse.recommenders.models.dependencies;

import java.io.File;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.eclipse.recommenders.utils.Checks;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

public class DependencyInfo {

    public static final String EXECUTION_ENVIRONMENT = "EXECUTION_ENVIRONMENT";
    public static final String EXECUTION_ENVIRONMENT_VERSION = "EXECUTION_ENVIRONMENT_VERSION";

    private final File file;
    private final DependencyType type;
    private final Map<String, String> attributes;

    public DependencyInfo(File file, DependencyType type) {
        this(file, type, Collections.<String, String> emptyMap());
    }

    public DependencyInfo(File file, DependencyType type, Map<String, String> attributes) {
        this.file = file;
        this.type = type;
        this.attributes = Checks.ensureIsNotNull(attributes);
    }

    public File getFile() {
        return file;
    }

    public DependencyType getType() {
        return type;
    }

    public Optional<String> getAttribute(String key) {
        return Optional.fromNullable(attributes.get(key));
    }

    public Map<String, String> getAttributeMap() {
        return ImmutableMap.copyOf(attributes);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(file).append(type).append(attributes).toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DependencyInfo) {
            final DependencyInfo other = (DependencyInfo) obj;
            return new EqualsBuilder().append(file, other.file).append(type, other.type)
                    .append(attributes, other.attributes).isEquals();
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return Objects.toStringHelper("").addValue(file).addValue(type).addValue(attributes).toString();
    }
}
