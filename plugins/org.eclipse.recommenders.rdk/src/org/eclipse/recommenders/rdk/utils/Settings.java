/**
 * Copyright (c) 2010, 2012 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.rdk.utils;

import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Optional.of;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.eclipse.recommenders.utils.Throws.throwIllegalArgumentException;

import java.io.File;

import org.eclipse.recommenders.utils.annotations.Provisional;

import com.google.common.base.Optional;

/**
 * Utility class for working with system properties and converting their values to the expected type. Conversion is done
 * automatically but may return {@link Optional#absent()} if the property is not set.
 * <p>
 * Note that runtime exceptions that may happen during conversion are propagated to the caller.
 * 
 * @see System#getProperty(String)
 */
@Provisional
public class Settings {

    public static final String EXAMPLE_ARTIFACTS_URL = "cr.example.artifacts.url";
    public static final String ANALYSIS_ARTIFACTS_URL = "cr.analysis.artifacts.url";
    public static final String MODEL_ARTIFACTS_URL = "cr.model.artifacts.url";
    public static final String LOCAL_REPOSITORY_PATH = "cr.local.repository.path";

    public static Optional<File> getFile(String key) {
        String value = System.getProperty(key);
        return isEmpty(value) ? Optional.<File> absent() : of(new File(value));
    }

    public static Optional<Boolean> getBool(String key) {
        String value = System.getProperty(key);
        if (isEmpty(value))
            return Optional.<Boolean> absent();
        else if (equalsIgnoreCase(value, "true"))
            return Optional.of(true);
        else if (equalsIgnoreCase(value, "false"))
            return Optional.of(false);
        else
            throw throwIllegalArgumentException("Invalid value '%s' for boolean property '%s'", value, key);
    }

    public static Optional<Integer> getInt(String key) {
        String value = System.getProperty(key);
        return isEmpty(value) ? Optional.<Integer> absent() : of(Integer.parseInt(value));
    }

    public static Optional<String> getString(String key) {
        String value = System.getProperty(key);
        return fromNullable(value);
    }
}