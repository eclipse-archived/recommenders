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

public class VersionRange {

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
}
