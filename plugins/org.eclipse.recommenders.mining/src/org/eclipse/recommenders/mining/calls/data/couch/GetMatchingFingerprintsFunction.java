/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marcel Bruch - Initial API and implementation
 */
package org.eclipse.recommenders.mining.calls.data.couch;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.Callable;

import org.eclipse.recommenders.commons.udc.LibraryIdentifier;
import org.eclipse.recommenders.commons.udc.ModelSpecification;

import com.google.common.collect.Sets;

public class GetMatchingFingerprintsFunction implements Callable<Set<String>> {

    private final ModelSpecification spec;
    private final CouchDbDataAccess db;

    public GetMatchingFingerprintsFunction(final ModelSpecification spec, final CouchDbDataAccess db) {
        this.spec = spec;
        this.db = db;
    }

    public static Set<String> collectFingerprintsMatchingModelSpec(final ModelSpecification spec,
            final CouchDbDataAccess db) {
        return new GetMatchingFingerprintsFunction(spec, db).call();
    }

    @Override
    public Set<String> call() {
        final Set<String> res = Sets.newHashSet();
        for (final String name : spec.getAllSymbolicNames()) {
            final Collection<LibraryIdentifier> libIds = db.getLibraryIdentifiersForSymbolicName(name);
            for (final LibraryIdentifier libId : libIds) {
                if (spec.getVersionRange().isIncluded(libId.version)) {
                    res.add(libId.fingerprint);
                }
            }
        }
        return res;
    }
}
