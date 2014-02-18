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

import java.io.File;
import java.text.MessageFormat;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.recommenders.models.DownloadCallback;
import org.eclipse.recommenders.models.IModelRepository;
import org.eclipse.recommenders.models.ModelCoordinate;
import org.eclipse.recommenders.models.rcp.ModelEvents.ModelArchiveDownloadedEvent;
import org.eclipse.recommenders.rcp.utils.Logs;
import org.eclipse.ui.statushandlers.StatusManager;

import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;

public class DownloadModelArchiveJob extends Job {

    private final Map<String, IProgressMonitor> downloads = Maps.newHashMap();

    private final IModelRepository repository;
    private final ModelCoordinate mc;
    private final boolean forceDownload;
    private final EventBus bus;

    public DownloadModelArchiveJob(IModelRepository repository, ModelCoordinate mc, boolean forceDownload, EventBus bus) {
        super(MessageFormat.format(Messages.JOB_RESOLVING_MODEL, mc));
        this.repository = repository;
        this.mc = mc;
        this.forceDownload = forceDownload;
        this.bus = bus;
    }

    @Override
    protected IStatus run(final IProgressMonitor monitor) {
        try {
            monitor.beginTask(MessageFormat.format(Messages.TASK_RESOLVING_MODEL, mc), IProgressMonitor.UNKNOWN);
            ModelArchiveDownloadCallback cb = new ModelArchiveDownloadCallback(monitor);
            File result = repository.resolve(mc, forceDownload, cb).orNull();
            if (cb.downloadedArchive) {
                bus.post(new ModelArchiveDownloadedEvent(mc));
            }
            // Returns null if the model coordiante could not be resolved. This may because we are requesting an mc that
            // does not exist in the repository or because of the network being down.
            // Moreover, we can get *cached* null answers, i.e., the same negative result over and over again.
            if (result == null) {
                // Whatever is the case, we only should log that as informational but do not open a popup.
                IStatus err = Logs.newStatus(IStatus.INFO, null, Constants.BUNDLE_ID,
                        Messages.LOG_INFO_NO_MODEL_RESOLVED, mc, repository);
                StatusManager.getManager().handle(err, StatusManager.LOG);
                return Status.CANCEL_STATUS;
            }
        } catch (Exception e) {
            return Logs.newStatus(IStatus.ERROR, e, Constants.BUNDLE_ID, Messages.LOG_ERROR_MODEL_RESOLUTION_FAILURE,
                    mc);
        } finally {
            monitor.done();
        }
        return OK_STATUS;
    }

    private final class ModelArchiveDownloadCallback extends DownloadCallback {
        private final IProgressMonitor monitor;
        private boolean downloadedArchive;

        private ModelArchiveDownloadCallback(IProgressMonitor monitor) {
            this.monitor = monitor;
        }

        @Override
        public synchronized void downloadInitiated(String path) {
            downloads.put(path, new SubProgressMonitor(monitor, 1));
        }

        @Override
        public synchronized void downloadProgressed(String path, long transferred, long total) {
            IProgressMonitor submonitor = downloads.get(path);
            String message = bytesToString(transferred) + "/" + bytesToString(total); //$NON-NLS-1$
            submonitor.subTask(message);
            submonitor.worked(1);
        }

        @Override
        public synchronized void downloadSucceeded(String path) {
            downloads.get(path).done();
            downloadedArchive = true;
        }

        @Override
        public synchronized void downloadFailed(String path) {
            downloads.get(path).done();
        }

        private String bytesToString(long bytes) {
            if (bytes < 1024) {
                return bytes + " B"; //$NON-NLS-1$
            }
            int exp = (int) (Math.log(bytes) / Math.log(1024));
            String pre = "KMGTPE".charAt(exp - 1) + "i"; //$NON-NLS-1$ //$NON-NLS-2$
            return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre); //$NON-NLS-1$
        }
    }
}
