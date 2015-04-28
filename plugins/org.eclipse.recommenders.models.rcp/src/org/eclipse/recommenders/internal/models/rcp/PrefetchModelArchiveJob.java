/**
 * Copyright (c) 2015 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.models.rcp;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IType;
import org.eclipse.recommenders.models.IModelProvider;
import org.eclipse.recommenders.models.UniqueTypeName;
import org.eclipse.recommenders.models.rcp.IProjectCoordinateProvider;

public class PrefetchModelArchiveJob<M> extends Job {

    private IType receiverType;
    private IProjectCoordinateProvider pcProvider;
    private IModelProvider<UniqueTypeName, M> modelProvider;

    public PrefetchModelArchiveJob(IType type, IProjectCoordinateProvider pcProvider,
            IModelProvider<UniqueTypeName, M> modelProvider) {
        super("Prefetching Model Archive");
        this.receiverType = type;
        this.pcProvider = pcProvider;
        this.modelProvider = modelProvider;
        setSystem(true);
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        monitor.beginTask("Resolving coordinates", IProgressMonitor.UNKNOWN);
        try {
            UniqueTypeName name = pcProvider.toUniqueName(receiverType).orNull();
            M model = modelProvider.acquireModel(name).orNull();
            modelProvider.releaseModel(model);
        } catch (Exception e) {
            // ignore
        } finally {
            monitor.done();
        }
        return Status.OK_STATUS;
    }
}
