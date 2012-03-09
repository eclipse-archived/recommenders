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

import static org.eclipse.recommenders.mining.calls.data.couch.GetMatchingFingerprintsFunction.collectFingerprintsMatchingModelSpec;

import java.util.Set;

import org.eclipse.recommenders.commons.udc.ModelSpecification;
import org.eclipse.recommenders.internal.analysis.codeelements.ObjectUsage;
import org.eclipse.recommenders.mining.calls.data.IObjectUsageProvider;

import com.google.inject.Inject;

public class CouchObjectUsageProvider implements IObjectUsageProvider {

    private final CouchDbDataAccess db;

    @Inject
    public CouchObjectUsageProvider(final CouchDbDataAccess db) {
        this.db = db;
    }

    @Override
    public Iterable<ObjectUsage> findObjectUsages(final ModelSpecification spec) {
        final Set<String> fingerprints = collectFingerprintsMatchingModelSpec(spec, db);
        return db.getObjectUsages(fingerprints);
    }
}