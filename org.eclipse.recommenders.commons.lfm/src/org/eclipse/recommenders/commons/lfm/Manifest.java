package org.eclipse.recommenders.commons.lfm;

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

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.eclipse.recommenders.commons.utils.VersionRange;

public class Manifest {

    public static Manifest NULL = new Manifest("", VersionRange.ALL, new Date(0));

    private String name;
    private VersionRange versionRange;
    private Date timestamp;

    protected Manifest() {
    }

    public Manifest(final String name, final VersionRange version, final Date timestamp) {
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

    // TODO: Move somewhere else
    public String getIdentifier() {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmm");
        final String time = getTimestamp() == null ? "" : dateFormat.format(getTimestamp());
        return getName() + "_" + getVersionRange() + "_" + time;
    }
}
