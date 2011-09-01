/**
 * Copyright (c) 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 */
package org.eclipse.recommenders.mining.calls.couch;

import java.util.Collection;
import java.util.Set;

import javax.inject.Inject;

import org.eclipse.recommenders.commons.udc.LibraryIdentifier;
import org.eclipse.recommenders.commons.udc.ModelSpecification;
import org.eclipse.recommenders.commons.utils.Version;
import org.eclipse.recommenders.commons.utils.VersionRange;
import org.eclipse.recommenders.commons.utils.VersionRange.VersionRangeBuilder;

import com.google.common.collect.Sets;

public class ModelSpecsGenerator {

    private final CouchDbDataAccess db;

    @Inject
    public ModelSpecsGenerator(final CouchDbDataAccess db) {
        this.db = db;
    }

    public void execute() {
        final Collection<ModelSpecification> modelSpecs = db.getModelSpecifications();
        final Collection<LibraryIdentifier> libraryIdentifiers = db.getLibraryIdentifiers();
        for (final LibraryIdentifier libraryIdentifier : libraryIdentifiers) {
            ModelSpecification modelSpec = findMatch(modelSpecs, libraryIdentifier);
            if (modelSpec == null) {
                modelSpec = createModelSpecification(libraryIdentifier);
            } else {
                modelSpec.addFingerprint(libraryIdentifier.fingerprint);
            }

            db.save(modelSpec);
            System.out.println("updated " + modelSpec.getIdentifier());
            modelSpecs.add(modelSpec);
        }
    }

    private ModelSpecification createModelSpecification(final LibraryIdentifier libraryIdentifier) {
        final VersionRange range = createVersionRange(libraryIdentifier);
        final Set<String> fingerprints = Sets.newHashSet(libraryIdentifier.fingerprint);
        return new ModelSpecification(libraryIdentifier.name, new String[0], range, null, fingerprints);
    }

    private VersionRange createVersionRange(final LibraryIdentifier libraryIdentifier) {
        if (libraryIdentifier.version.isUnknown()) {
            return VersionRange.EMPTY;
        } else {
            final int minMajor = libraryIdentifier.version.major;
            final int minMinor = libraryIdentifier.version.minor;
            final Version minVersion = Version.create(minMajor, minMinor);
            final Version maxVersion = Version.create(minMajor, minMinor + 1);
            return new VersionRangeBuilder().minInclusive(minVersion).maxExclusive(maxVersion).build();
        }
    }

    private ModelSpecification findMatch(final Collection<ModelSpecification> modelSpecs, final LibraryIdentifier libId) {
        for (final ModelSpecification modelSpec : modelSpecs) {
            if (hasNameOrAlias(modelSpec, libId.name)) {
                if (modelSpec.containsFingerprint(libId.fingerprint)
                        || modelSpec.getVersionRange().isIncluded(libId.version)) {
                    return modelSpec;
                }
            }
        }
        return null;
    }

    private boolean hasNameOrAlias(final ModelSpecification modelSpec, final String name) {
        if (modelSpec.getSymbolicName().equals(name)) {
            return true;
        }

        for (final String alias : modelSpec.getAliases()) {
            if (alias.equals(name)) {
                return true;
            }
        }
        return false;
    }

}
