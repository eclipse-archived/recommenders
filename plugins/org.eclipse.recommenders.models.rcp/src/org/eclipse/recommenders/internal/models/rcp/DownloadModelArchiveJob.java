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

import static org.eclipse.core.runtime.Status.OK_STATUS;
import static org.eclipse.recommenders.internal.models.rcp.Constants.BUNDLE_ID;
import static org.eclipse.recommenders.internal.models.rcp.Messages.*;
import static org.eclipse.ui.internal.misc.StatusUtil.newStatus;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.recommenders.models.ModelRepository.DownloadCallback;
import org.eclipse.recommenders.models.IModelRepository;
import org.eclipse.recommenders.models.ModelCoordinate;

@SuppressWarnings("restriction")
public class DownloadModelArchiveJob extends Job {

    private IModelRepository repository;
    private ModelCoordinate mc;

    public DownloadModelArchiveJob(IModelRepository repository, ModelCoordinate mc) {
        super(String.format(JOB_RESOLVING_MODEL, mc));
        this.repository = repository;
        this.mc = mc;
    }

    @Override
    protected IStatus run(final IProgressMonitor monitor) {
        try {
            repository.resolve(mc, new DownloadCallback() {
                @Override
                public void downloadStarted() {
                    monitor.beginTask(TASK_RESOLVING, IProgressMonitor.UNKNOWN);
                }

                @Override
                public void downloadProgressed(long transferred, long total) {
                    String message = bytesToString(transferred) + "/" + bytesToString(total);
                    monitor.subTask(message);
                    monitor.worked(1);
                }
            }).get();
        } catch (Exception e) {
            return newStatus(BUNDLE_ID, "failed to download " + mc, e);
        } finally {
            monitor.done();
        }
        return OK_STATUS;
    }

    private static String bytesToString(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "i";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
}
