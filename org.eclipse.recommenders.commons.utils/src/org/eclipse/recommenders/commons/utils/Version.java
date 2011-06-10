/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.commons.utils;

import static java.lang.String.format;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.eclipse.recommenders.commons.utils.parser.OsgiVersionParser;

public class Version implements Comparable<Version> {

    public static final Version ZERO = create(0, 0, 0);
    public static final Version UNKNOWN = create(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
    public static final Version LATEST = create(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);

    public static Version create(final int major, final int minor, final int micro, final String qualifier) {
        final Version res = new Version(major, minor, micro, qualifier);
        return res;
    }

    public static Version create(final int major, final int minor, final int micro) {
        return create(major, minor, micro, "");
    }

    public static Version create(final int major, final int minor) {
        return create(major, minor, 0, "");
    }

    public static Version valueOf(final String version) {
        return new OsgiVersionParser().parse(version);
    }

    protected Version() {
    }

    protected Version(final int major, final int minor, final int micro, final String qualifier) {
        this.major = major;
        this.minor = minor;
        this.micro = micro;
        this.qualifier = qualifier;
    }

    public int major;

    public int minor;

    public int micro;

    public String qualifier = "";

    @Override
    public int compareTo(final Version v) {
        if (major != v.major) {
            return new Integer(major).compareTo(v.major);
        } else if (minor != v.minor) {
            return new Integer(minor).compareTo(v.minor);
        } else if (micro != v.micro) {
            return new Integer(micro).compareTo(v.micro);
        } else {
            return qualifier.compareTo(v.qualifier);
        }
    }

    @Override
    public String toString() {
        final String res = qualifier.isEmpty() ? format("%d.%d.%d", major, minor, micro) : format("%d.%d.%d.%s", major,
                minor, micro, qualifier);
        return res;
    }

    @Override
    public boolean equals(final Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    public boolean isUnknown() {
        return equals(UNKNOWN);
    }
}
