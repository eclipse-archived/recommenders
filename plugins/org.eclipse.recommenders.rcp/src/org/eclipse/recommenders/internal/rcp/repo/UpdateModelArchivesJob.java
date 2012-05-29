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
package org.eclipse.recommenders.internal.rcp.repo;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.recommenders.rcp.repo.IModelRepository;

public class UpdateModelArchivesJob extends Job {

    private IModelRepository repo;

    public UpdateModelArchivesJob(IModelRepository repo) {
        super("Update model archives");
        this.repo = repo;
        setSystem(true);
        setPriority(Job.LONG);
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {

        return Status.OK_STATUS;
    }

}
