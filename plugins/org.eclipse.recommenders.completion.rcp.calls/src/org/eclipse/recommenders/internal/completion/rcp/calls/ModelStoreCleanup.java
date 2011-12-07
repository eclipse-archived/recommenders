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
package org.eclipse.recommenders.internal.completion.rcp.calls;

import static org.eclipse.recommenders.internal.completion.rcp.calls.CallsCompletionModule.CALLS_STORE_LOCATION;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.eclipse.recommenders.internal.completion.rcp.calls.store.ModelArchive;
import org.eclipse.recommenders.rcp.RecommendersPlugin;
import org.eclipse.recommenders.utils.VersionRange;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class ModelStoreCleanup {

    private final File modelArchiveLocation;

    @Inject
    public ModelStoreCleanup(@Named(CALLS_STORE_LOCATION) final File modelArchiveLocation) {
        this.modelArchiveLocation = modelArchiveLocation;
    }

    public void initializeModelIndex() {
        final List<ModelArchive> deprecatedArchives = findDeprecatedArchives(loadArchives());
        deleteArchives(deprecatedArchives);
    }

    protected List<ModelArchive> loadArchives() {
        final List<ModelArchive> archives = Lists.newLinkedList();
        final Collection<File> files = FileUtils.listFiles(modelArchiveLocation,
                FileFilterUtils.suffixFileFilter(".zip"), TrueFileFilter.INSTANCE);
        for (final File file : files) {
            archives.add(new ModelArchive(file));
        }
        return archives;
    }

    private void deleteArchives(final List<ModelArchive> deprecatedArchives) {
        for (final ModelArchive archive : deprecatedArchives) {
            try {
                archive.close();
                archive.getFile().delete();
            } catch (final IOException e) {
                RecommendersPlugin.logError(e, "Exception while trying to delete deprecated ModelArchive: "
                        + archive.getFile().getAbsolutePath());
            }
        }
    }

    private List<ModelArchive> findDeprecatedArchives(final List<ModelArchive> archives) {
        final List<ModelArchive> deprecatedArchives = Lists.newLinkedList();
        final HashMultimap<String, ModelArchive> archivesPerName = HashMultimap.create();

        for (final ModelArchive archive : archives) {
            final String name = archive.getManifest().getName();
            archivesPerName.put(name, archive);
        }

        for (final String name : archivesPerName.keySet()) {
            deprecatedArchives.addAll(findDeprecatedArchivesAssumeSameName(archivesPerName.get(name)));
        }
        return deprecatedArchives;
    }

    private Collection<ModelArchive> findDeprecatedArchivesAssumeSameName(final Set<ModelArchive> archives) {
        final List<ModelArchive> deprecatedArchives = Lists.newLinkedList();

        for (final ModelArchive archive : archives) {
            final HashSet<ModelArchive> otherArchives = Sets.newHashSet(archives);
            otherArchives.remove(archive);
            if (!hasUniqueOrNewerParts(archive, otherArchives)) {
                deprecatedArchives.add(archive);
            }
        }

        return deprecatedArchives;
    }

    private boolean hasUniqueOrNewerParts(final ModelArchive candidate, final HashSet<ModelArchive> archives) {
        List<VersionRange> currentRanges = Lists.newLinkedList();
        currentRanges.add(candidate.getManifest().getVersionRange());
        final Date candidateTimestamp = candidate.getManifest().getTimestamp();

        for (final ModelArchive archive : archives) {
            final Date timestamp = archive.getManifest().getTimestamp();
            if (candidateTimestamp.compareTo(timestamp) < 0) {
                final VersionRange checkRange = archive.getManifest().getVersionRange();
                currentRanges = removeFromRanges(currentRanges, checkRange);
            }
        }
        return currentRanges.size() > 0;
    }

    private List<VersionRange> removeFromRanges(final List<VersionRange> currentRanges, final VersionRange checkRange) {
        final List<VersionRange> newRanges = Lists.newLinkedList();
        for (final VersionRange range : currentRanges) {
            newRanges.addAll(range.getResidues(checkRange));
        }
        return newRanges;
    }
}
