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
package org.eclipse.recommenders.internal.rcp.repo;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.recommenders.rcp.l10n.Messages;
import org.eclipse.recommenders.rcp.repo.IModelRepository;

/**
 * Deletes all ZIP archives from the model repository or - if a file is currently in use - marks it for deletion on next
 * shutdown.
 */
public class ClearModelRepositoryJob extends Job {

    private IModelRepository repo;

    public ClearModelRepositoryJob(IModelRepository repo) {
        super(Messages.JOB_CLEAR_MODEL_REPOSITORY);
        this.repo = repo;
        setPriority(Job.LONG);
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        monitor.beginTask("", IProgressMonitor.UNKNOWN); //$NON-NLS-1$
        File location = repo.getLocation();
        try {
            FileUtils.deleteQuietly(location);
            FileUtils.forceDeleteOnExit(location);
        } catch (IOException e) {
        }
        monitor.done();
        return Status.OK_STATUS;
    }
}
