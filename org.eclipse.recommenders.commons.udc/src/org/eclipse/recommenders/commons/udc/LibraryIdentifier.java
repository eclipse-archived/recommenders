package org.eclipse.recommenders.commons.udc;

/**
 * Copyright (c) 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 */

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.eclipse.recommenders.commons.utils.Version;

import com.google.gson.annotations.SerializedName;

public class LibraryIdentifier {

    private static String[] EQUALS_EXCLUDE_FIELDS = new String[] { "_rev" };

    public static LibraryIdentifier UNKNOWN = new LibraryIdentifier("", Version.UNKNOWN, "");

    private String _rev;

    public String name;
    public Version version;
    @SerializedName("_id")
    public String fingerprint;

    protected LibraryIdentifier() {
        // Only used for deserialization
    }

    public LibraryIdentifier(final String name, final Version version, final String fingerprint) {
        this.name = name;
        this.version = version;
        this.fingerprint = fingerprint;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this, EQUALS_EXCLUDE_FIELDS);
    }

    @Override
    public boolean equals(final Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj, EQUALS_EXCLUDE_FIELDS);
    }
}
