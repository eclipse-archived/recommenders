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

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Version implements Comparable<Version> {

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
        int major = 0;
        int minor = 0;
        int micro = 0;
        String qualifier = "";
        try {
            final StringTokenizer tokenizer = new StringTokenizer(version, ".", true);
            major = parseInt(tokenizer);
            if (tokenizer.hasMoreTokens()) {
                consumeDelimiter(tokenizer);
                minor = parseInt(tokenizer);
                if (tokenizer.hasMoreTokens()) {
                    consumeDelimiter(tokenizer);
                    micro = parseInt(tokenizer);
                    if (tokenizer.hasMoreTokens()) {
                        consumeDelimiter(tokenizer);
                        qualifier = parseString(tokenizer);
                        if (tokenizer.hasMoreTokens()) {
                            Throws.throwIllegalArgumentException("couldn't convert string into version: '%s'", version);
                        }
                    }
                }
            }
        } catch (final NoSuchElementException e) {
            Throws.throwIllegalArgumentException("couldn't convert string into version: '%s'", version);
        }
        return create(major, minor, micro, qualifier);
    }

    private static String parseString(final StringTokenizer st) {
        return st.nextToken();
    }

    private static int parseInt(final StringTokenizer st) {
        return Integer.parseInt(st.nextToken());
    }

    private static void consumeDelimiter(final StringTokenizer st) {
        st.nextToken();
    }

    protected Version(final int major, final int minor, final int micro, final String qualifier) {
        this.major = major;
        this.minor = minor;
        this.micro = micro;
        this.qualifier = qualifier;
    }

    public final int major;

    public final int minor;

    public final int micro;

    public final String qualifier;

    @Override
    public int compareTo(final Version v) {
        if (major != v.major) {
            return major - v.major;
        } else if (minor != v.minor) {
            return minor - v.minor;
        } else if (micro != v.micro) {
            return micro - v.micro;
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
}
