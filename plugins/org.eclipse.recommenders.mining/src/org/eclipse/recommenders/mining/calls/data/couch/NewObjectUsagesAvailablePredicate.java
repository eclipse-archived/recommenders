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

import java.util.Date;
import java.util.Set;

import org.eclipse.recommenders.commons.udc.ModelSpecification;

import com.google.common.base.Predicate;

public class NewObjectUsagesAvailablePredicate implements Predicate<ModelSpecification> {

    private final CouchDbDataAccess db;
    private final boolean forceModelGeneration;
    private ModelSpecification spec;
    private Set<String> fingerprints;

    public NewObjectUsagesAvailablePredicate(final CouchDbDataAccess db, final boolean forceModelGeneration) {
        this.db = db;
        this.forceModelGeneration = forceModelGeneration;
    }

    @Override
    public boolean apply(final ModelSpecification spec) {
        if (forceModelGeneration) {
            return true;
        }
        this.spec = spec;
        this.fingerprints = GetMatchingFingerprintsFunction.collectFingerprintsMatchingModelSpec(spec, db);
        final boolean hasNewObjectUsages = hasNewObjectUsages();
        return hasNewObjectUsages;
    }

    private boolean hasNewObjectUsages() {
        final Date latestCuTimestamp = getLatestTimestampForFingerprints(fingerprints);
        return spec.isAfterLastBuilt(latestCuTimestamp);
    }

    private Date getLatestTimestampForFingerprints(final Set<String> fingerprints) {
        Date latest = new Date(0);
        for (final String fingerprint : fingerprints) {
            final Date timestamp = db.getLatestTimestampForFingerprint(fingerprint);
            if (latest.compareTo(timestamp) <= 0) {
                latest = timestamp;
            }
        }
        return latest;
    }

}