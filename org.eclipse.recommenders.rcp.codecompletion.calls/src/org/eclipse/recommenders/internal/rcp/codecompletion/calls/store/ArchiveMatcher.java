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

import java.util.List;
import java.util.ListIterator;

import org.eclipse.recommenders.commons.utils.Version;
import org.eclipse.recommenders.commons.utils.VersionRange;
import org.eclipse.recommenders.commons.utils.VersionRange.VersionRangeBuilder;

import com.google.common.collect.Lists;

public class ArchiveMatcher {

    private final List<IModelArchive> archives;
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
        final ListIterator<IModelArchive> iterator = archives.listIterator();
        while (iterator.hasNext()) {
            final Manifest manifest = iterator.next().getManifest();
            if (!name.equals(manifest.getName())) {
                iterator.remove();
            }
        }
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
        closestUpperMatch = null;
        VersionRange closestRange = new VersionRangeBuilder().minExclusive(Version.LATEST).build();
        for (final IModelArchive archive : archives) {
            final VersionRange range = archive.getManifest().getVersionRange();
            if (range.isVersionBelow(targetVersion)) {
                if (closestUpperMatch == null || range.isLowerBoundLowerThan(closestRange)) {
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
        closestLowerMatch = null;
        VersionRange closestRange = new VersionRangeBuilder().maxExclusive(Version.UNKNOWN).build();
        for (final IModelArchive archive : archives) {
            final VersionRange range = archive.getManifest().getVersionRange();
            if (range.isVersionAbove(targetVersion)) {
                if (closestLowerMatch == null || range.isUpperBoundHigherThan(closestRange)) {
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
