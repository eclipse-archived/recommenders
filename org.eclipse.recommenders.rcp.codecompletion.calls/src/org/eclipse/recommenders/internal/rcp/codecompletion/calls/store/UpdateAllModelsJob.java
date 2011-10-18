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

import java.io.File;
import java.util.Set;

import javax.inject.Inject;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.store.ResolveCallsModelJob.OverridePolicy;

public class UpdateAllModelsJob extends WorkspaceJob {

    private final ClasspathDependencyStore dependencyStore;
    private final RemoteResolverJobFactory jobFactory;

    @Inject
    public UpdateAllModelsJob(final ClasspathDependencyStore dependencyStore, final RemoteResolverJobFactory jobFactory) {
        super("Updating all dependencies");
        this.dependencyStore = dependencyStore;
        this.jobFactory = jobFactory;
        setSystem(true);
    }

    @Override
    public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
        final Set<File> files = dependencyStore.getFiles();
        for (final File file : files) {
            if (dependencyStore.containsManifest(file)
                    && dependencyStore.getManifestResolvementInfo(file).isResolvedManual()) {
                continue;
            }

            final ResolveCallsModelJob job = jobFactory.create(file, OverridePolicy.ALL);
            job.schedule();
        }
        return Status.OK_STATUS;
    }

}
