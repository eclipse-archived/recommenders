/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 */
package org.eclipse.recommenders.commons.udc;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.eclipse.recommenders.utils.VersionRange;

public class Manifest {

    public static Manifest NULL = new Manifest("", VersionRange.ALL, new Date(0));

    private String name;
    private VersionRange versionRange;
    private Date timestamp;

    protected Manifest() {
    }

    public Manifest(final String name, final VersionRange version, final Date timestamp) {
        // XXX: is null for each of these fields allowed?
        this.name = name;
        this.versionRange = version;
        this.timestamp = timestamp;
    }

    public String getName() {
        return name;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public VersionRange getVersionRange() {
        return versionRange;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    // TODO: Move somewhere else
    public String getIdentifier() {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmm");
        final String time = getTimestamp() == null ? "" : dateFormat.format(getTimestamp());
        return getName() + "_" + getVersionRange() + "_" + time;
    }
}
