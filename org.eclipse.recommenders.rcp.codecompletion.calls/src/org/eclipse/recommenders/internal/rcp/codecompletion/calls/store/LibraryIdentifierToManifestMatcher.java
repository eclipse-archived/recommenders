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

import org.eclipse.recommenders.commons.lfm.LibraryIdentifier;

public class LibraryIdentifierToManifestMatcher {

    private final List<IModelArchive> archives;
    private final LibraryIdentifier libraryId;
    private IModelArchive bestMatch = IModelArchive.NULL;

    public LibraryIdentifierToManifestMatcher(final LibraryIdentifier libraryId, final List<IModelArchive> archives) {
        this.libraryId = libraryId;
        this.archives = archives;

        findBestMatch();
    }

    private void findBestMatch() {
        for (final IModelArchive archive : archives) {
            if (isExactMatch(archive)) {
                bestMatch = archive;
                return;
            }
        }
    }

    private boolean isExactMatch(final IModelArchive archive) {
        // TODO
        return false;
    }

    public IModelArchive getBestMatch() {
        return bestMatch;
    }
}
