/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 */
package org.eclipse.recommenders.mining.calls.data.couch;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.fromNullable;

import java.util.Collection;
import java.util.Set;

import org.eclipse.recommenders.commons.udc.LibraryIdentifier;
import org.eclipse.recommenders.commons.udc.ModelSpecification;
import org.eclipse.recommenders.utils.Version;
import org.eclipse.recommenders.utils.VersionRange;
import org.eclipse.recommenders.utils.VersionRange.VersionRangeBuilder;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

public class CouchModelSpecificationsGenerator implements Runnable {

    @Inject
    private CouchDbDataAccess db;

    @Override
    public void run() {
        final Collection<ModelSpecification> modelSpecs = db.getModelSpecifications();
        final Collection<LibraryIdentifier> libraryIdentifiers = db.getLibraryIdentifiers();
        for (final LibraryIdentifier libraryIdentifier : libraryIdentifiers) {
            final Optional<ModelSpecification> modelSpecOption = findMatchingSpecificationsForLibraryId(
                    libraryIdentifier, modelSpecs);

            ModelSpecification modelSpec;
            if (modelSpecOption.isPresent()) {
                modelSpec = modelSpecOption.get();
                modelSpec.addFingerprint(libraryIdentifier.fingerprint);
            } else {
                modelSpec = createSimpleModelSpecification(libraryIdentifier);
            }
            db.save(modelSpec);
            modelSpecs.add(modelSpec);
        }
    }

    private Optional<ModelSpecification> findMatchingSpecificationsForLibraryId(final LibraryIdentifier libId,
            final Collection<ModelSpecification> modelSpecs) {
        for (final ModelSpecification modelSpec : modelSpecs) {
            if (isFingerprintIncludedInSpec(libId, modelSpec) || isNameAndVersionCompatible(libId, modelSpec)) {
                return fromNullable(modelSpec);
            }
        }
        return absent();
    }

    private boolean isFingerprintIncludedInSpec(final LibraryIdentifier libId, final ModelSpecification modelSpec) {
        return modelSpec.containsFingerprint(libId.fingerprint);
    }

    private boolean isNameAndVersionCompatible(final LibraryIdentifier libId, final ModelSpecification modelSpec) {
        return modelSpec.getVersionRange().isIncluded(libId.version) && modelSpec.containsSymbolicName(libId.name);
    }

    private ModelSpecification createSimpleModelSpecification(final LibraryIdentifier libId) {
        final VersionRange range = createSimpleVersionRange(libId.version);
        final Set<String> fingerprints = Sets.newHashSet(libId.fingerprint);
        return new ModelSpecification(libId.name, new String[0], range, null, fingerprints);
    }

    private static VersionRange createSimpleVersionRange(final Version version) {
        if (version.isUnknown()) {
            return VersionRange.EMPTY;
        } else {
            final int minMajor = version.major;
            final int minMinor = version.minor;
            final Version minVersion = Version.create(minMajor, minMinor);
            final Version maxVersion = Version.create(minMajor, minMinor + 1);
            return new VersionRangeBuilder().minInclusive(minVersion).maxExclusive(maxVersion).build();
        }
    }

}
