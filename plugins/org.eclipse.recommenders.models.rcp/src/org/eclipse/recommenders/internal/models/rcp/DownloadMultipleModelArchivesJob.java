/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.models.rcp;

import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.recommenders.models.IModelRepository;
import org.eclipse.recommenders.models.ModelCoordinate;

import com.google.common.eventbus.EventBus;

public class DownloadMultipleModelArchivesJob extends Job {

    private final IModelRepository repository;
    private final Collection<ModelCoordinate> coordinates;
    private final boolean forceDownloads;
    private EventBus bus;

    public DownloadMultipleModelArchivesJob(IModelRepository repository, Collection<ModelCoordinate> coordinates,
            boolean forceDownloads, EventBus bus) {
        super("Downloading model coordinates");
        this.repository = repository;
        this.coordinates = coordinates;
        this.forceDownloads = forceDownloads;
        this.bus = bus;
    }

    @Override
    protected IStatus run(final IProgressMonitor monitor) {
        MultiStatus report = new MultiStatus(Constants.BUNDLE_ID, 0, "Download Model Coordinates Report", null);
        monitor.beginTask("", coordinates.size());
        for (ModelCoordinate mc : coordinates) {
            monitor.subTask("Downloading " + mc);
            IStatus subtask = new DownloadModelArchiveJob(repository, mc, forceDownloads, bus)
                    .run(new SubProgressMonitor(monitor, 1));
            report.add(subtask);
        }
        monitor.done();
        return report;
    }
}
