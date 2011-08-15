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

import java.util.List;

import org.eclipse.recommenders.commons.utils.parser.VersionParserFactory;

import com.google.common.collect.Lists;

public class VersionRange {

    public static final VersionRange EMPTY = new VersionRangeBuilder().minExclusive(Version.ZERO)
            .maxExclusive(Version.ZERO).build();
    public static VersionRange ALL = new VersionRangeBuilder().minInclusive(Version.UNKNOWN)
            .maxInclusive(Version.LATEST).build();

    public static class VersionRangeBuilder {

        private Version minVersion = Version.ZERO;

        private boolean minVersionInclusive = true;

        private Version maxVersion = Version.LATEST;

        private boolean maxVersionInclusive = true;

        public VersionRangeBuilder() {
        }

        public VersionRangeBuilder minInclusive(final Version version) {
            minVersionInclusive = true;
            minVersion = version;
            return this;
        }

        public VersionRangeBuilder minExclusive(final Version version) {
            minVersionInclusive = false;
            minVersion = version;
            return this;
        }

        public VersionRangeBuilder maxInclusive(final Version version) {
            maxVersionInclusive = true;
            maxVersion = version;
            return this;
        }

        public VersionRangeBuilder maxExclusive(final Version version) {
            maxVersionInclusive = false;
            maxVersion = version;
            return this;
        }

        public VersionRange build() {
            return new VersionRange(minVersion, minVersionInclusive, maxVersion, maxVersionInclusive);
        }
    }

    private Version minVersion;

    private boolean minVersionInclusive;

    private Version maxVersion;

    private boolean maxVersionInclusive;

    protected VersionRange() {

    }

    public VersionRange(final Version minVersion, final boolean minVersionInclusive, final Version maxVersion,
            final boolean maxVersionInclusive) {
        this.minVersion = minVersion;
        this.minVersionInclusive = minVersionInclusive;
        this.maxVersion = maxVersion;
        this.maxVersionInclusive = maxVersionInclusive;
    }

    public boolean isIncluded(final Version version) {
        Checks.ensureIsNotNull(version);
        final int minCheck = isMinVersionInclusive() ? 0 : 1;
        final int maxCheck = isMaxVersionInclusive() ? 0 : -1;
        return version.compareTo(getMinVersion()) >= minCheck && version.compareTo(getMaxVersion()) <= maxCheck;
    }

    /**
     * <pre>
     *  Example:
     * [_, 3].hasGreaterEqualUpperBoundThan( [_, 2] ) => true
     * Examples for cases where maxVersion is equal in both ranges:
     * [_, x].hasGreaterEqualUpperBoundThan( [_, x] ) => true
     * [_, x].hasGreaterEqualUpperBoundThan( [_, x[ ) => true
     * [_, x[.hasGreaterEqualUpperBoundThan( [_, x] ) => false
     * [_, x[.hasGreaterEqualUpperBoundThan( [_, x[ ) => true
     * </pre>
     */
    public boolean hasGreaterEqualUpperBoundThan(final VersionRange versionRange) {
        Checks.ensureIsNotNull(versionRange);
        final int versionCompare = versionRange.getMaxVersion().compareTo(getMaxVersion());
        if (versionCompare != 0) {
            return versionCompare < 0;
        }
        return isMaxVersionInclusive() || !versionRange.isMaxVersionInclusive() ? true : false;
    }

    public Version getMinVersion() {
        return minVersion;
    }

    public boolean isMinVersionInclusive() {
        return minVersionInclusive;
    }

    public Version getMaxVersion() {
        return maxVersion;
    }

    public boolean isMaxVersionInclusive() {
        return maxVersionInclusive;
    }

    @Override
    public String toString() {
        final String lowerBoundCharacter = isMinVersionInclusive() ? "[" : "(";
        final String upperBoundCharacter = isMaxVersionInclusive() ? "]" : ")";
        return String.format("%s%s,%s%s", lowerBoundCharacter, getMinVersion(), getMaxVersion(), upperBoundCharacter);
    }

