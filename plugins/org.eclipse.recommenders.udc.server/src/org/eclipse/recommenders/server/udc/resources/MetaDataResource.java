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
package org.eclipse.recommenders.server.udc.resources;

import static org.eclipse.recommenders.utils.Checks.ensureIsNotEmpty;
import static org.eclipse.recommenders.utils.Checks.ensureIsNotNull;

import java.util.Collection;
import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.recommenders.commons.udc.DependencyInformation;
import org.eclipse.recommenders.commons.udc.LibraryIdentifier;
import org.eclipse.recommenders.commons.udc.Manifest;
import org.eclipse.recommenders.commons.udc.ManifestMatchResult;
import org.eclipse.recommenders.commons.udc.ModelSpecification;
import org.eclipse.recommenders.internal.server.udc.CouchDBAccessService;
import org.eclipse.recommenders.internal.server.udc.ManifestMatcher;
import org.eclipse.recommenders.utils.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

@Path("/")
public class MetaDataResource {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Inject
    private CouchDBAccessService dataAccess;

    @Produces({ MediaType.APPLICATION_JSON })
    @Consumes({ MediaType.APPLICATION_JSON })
    @POST
    @Path("/dependencyInfo")
    public void registerClasspathDependencyInfo(final DependencyInformation dependencyInfo) {
        findOrCreateLibraryIdentifier(dependencyInfo);
    }

    @Produces({ MediaType.APPLICATION_JSON })
    @Consumes({ MediaType.APPLICATION_JSON })
    @POST
    @Path("manifest")
    public ManifestMatchResult searchManifest(final DependencyInformation dependencyInfo) {
        log.debug("Received search request for {}.", dependencyInfo);

        final LibraryIdentifier libraryIdentifier = findOrCreateLibraryIdentifier(dependencyInfo);
        Manifest manifest = findFingerprintMatch(libraryIdentifier);
        if (manifest != null) {
            return new ManifestMatchResult(manifest);
        }

        final ManifestMatcher matcher = createManifestMatcher(libraryIdentifier);
        manifest = matcher.getBestMatch();
        if (manifest == Manifest.NULL) {
            return new ManifestMatchResult();
        } else {
            return new ManifestMatchResult(manifest);
        }
    }

    private Manifest findFingerprintMatch(final LibraryIdentifier libId) {
        final ModelSpecification modelSpec = dataAccess.getModelSpecificationByFingerprint(libId.fingerprint);
        if (modelSpec == null || !modelSpec.getLastBuilt().isPresent()) {
            return null;
        } else {
            return createManifest(modelSpec);
        }
    }

    private ManifestMatcher createManifestMatcher(final LibraryIdentifier libraryIdentifier) {
        final Collection<ModelSpecification> modelSpecs = dataAccess
                .getModelSpecificationsByNameOrAlias(libraryIdentifier.name);
        final Collection<Manifest> potentialManifests = Lists.newLinkedList();
        for (final ModelSpecification modelSpec : modelSpecs) {
            if (modelSpec.getLastBuilt().isPresent()) {
                potentialManifests.add(createManifest(modelSpec));
            }
        }
        final ManifestMatcher matcher = new ManifestMatcher(potentialManifests, libraryIdentifier, false);
        return matcher;
    }

    private Manifest createManifest(final ModelSpecification modelSpec) {

        final Date timestamp = modelSpec.getLastBuilt().isPresent() ? modelSpec.getLastBuilt().get() : null;
        return new Manifest(modelSpec.getSymbolicName(), modelSpec.getVersionRange(), timestamp);
    }

    private LibraryIdentifier findOrCreateLibraryIdentifier(final DependencyInformation dependencyInfo) {
        ensureIsNotNull(dependencyInfo);
        ensureIsNotEmpty(dependencyInfo.jarFileFingerprint,
                "ClasspathDependencyInformation must at least contain a fingerprint.");

        LibraryIdentifier libraryIdentifier = dataAccess
                .getLibraryIdentifierForFingerprint(dependencyInfo.jarFileFingerprint);
        if (libraryIdentifier == null) {
            libraryIdentifier = createDefaultLibraryIdentifier(dependencyInfo);
            if (libraryIdentifier == null) {
                libraryIdentifier = LibraryIdentifier.UNKNOWN;
            }
        }

        return libraryIdentifier;
    }

    private LibraryIdentifier createDefaultLibraryIdentifier(final DependencyInformation dependencyInfo) {
        final String symbolicName = dependencyInfo.symbolicName;
        if (symbolicName != null && !symbolicName.isEmpty()) {
            final Version version = dependencyInfo.version == null ? Version.UNKNOWN : dependencyInfo.version;
            final LibraryIdentifier libraryIdentifier = new LibraryIdentifier(symbolicName, version,
                    dependencyInfo.jarFileFingerprint);
            dataAccess.save(libraryIdentifier);
            return libraryIdentifier;
        }
        return null;
    }
}