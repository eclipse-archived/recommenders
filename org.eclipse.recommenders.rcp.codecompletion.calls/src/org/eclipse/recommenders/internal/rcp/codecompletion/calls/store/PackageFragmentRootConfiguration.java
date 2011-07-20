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

import org.eclipse.recommernders.server.lfm.model.LibraryIdentifier;

public class PackageFragmentRootConfiguration {

    private LibraryIdentifier libraryIdentifier;
    private UpdatePolicy updatePolicy;

    protected PackageFragmentRootConfiguration() {
        // Used for deserialization only
    }

    public PackageFragmentRootConfiguration(final LibraryIdentifier libraryIdentifier, final UpdatePolicy updatePolicy) {
        this.libraryIdentifier = libraryIdentifier;
        this.updatePolicy = updatePolicy;
    }

    public LibraryIdentifier getLibraryIdentifier() {
        return libraryIdentifier;
    }

    public UpdatePolicy getUpdatePolicy() {
        return updatePolicy;
    }

}
