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
import org.eclipse.recommenders.commons.utils.Version;

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
            replaceIfServesAndNewer(newModelArchive, packageRoot);
        }
    }

    private void replaceIfServesAndNewer(final IModelArchive newModelArchive, final IPackageFragmentRoot packageRoot) {
        final LibraryIdentifier libraryIdentifier = packageRoot2Id.get(packageRoot);
        final Manifest manifest = newModelArchive.getManifest();
        if (serves(libraryIdentifier, manifest)) {
            final IModelArchive previousArchive = packageRoot2modelArchive.get(packageRoot);
            if (isPreviousMatchOlder(previousArchive, newModelArchive)) {
                packageRoot2modelArchive.put(packageRoot, newModelArchive);
            }
        }
    }

    public void setResolved(final IPackageFragmentRoot packageRoot, final LibraryIdentifier libraryId) {
        packageRoot2Id.put(packageRoot, libraryId);
        final IModelArchive archive = findMatchingModelArchive(libraryId);
        packageRoot2modelArchive.put(packageRoot, archive);
    }

    private IModelArchive findMatchingModelArchive(final LibraryIdentifier libraryIdentifier) {
        IModelArchive bestMatch = IModelArchive.NULL;
        for (final IModelArchive archive : archives) {
            if (serves(libraryIdentifier, archive.getManifest()) && isPreviousMatchOlder(bestMatch, archive)) {
                bestMatch = archive;
            }
        }
        return bestMatch;
    }

    private boolean serves(final LibraryIdentifier libraryIdentifier, final Manifest manifest) {
        final Version libraryVersion = libraryIdentifier.version;
        final boolean isNameEquals = libraryIdentifier.name.equals(manifest.getName());
        final boolean isVersionIncluded = manifest.getVersionRange().isIncluded(libraryVersion);
        return isNameEquals && (isVersionIncluded || libraryVersion.isUnknown());
    }

    private boolean isPreviousMatchOlder(final IModelArchive previousMatch, final IModelArchive newMatch) {
        final Version prevMaxVersion = previousMatch.getManifest().getVersionRange().getMaxVersion();
        final Version newMaxVersion = newMatch.getManifest().getVersionRange().getMaxVersion();

        return previousMatch.getManifest().getTimestamp().compareTo(newMatch.getManifest().getTimestamp()) < 0;
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
