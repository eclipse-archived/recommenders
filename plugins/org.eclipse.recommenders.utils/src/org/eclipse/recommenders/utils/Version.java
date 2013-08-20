/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Sewe - initial API and implementation.
 */
package org.eclipse.recommenders.utils;

import static java.lang.String.format;

import java.util.regex.Pattern;

import com.google.common.base.Preconditions;

public class Version implements Comparable<Version> {

    private static final Pattern VALID_VERSIONS = Pattern.compile("\\d+\\.\\d+\\.\\d+");

    private final int major;
    private final int minor;
    private final int patch;

    public static Version valueOf(String version) {
        Preconditions.checkArgument(VALID_VERSIONS.matcher(version).matches());
        String[] components = version.split("\\.");
        return new Version(components[0], components[1], components[2]);
    }

    public Version(final int major, final int minor, final int patch) {
        Preconditions.checkArgument(major >= 0);
        Preconditions.checkArgument(minor >= 0);
        Preconditions.checkArgument(patch >= 0);
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    public Version(String major, String minor, String patch) {
        this(Integer.valueOf(major), Integer.valueOf(minor), Integer.valueOf(patch));
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getPatch() {
        return patch;
    }

    @Override
    public int compareTo(final Version that) {
        if (major != that.major) {
            return Integer.valueOf(major).compareTo(that.major);
        } else if (minor != that.minor) {
            return Integer.valueOf(minor).compareTo(that.minor);
        } else {
            return Integer.valueOf(patch).compareTo(that.patch);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + major;
        result = prime * result + minor;
        result = prime * result + patch;
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else if (other == null) {
            return false;
        } else if (getClass() != other.getClass()) {
            return false;
        }

        Version that = (Version) other;
        return major == that.major && minor == that.minor && patch == that.patch;
    }

    @Override
    public String toString() {
        return format("%d.%d.%d", major, minor, patch);
    }
}
