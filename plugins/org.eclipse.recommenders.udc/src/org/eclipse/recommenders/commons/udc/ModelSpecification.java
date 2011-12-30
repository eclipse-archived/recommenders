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

import static com.google.common.base.Optional.fromNullable;
import static org.eclipse.recommenders.utils.Checks.ensureIsNotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.eclipse.recommenders.utils.VersionRange;
import org.eclipse.recommenders.utils.annotations.Nullable;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;

public class ModelSpecification {

    private static String[] EQUALS_EXCLUDE_FIELDS = new String[] { "_id", "_rev" };

    public String _id;
    public String _rev;

    private String symbolicName;
    private String[] aliases;
    private VersionRange versionRange;
    private Date lastBuilt;
    private final Set<String> fingerprints;

    protected ModelSpecification() {
        fingerprints = Sets.newHashSet();
    }

    public ModelSpecification(final String symbolicName, final String[] aliases, final VersionRange version,
            @Nullable final Date timestamp, final Set<String> fingerprints) {
        ensureIsNotNull(symbolicName);
        ensureIsNotNull(aliases);
        ensureIsNotNull(version);
        ensureIsNotNull(fingerprints);
        this.symbolicName = symbolicName;
        this.aliases = aliases;
        this.versionRange = version;
        this.lastBuilt = timestamp;
        this.fingerprints = fingerprints;
    }

    /**
     * Returns union of the symbolic name and all known aliases.
     */
    public Set<String> getAllSymbolicNames() {
        final Set<String> res = Sets.newHashSet(aliases);
        res.add(symbolicName);
        return res;
    }

    public String getSymbolicName() {
        return symbolicName;
    }

    public Optional<Date> getLastBuilt() {
        return fromNullable(lastBuilt);
    }

    public String[] getAliases() {
        return aliases;
    }

    public VersionRange getVersionRange() {
        return versionRange;
    }

    @Override
    public String toString() {
        return String.format("[%s, %s, %s, %s]", symbolicName, versionRange, lastBuilt, fingerprints.toString());
    }

    public String getIdentifier() {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmm");
        final String time = lastBuilt == null ? "" : dateFormat.format(lastBuilt);
        return getSymbolicName() + "_" + getVersionRange() + "_" + time;
    }

    public boolean isAfterLastBuilt(final Date timestamp) {
        if (lastBuilt == null) {
            return true;
        } else {
            return lastBuilt.compareTo(timestamp) < 0;
        }
    }

    public void setLatestBuilt(final Date lastBuilt) {
        this.lastBuilt = lastBuilt;
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this, EQUALS_EXCLUDE_FIELDS);
    }

    @Override
    public boolean equals(final Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj, EQUALS_EXCLUDE_FIELDS);
    }

    public void addFingerprint(final String fingerprint) {
        fingerprints.add(fingerprint);
    }

    /**
     * @see #getAllSymbolicNames()
     */
    public boolean containsSymbolicName(final String name) {
        return getAllSymbolicNames().contains(name);
    }

    public boolean containsFingerprint(final String fingerprint) {
        return fingerprints.contains(fingerprint);
    }

    public Set<String> getFingerprints() {
        return fingerprints;
    }
}
