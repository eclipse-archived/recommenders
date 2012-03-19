/**
 * Copyright (c) 2010, 2012 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.models.store;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.eclipse.core.runtime.Status.CANCEL_STATUS;
import static org.eclipse.core.runtime.Status.OK_STATUS;
import static org.eclipse.recommenders.internal.rcp.models.ModelArchiveMetadata.ModelArchiveResolutionStatus.FAILED;
import static org.eclipse.recommenders.internal.rcp.models.ModelArchiveMetadata.ModelArchiveResolutionStatus.RESOLVED;
import static org.eclipse.recommenders.internal.rcp.repo.RepositoryUtils.asCoordinate;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.recommenders.internal.rcp.models.ModelArchiveMetadata;
import org.eclipse.recommenders.internal.rcp.models.ModelArchiveMetadata.ModelArchiveResolutionStatus;
import org.eclipse.recommenders.rcp.ClasspathEntryInfo;
import org.eclipse.recommenders.rcp.IClasspathEntryInfoProvider;
import org.eclipse.recommenders.rcp.repo.IModelRepository;
import org.eclipse.recommenders.rcp.repo.IModelRepositoryIndex;
import org.eclipse.recommenders.utils.Version;
import org.sonatype.aether.artifact.Artifact;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

@SuppressWarnings("rawtypes")
public class ModelArchiveResolutionJob extends Job {

    private final ModelArchiveMetadata metadata;
    private final IClasspathEntryInfoProvider cpeInfos;
    private final IModelRepository repository;

    private ClasspathEntryInfo cpeInfo;
    private File pkgRoot;
    private final IModelRepositoryIndex index;
    private final String classifier;

    @Inject
    public ModelArchiveResolutionJob(@Assisted ModelArchiveMetadata metadata,
            IClasspathEntryInfoProvider cpeInfoProvider, IModelRepository repository, IModelRepositoryIndex index,
            @Assisted String classifier) {
        super("Resolving model for " + metadata.getLocation().getName() + "...");
        this.metadata = metadata;
        this.cpeInfos = cpeInfoProvider;
        this.repository = repository;
        this.index = index;
        this.classifier = classifier;
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {

        monitor.beginTask("Model requested for " + metadata.getLocation().getName(), 4);
        monitor.worked(1);
        metadata.setStatus(ModelArchiveResolutionStatus.FAILED);
        try {
            monitor.subTask("Looking up available models in index...");
            pkgRoot = metadata.getLocation();
            cpeInfo = cpeInfos.getInfo(pkgRoot).orNull();
            if (cpeInfo == null) {
                metadata.setError(format("No class path info available for '%s'. Skipped.", pkgRoot));
                return CANCEL_STATUS;
            }

            if (isEmpty(cpeInfo.getFingerprint())) {
                metadata.setError(format("Fingerprint for '%s' was null.", cpeInfo.getLocation()));
                return CANCEL_STATUS;
            }

            Optional<Artifact> handle = index.searchByFingerprint(cpeInfo.getFingerprint(), classifier);
            if (!handle.isPresent() && !isEmpty(cpeInfo.getSymbolicName())) {
                handle = index.searchByArtifactId(cpeInfo.getSymbolicName(), classifier);
            }
            if (!handle.isPresent()) {
                metadata.setError(format(
                        "No call model found for '%s'. Neither fingerprint '%s' nor symbolic name '%s' are known.",
                        cpeInfo.getLocation(), cpeInfo.getFingerprint(), cpeInfo.getSymbolicName()));
                return CANCEL_STATUS;
            }

            Optional<Artifact> bestMatch = findBestMatch(handle.get(), cpeInfo.getVersion());
            if (!bestMatch.isPresent()) {
                metadata.setError(format("No best matching model found for '%s'. This is probably a bug.",
                        cpeInfo.getLocation()));
                return CANCEL_STATUS;
            }

            monitor.worked(1);
            repository.resolve(bestMatch.get(), monitor);
            monitor.worked(2);
            File local = repository.location(bestMatch.get());
            if (!local.exists()) {
                metadata.setError(format("Failed to download and install model artifact '%s' to local repostiory",
                        asCoordinate(bestMatch.get())));
                return CANCEL_STATUS;
            }
            metadata.setStatus(RESOLVED);
            metadata.setArtifact(bestMatch.get());
            metadata.setCoordinate(bestMatch.get().toString());
            return OK_STATUS;

        } catch (Exception x) {
            metadata.setStatus(FAILED);
            metadata.setError(x.getMessage());
            return CANCEL_STATUS;
        } finally {
            monitor.done();
        }
    }

    private Optional<Artifact> findBestMatch(Artifact artifact, Version version) {
        Artifact query = null;
        if (version.isUnknown()) {
            query = artifact.setVersion("([0,");
            return repository.findHigestVersion(query);
        }

        String upperBound = Version.create(version.major, version.minor + 1).toString();
        query = artifact.setVersion("[0," + upperBound + ")");
        Optional<Artifact> match = repository.findHigestVersion(query);
        if (match.isPresent()) {
            return match;
        }

        query = artifact.setVersion("[" + upperBound + ",)");
        match = repository.findLowestVersion(query);
        return match;
    }
}