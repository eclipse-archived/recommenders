/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.completion.rcp.calls.store2;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.eclipse.core.runtime.Status.CANCEL_STATUS;
import static org.eclipse.core.runtime.Status.OK_STATUS;
import static org.eclipse.recommenders.internal.completion.rcp.calls.store2.CallModelResolutionData.ModelResolutionStatus.FAILED;
import static org.eclipse.recommenders.internal.completion.rcp.calls.store2.CallModelResolutionData.ModelResolutionStatus.RESOLVED;
import static org.eclipse.recommenders.internal.rcp.repo.RepositoryUtils.asCoordinate;

import java.io.File;

import javax.inject.Inject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.recommenders.internal.rcp.repo.RepositoryUtils;
import org.eclipse.recommenders.rcp.ClasspathEntryInfo;
import org.eclipse.recommenders.rcp.IClasspathEntryInfoProvider;
import org.eclipse.recommenders.rcp.repo.IModelRepository;
import org.eclipse.recommenders.rcp.repo.IModelRepositoryIndex;
import org.eclipse.recommenders.utils.Version;
import org.sonatype.aether.artifact.Artifact;

import com.google.common.base.Optional;
import com.google.inject.assistedinject.Assisted;

public class CallModelDownloadJob extends Job {

    public static interface JobFactory {
        CallModelDownloadJob create(File file);
    }

    private ClasspathEntryInfo cpeInfo;
    private final CallModelStore store;
    private final IModelRepository repo;
    private final IModelRepositoryIndex index;
    private final CallModelResolutionData mapping = new CallModelResolutionData();
    private final IClasspathEntryInfoProvider cpeInfoProvider;
    private final File file;

    @Inject
    public CallModelDownloadJob(@Assisted File file, CallModelStore store, IModelRepository repo,
            IModelRepositoryIndex index, IClasspathEntryInfoProvider cpeInfoProvider) {
        super("Downloading calls model...");
        this.file = file;
        this.cpeInfoProvider = cpeInfoProvider;
        this.store = store;
        this.repo = repo;
        this.index = index;
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        monitor.beginTask("Model requested for " + file.getName(), 4);
        monitor.worked(1);
        mapping.setStatus(FAILED);
        try {
            monitor.subTask("Looking up available models in index...");
            cpeInfo = cpeInfoProvider.getInfo(file).orNull();
            if (cpeInfo == null) {
                mapping.setError(format("No class path info available for '%s'. Skipped.", file));
                return CANCEL_STATUS;
            }

            if (isEmpty(cpeInfo.getFingerprint())) {
                mapping.setError(format("Fingerprint for '%s' was null.", cpeInfo.getLocation()));
                return CANCEL_STATUS;
            }

            Optional<Artifact> handle = index.searchByFingerprint(cpeInfo.getFingerprint(), "cr-calls");
            if (!handle.isPresent() && !isEmpty(cpeInfo.getSymbolicName())) {
                handle = index.searchByArtifactId(cpeInfo.getSymbolicName(), "cr-calls");
            }
            if (!handle.isPresent()) {
                mapping.setError(format(
                        "No call model found for '%s'. Neither fingerprint '%s' nor symbolic name '%s' are known.",
                        cpeInfo.getLocation(), cpeInfo.getFingerprint(), cpeInfo.getSymbolicName()));
                return CANCEL_STATUS;
            }

            Optional<Artifact> bestMatch = findBestMatch(handle.get(), cpeInfo.getVersion());
            if (!bestMatch.isPresent()) {
                mapping.setError(format("No best matching model found for '%s'. This is probably a bug.",
                        cpeInfo.getLocation()));
                return CANCEL_STATUS;
            }

            monitor.worked(1);
            repo.resolve(bestMatch.get(), monitor);
            monitor.worked(2);
            File local = repo.location(bestMatch.get());
            if (!local.exists()) {
                mapping.setError(format("Failed to download and install model artifact '%s' to local repostiory",
                        asCoordinate(bestMatch.get())));
                return CANCEL_STATUS;
            }
            mapping.setStatus(RESOLVED);
            mapping.setCoordinate(RepositoryUtils.asCoordinate(bestMatch.get()));
            return OK_STATUS;

        } catch (Exception x) {
            mapping.setStatus(FAILED);
            mapping.setError(x.getMessage());
            return CANCEL_STATUS;
        } finally {
            store.getMappings().put(file, mapping);
            monitor.done();
        }
    }

    private Optional<Artifact> findBestMatch(Artifact artifact, Version version) {
        Artifact query = null;
        if (version.isUnknown()) {
            query = artifact.setVersion("([0,");
            return repo.findHigestVersion(query);
        }

        String upperBound = Version.create(version.major, version.minor + 1).toString();
        query = artifact.setVersion("[0," + upperBound + ")");
        Optional<Artifact> match = repo.findHigestVersion(query);
        if (match.isPresent()) {
            return match;
        }

        query = artifact.setVersion("[" + upperBound + ",");
        match = repo.findLowestVersion(query);
        return match;
    }
}
