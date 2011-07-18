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
package org.eclipse.recommenders.commons.lfm;

import java.util.Collection;
import java.util.List;

import org.eclipse.recommenders.commons.utils.Version;
import org.eclipse.recommenders.commons.utils.VersionRange;
import org.eclipse.recommenders.commons.utils.VersionRange.VersionRangeBuilder;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

public class ManifestMatcher {

    private Collection<Manifest> manifests;
    private Manifest bestExactMatch;
    private Manifest closestUpperMatch;
    private Manifest bestMatch = Manifest.NULL;
    private Manifest closestLowerMatch;
    private Version targetVersion;

    public ManifestMatcher(final Collection<Manifest> manifests, final LibraryIdentifier libraryIdentifier) {
        this.manifests = manifests;
        setTargetVersion(libraryIdentifier.version);

        filterByName(libraryIdentifier.name);
        if (findBestExactMatch()) {
            bestMatch = bestExactMatch;
        } else if (findClosestUpperMatch()) {
            bestMatch = closestUpperMatch;
        } else if (findClosestLowerMatch()) {
            bestMatch = closestLowerMatch;
        }
    }

    private void setTargetVersion(final Version version) {
        if (version.equals(Version.UNKNOWN)) {
            targetVersion = Version.LATEST;
        } else {
            targetVersion = version;
        }
    }

    private void filterByName(final String name) {
        manifests = Collections2.filter(manifests, new Predicate<Manifest>() {

            @Override
            public boolean apply(final Manifest input) {
                final String manifestNname = input.getName();
                return name.equals(manifestNname);
            }
        });
    }

    private boolean findBestExactMatch() {
        final List<Manifest> matches = getExactVersionMatches();
        bestExactMatch = getNewestArchive(matches);
        return bestExactMatch != Manifest.NULL;
    }

    private Manifest getNewestArchive(final List<Manifest> archives) {
        Manifest newest = Manifest.NULL;
        for (final Manifest archive : archives) {
            if (isPreviousMatchOlder(newest, archive)) {
                newest = archive;
            }
        }
        return newest;
    }

    private List<Manifest> getExactVersionMatches() {
        final List<Manifest> exactMatches = Lists.newLinkedList();
        for (final Manifest manifest : manifests) {
            if (manifest.getVersionRange().isIncluded(targetVersion)) {
                exactMatches.add(manifest);
            }
        }
        return exactMatches;
    }

    private boolean findClosestUpperMatch() {
        VersionRange closestRange = new VersionRangeBuilder().minExclusive(Version.LATEST).build();
        for (final Manifest manifest : manifests) {
            final VersionRange range = manifest.getVersionRange();
            if (range.isVersionBelow(targetVersion)) {
                if (range.isLowerBoundLowerThan(closestRange)) {
                    closestUpperMatch = manifest;
                    closestRange = range;
                } else if (range.isLowerBoundEquals(closestRange) && isPreviousMatchOlder(closestUpperMatch, manifest)) {
                    closestUpperMatch = manifest;
                    closestRange = range;
                }
            }
        }
        return closestUpperMatch != null;
    }

    private boolean findClosestLowerMatch() {
        VersionRange closestRange = new VersionRangeBuilder().maxExclusive(Version.UNKNOWN).build();
        for (final Manifest manifest : manifests) {
            final VersionRange range = manifest.getVersionRange();
            if (range.isVersionAbove(targetVersion)) {
                if (range.isUpperBoundHigherThan(closestRange)) {
                    closestLowerMatch = manifest;
                    closestRange = range;
                } else if (range.isUpperBoundEquals(closestRange) && isPreviousMatchOlder(closestLowerMatch, manifest)) {
                    closestLowerMatch = manifest;
                    closestRange = range;
                }
            }
        }
        return closestLowerMatch != null;
    }

    private boolean isPreviousMatchOlder(final Manifest previousMatch, final Manifest newMatch) {
        return previousMatch.getTimestamp().compareTo(newMatch.getTimestamp()) < 0;
    }

    public Manifest getBestMatch() {
        return bestMatch;
    }

}
