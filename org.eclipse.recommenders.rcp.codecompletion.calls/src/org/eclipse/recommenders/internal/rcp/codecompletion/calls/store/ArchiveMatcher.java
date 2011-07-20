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
package org.eclipse.recommenders.internal.rcp.codecompletion.calls.store;

import java.util.Collection;
import java.util.List;

import org.eclipse.recommenders.commons.lfm.Manifest;
import org.eclipse.recommenders.commons.utils.Version;
import org.eclipse.recommenders.commons.utils.VersionRange;
import org.eclipse.recommenders.commons.utils.VersionRange.VersionRangeBuilder;
import org.eclipse.recommernders.server.lfm.model.LibraryIdentifier;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

public class ArchiveMatcher {

    private Collection<IModelArchive> archives;
    private IModelArchive bestExactMatch;
    private IModelArchive closestUpperMatch;
    private IModelArchive bestMatch = IModelArchive.NULL;
    private IModelArchive closestLowerMatch;
    private Version targetVersion;

    public ArchiveMatcher(final List<IModelArchive> archives, final LibraryIdentifier libraryIdentifier) {
        this.archives = archives;
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
        archives = Collections2.filter(archives, new Predicate<IModelArchive>() {

            @Override
            public boolean apply(final IModelArchive input) {
                final String manifestNname = input.getManifest().getName();
                return name.equals(manifestNname);
            }
        });
    }

    private boolean findBestExactMatch() {
        final List<IModelArchive> matches = getExactVersionMatches();
        bestExactMatch = getNewestArchive(matches);
        return bestExactMatch != IModelArchive.NULL;
    }

    private IModelArchive getNewestArchive(final List<IModelArchive> archives) {
        IModelArchive newest = IModelArchive.NULL;
        for (final IModelArchive archive : archives) {
            if (isPreviousMatchOlder(newest, archive)) {
                newest = archive;
            }
        }
        return newest;
    }

    private List<IModelArchive> getExactVersionMatches() {
        final List<IModelArchive> exactMatches = Lists.newLinkedList();
        for (final IModelArchive archive : archives) {
            final Manifest manifest = archive.getManifest();
            if (manifest.getVersionRange().isIncluded(targetVersion)) {
                exactMatches.add(archive);
            }
        }
        return exactMatches;
    }

    private boolean findClosestUpperMatch() {
        VersionRange closestRange = new VersionRangeBuilder().minExclusive(Version.LATEST).build();
        for (final IModelArchive archive : archives) {
            final VersionRange range = archive.getManifest().getVersionRange();
            if (range.isVersionBelow(targetVersion)) {
                if (range.isLowerBoundLowerThan(closestRange)) {
                    closestUpperMatch = archive;
                    closestRange = range;
                } else if (range.isLowerBoundEquals(closestRange) && isPreviousMatchOlder(closestUpperMatch, archive)) {
                    closestUpperMatch = archive;
                    closestRange = range;
                }
            }
        }
        return closestUpperMatch != null;
    }

    private boolean findClosestLowerMatch() {
        VersionRange closestRange = new VersionRangeBuilder().maxExclusive(Version.UNKNOWN).build();
        for (final IModelArchive archive : archives) {
            final VersionRange range = archive.getManifest().getVersionRange();
            if (range.isVersionAbove(targetVersion)) {
                if (range.isUpperBoundHigherThan(closestRange)) {
                    closestLowerMatch = archive;
                    closestRange = range;
                } else if (range.isUpperBoundEquals(closestRange) && isPreviousMatchOlder(closestLowerMatch, archive)) {
                    closestLowerMatch = archive;
                    closestRange = range;
                }
            }
        }
        return closestLowerMatch != null;
    }

    private boolean isPreviousMatchOlder(final IModelArchive previousMatch, final IModelArchive newMatch) {
        return previousMatch.getManifest().getTimestamp().compareTo(newMatch.getManifest().getTimestamp()) < 0;
    }

    public IModelArchive getBestMatch() {
        return bestMatch;
    }

}
