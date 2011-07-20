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
package org.eclipse.recommenders.tests.rcp.codecompletion.calls.unit;

import java.util.Collection;
import java.util.Set;

import org.eclipse.recommenders.commons.lfm.Manifest;
import org.eclipse.recommenders.commons.utils.VersionRange;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.store.IModelArchive;
import org.eclipse.recommernders.server.lfm.model.LibraryIdentifier;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

public class BestMatchforUnknownModelFinder {

    private final class SameNamePredicate implements Predicate<IModelArchive> {
        private final LibraryIdentifier library;

        private SameNamePredicate(final LibraryIdentifier library) {
            this.library = library;
        }

        @Override
        public boolean apply(final IModelArchive input) {
            final Manifest mf = input.getManifest();
            return mf.getName().equals(library.name);
        }
    }

    public IModelArchive findBestMatch(final LibraryIdentifier library, final Set<IModelArchive> modelArchives) {
        final Collection<IModelArchive> sameNames = Collections2.filter(modelArchives, new SameNamePredicate(library));
        IModelArchive bestMatch = IModelArchive.NULL;
        for (final IModelArchive cur : sameNames) {
            final VersionRange bestMatchRange = bestMatch.getManifest().getVersionRange();
            final VersionRange currentRange = cur.getManifest().getVersionRange();
            if (currentRange.hasGreaterEqualUpperBoundThan(bestMatchRange)) {
                bestMatch = cur;
            }
        }
        return bestMatch;
    }
}
