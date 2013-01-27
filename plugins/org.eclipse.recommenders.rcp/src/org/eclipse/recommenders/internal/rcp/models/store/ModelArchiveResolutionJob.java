/**
 * Copyright (c) 2010, 2012 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 *    Olav Lenz - externalize Strings.
 */
package org.eclipse.recommenders.internal.rcp.models.store;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.eclipse.core.runtime.Status.CANCEL_STATUS;
import static org.eclipse.core.runtime.Status.OK_STATUS;
import static org.eclipse.recommenders.internal.rcp.models.ModelArchiveMetadata.ModelArchiveResolutionStatus.RESOLVED;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.recommenders.internal.rcp.models.ModelArchiveMetadata;
import org.eclipse.recommenders.internal.rcp.models.ModelArchiveMetadata.ModelArchiveResolutionStatus;
import org.eclipse.recommenders.rcp.ClasspathEntryInfo;
import org.eclipse.recommenders.rcp.IClasspathEntryInfoProvider;
import org.eclipse.recommenders.rcp.RecommendersPlugin;
import org.eclipse.recommenders.rcp.l10n.Messages;
import org.eclipse.recommenders.rcp.repo.IModelRepository;
import org.eclipse.recommenders.rcp.repo.IModelRepositoryIndex;
import org.eclipse.recommenders.utils.Version;
import org.sonatype.aether.artifact.Artifact;

import com.google.common.annotations.VisibleForTesting;
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
    private Artifact model;

    @Inject
    public ModelArchiveResolutionJob(@Assisted ModelArchiveMetadata metadata,
            IClasspathEntryInfoProvider cpeInfoProvider, IModelRepository repository, IModelRepositoryIndex index,
            @Assisted String classifier) {
        super(String.format(Messages.JOB_RESOLVING_MODEL, metadata.getLocation().getName()));
        this.metadata = metadata;
        this.cpeInfos = cpeInfoProvider;
        this.repository = repository;
        this.index = index;
        this.classifier = classifier;
    }

    @Override
    @VisibleForTesting
    public IStatus run(IProgressMonitor monitor) {
        if (!isAutoDownloadAllowed()) {
            return Status.CANCEL_STATUS;
        }
        monitor.beginTask(String.format(Messages.TASK_LOOKING_FOR_MODEL, classifier, metadata.getLocation().getName()),
                5);
        monitor.worked(1);
        metadata.setStatus(ModelArchiveResolutionStatus.UNRESOLVED);
        try {

            if (!findClasspathInfo()) {
                metadata.setError(format("No class path info available for '%s'. Skipped.", pkgRoot)); //$NON-NLS-1$
                // we didn't found it yet. The system is currently starting and indexing. Try again later.
                metadata.setResolutionRequestedSinceStartup(false);
                return CANCEL_STATUS;
            }

            if (!findInIndex()) {
                metadata.setError(format(
                        "No call model found for '%1$s'. Neither fingerprint '%2$s' nor symbolic name '%3$s' are known.", //$NON-NLS-1$
                        cpeInfo.getLocation(), cpeInfo.getFingerprint(), cpeInfo.getSymbolicName()));
                return CANCEL_STATUS;
            }
            monitor.worked(1);
            findBestMatchingLatestModel();
            monitor.worked(1);
            repository.resolve(model, monitor);
            monitor.worked(2);
            updateMetadata();
            return OK_STATUS;
        } catch (Exception x) {
            metadata.setStatus(ModelArchiveResolutionStatus.UNRESOLVED);
            metadata.setError(x.getMessage());
            return CANCEL_STATUS;
        } finally {
            monitor.done();
        }
    }

    @VisibleForTesting
    protected boolean isAutoDownloadAllowed() {
        IPreferenceStore store = RecommendersPlugin.getDefault().getPreferenceStore();
        return store.getBoolean(RecommendersPlugin.P_REPOSITORY_ENABLE_AUTO_DOWNLOAD);
    }

    private void updateMetadata() {
        metadata.setStatus(RESOLVED);
        metadata.setArtifact(model);
        metadata.setCoordinate(model.toString());
    }

    private boolean findClasspathInfo() {
        pkgRoot = metadata.getLocation();
        cpeInfo = cpeInfos.getInfo(pkgRoot).orNull();
        return cpeInfo != null;
    }

    private boolean findInIndex() {
        Optional<Artifact> tmp = Optional.absent();
        if (!isEmpty(cpeInfo.getFingerprint())) {
            tmp = index.searchByFingerprint(cpeInfo.getFingerprint(), classifier);
        }
        if (!tmp.isPresent() && !isEmpty(cpeInfo.getSymbolicName())) {
            tmp = index.searchByArtifactId(cpeInfo.getSymbolicName(), classifier);
        }
        model = tmp.orNull();
        return tmp.isPresent();
    }

    private void findBestMatchingLatestModel() {
        Version version = cpeInfo.getVersion();
        Artifact copy = model;
        Artifact query = null;
        String upperBound = version.isUnknown() ? "10000.0" : format("%d.%d", version.major, version.minor + 1); //$NON-NLS-1$ //$NON-NLS-2$
        query = model.setVersion("[0," + upperBound + ")"); //$NON-NLS-1$ //$NON-NLS-2$
        copy = repository.findHigestVersion(query).orNull();
        if (copy == null) {
            query = model.setVersion("[" + upperBound + ",)"); //$NON-NLS-1$ //$NON-NLS-2$
            copy = repository.findLowestVersion(query).orNull();
        }
        if (copy != null) {
            model = copy;
        }
    }
}
