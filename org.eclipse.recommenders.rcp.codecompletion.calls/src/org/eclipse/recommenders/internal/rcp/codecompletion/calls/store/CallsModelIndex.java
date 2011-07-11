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
import java.util.concurrent.ConcurrentMap;

import org.eclipse.recommenders.commons.lfm.LibraryIdentifier;

import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;

public class CallsModelIndex {

    private final List<IModelArchive> archives = Lists.newLinkedList();

    private final ConcurrentMap<LibraryIdentifier, IModelArchive> libraryIdentifier2modelArchive = new MapMaker()
            .makeMap();

    public void register(final IModelArchive newModelArchive) {
        archives.add(newModelArchive);

        for (final LibraryIdentifier libraryIdentifier : libraryIdentifier2modelArchive.keySet()) {
            updateArchiveReferenceIfBetterMatch(newModelArchive, libraryIdentifier);
        }
    }

    private void updateArchiveReferenceIfBetterMatch(final IModelArchive newModelArchive,
            final LibraryIdentifier libraryIdentifier) {
        if (isBetterMatch(newModelArchive, libraryIdentifier)) {
            libraryIdentifier2modelArchive.put(libraryIdentifier, newModelArchive);
        }
    }

    IModelArchive findMatchingModelArchive(final LibraryIdentifier libraryIdentifier) {
        final ArchiveMatcher matcher = new ArchiveMatcher(archives, libraryIdentifier);
        return matcher.getBestMatch();
    }

    List<IModelArchive> getArchives() {
        return archives;
    }

    private boolean isBetterMatch(final IModelArchive newModelArchive, final LibraryIdentifier libraryIdentifier) {
        final IModelArchive previousMatch = libraryIdentifier2modelArchive.get(libraryIdentifier);
        final ArchiveMatcher matcher = new ArchiveMatcher(Lists.newArrayList(previousMatch, newModelArchive),
                libraryIdentifier);
        return matcher.getBestMatch() == newModelArchive;
    }

    public IModelArchive findModelArchive(final LibraryIdentifier libraryIdentifier) {
        IModelArchive modelArchive = libraryIdentifier2modelArchive.get(libraryIdentifier);
        if (modelArchive == null) {
            modelArchive = findMatchingModelArchive(libraryIdentifier);
            libraryIdentifier2modelArchive.put(libraryIdentifier, modelArchive);
        }
        return modelArchive;
    }

}