    public boolean isVersionBelow(final Version version) {
        final int compare = minVersion.compareTo(version);
        if (compare == 0) {
            return !isMinVersionInclusive();
        }
        return compare > 0;
    }

    public boolean isVersionAbove(final Version version) {
        final int compare = maxVersion.compareTo(version);
        if (compare == 0) {
            return !isMaxVersionInclusive();
        }
        return compare < 0;
    }

    public boolean isUpperBoundHigherThan(final VersionRange range) {
        final int cmp = getMaxVersion().compareTo(range.getMaxVersion());
        if (cmp == 0) {
            return isMaxVersionInclusive() && !range.isMaxVersionInclusive();
        }
        return cmp > 0;
    }

    public boolean isLowerBoundLowerThan(final VersionRange range) {
        final int cmp = getMinVersion().compareTo(range.getMinVersion());
        if (cmp == 0) {
            return isMinVersionInclusive() && !range.isMinVersionInclusive();
        }
        return cmp < 0;
    }

    public boolean isUpperBoundEquals(final VersionRange range) {
        return isMaxVersionInclusive() == range.isMaxVersionInclusive()
                && getMaxVersion().equals(range.getMaxVersion());
    }

    public boolean isLowerBoundEquals(final VersionRange range) {
        return isMinVersionInclusive() == range.isMinVersionInclusive()
                && getMinVersion().equals(range.getMinVersion());
    }

    public boolean isEmpty() {
        final int compare = getMinVersion().compareTo(getMaxVersion());
        if (compare < 0) {
            return false;
        } else if (compare == 0 && isMinVersionInclusive() && isMaxVersionInclusive()) {
            return false;
        }
        return true;
    }

    public List<VersionRange> getResidues(final VersionRange range) {
        final List<VersionRange> residues = Lists.newLinkedList();
        final VersionRange lowerResidue = getLowerResidue(range);
        if (!lowerResidue.isEmpty()) {
            residues.add(lowerResidue);
        }
        final VersionRange upperResidue = getUpperResidue(range);
        if (!upperResidue.isEmpty()) {
            residues.add(upperResidue);
        }
        return residues;
    }

    private VersionRange getLowerResidue(final VersionRange range) {
        return new VersionRange(getMinVersion(), isMinVersionInclusive(), range.getMinVersion(),
                !range.isMinVersionInclusive());
    }

    private VersionRange getUpperResidue(final VersionRange range) {
        return new VersionRange(range.getMaxVersion(), !range.isMaxVersionInclusive(), getMaxVersion(),
                isMaxVersionInclusive());
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof VersionRange) {
            final VersionRange range = (VersionRange) obj;
            return minVersion.equals(range.minVersion) && maxVersion.equals(range.maxVersion)
                    && minVersionInclusive == range.minVersionInclusive
                    && maxVersionInclusive == range.maxVersionInclusive;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return minVersion.hashCode() ^ maxVersion.hashCode();
    }

    public static VersionRange create(final String versionString) {
        final VersionRange versionRange = new VersionRange();
        final char firstChar = versionString.charAt(0);
        final char lastChar = versionString.charAt(versionString.length() - 1);
        versionRange.minVersionInclusive = firstChar == '[';
        versionRange.maxVersionInclusive = lastChar == ']';

        final String[] versions = versionString.substring(1, versionString.length() - 1).split(",");
        if (versions.length != 2) {
            throw new IllegalArgumentException(String.format("Given string '%s' is not a valid VersionRange.",
                    versionString));
        }

        final String minVersionString = versions[0].trim();
        final String maxVersionString = versions[1].trim();

        versionRange.minVersion = VersionParserFactory.parse(minVersionString);
        versionRange.maxVersion = VersionParserFactory.parse(maxVersionString);
        return versionRange;
    }
}
