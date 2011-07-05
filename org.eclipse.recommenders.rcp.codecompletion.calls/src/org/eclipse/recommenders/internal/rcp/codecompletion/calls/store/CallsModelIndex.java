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

import org.eclipse.jdt.core.IPackageFragmentRoot;

import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;

public class CallsModelIndex {

    private final List<IModelArchive> archives = Lists.newLinkedList();
    private final ConcurrentMap<IPackageFragmentRoot, IModelArchive> packageRoot2modelArchive = new MapMaker()
            .makeMap();
    private final ConcurrentMap<IPackageFragmentRoot, LibraryIdentifier> packageRoot2Id = new MapMaker().makeMap();

    public void register(final IModelArchive newModelArchive) {
        archives.add(newModelArchive);

        for (final IPackageFragmentRoot packageRoot : packageRoot2Id.keySet()) {
            updateArchiveReferenceIfBetterMatch(newModelArchive, packageRoot);
        }
    }

    public void setResolved(final IPackageFragmentRoot packageRoot, final LibraryIdentifier libraryId) {
        packageRoot2Id.put(packageRoot, libraryId);
        final IModelArchive archive = findMatchingModelArchive(libraryId);
        packageRoot2modelArchive.put(packageRoot, archive);
    }

    private IModelArchive findMatchingModelArchive(final LibraryIdentifier libraryIdentifier) {
        final ArchiveMatcher matcher = new ArchiveMatcher(archives, libraryIdentifier);
        return matcher.getBestMatch();
    }

    private void updateArchiveReferenceIfBetterMatch(final IModelArchive newModelArchive, final IPackageFragmentRoot packageRoot) {
        final IModelArchive previousMatch = packageRoot2modelArchive.get(packageRoot);
        if (previousMatch == null || previousMatch == IModelArchive.NULL || isBetterMatch(newModelArchive, packageRoot)) {
            packageRoot2modelArchive.put(packageRoot, newModelArchive);
        }
    }

    private boolean isBetterMatch(final IModelArchive newModelArchive, final IPackageFragmentRoot packageRoot) {
        final IModelArchive previousMatch = packageRoot2modelArchive.get(packageRoot);
        final LibraryIdentifier libraryIdentifier = packageRoot2Id.get(packageRoot);
        final ArchiveMatcher matcher = new ArchiveMatcher(Lists.newArrayList(previousMatch, newModelArchive),
                libraryIdentifier);
        return matcher.getBestMatch() == newModelArchive;
    }

    public void load(final IPackageFragmentRoot[] packageFragmentRoots) {
        scheduleLookup(packageFragmentRoots);
    }

    boolean isLibraryIdentifierResolved(final IPackageFragmentRoot packageRoot) {
        return packageRoot2Id.containsKey(packageRoot);
    }

    private void scheduleLookup(final IPackageFragmentRoot[] packageRoots) {
        new LibraryIdentifierResolverJob(this, packageRoots).schedule();
    }

    public IModelArchive getModelArchive(final IPackageFragmentRoot packageFragmentRoot) {
        final IModelArchive modelArchive = packageRoot2modelArchive.get(packageFragmentRoot);
        if (modelArchive == null) {
            return IModelArchive.NULL;
        } else {
            return modelArchive;
        }
    }

}
