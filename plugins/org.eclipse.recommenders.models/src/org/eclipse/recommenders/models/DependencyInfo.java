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
package org.eclipse.recommenders.models;

import static org.eclipse.recommenders.utils.Checks.ensureIsNotNull;
import static org.eclipse.recommenders.utils.Checks.ensureIsTrue;

import java.io.File;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

public class DependencyInfo {

    public static final String EXECUTION_ENVIRONMENT = "EXECUTION_ENVIRONMENT";
    public static final String EXECUTION_ENVIRONMENT_VERSION = "EXECUTION_ENVIRONMENT_VERSION";
    public static final String PROJECT_NAME = "PROJECT_NAME";
    public static final String SURROUNDING_PROJECT_FILE = "SURROUNDING_PROJECT_FILE";

    private final File file;
    private final DependencyType type;
    private final Map<String, String> hints;

    public DependencyInfo(File file, DependencyType type) {
        this(file, type, Collections.<String, String>emptyMap());
    }

    public DependencyInfo(File file, DependencyType type, Map<String, String> hint) {
        this.file = ensureIsNotNull(file);
        ensureIsTrue(file.isAbsolute());
        this.type = type;
        hints = ensureIsNotNull(hint);
    }

    public File getFile() {
        return file;
    }

    public DependencyType getType() {
        return type;
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
        return Objects.toStringHelper(this).addValue(file).addValue(type).addValue(hints).toString();
    }
}
