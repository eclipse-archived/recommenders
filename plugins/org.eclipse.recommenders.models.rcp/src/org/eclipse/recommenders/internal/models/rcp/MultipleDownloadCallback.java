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

import java.text.MessageFormat;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.recommenders.models.DownloadCallback;

import com.google.common.collect.Maps;

/**
 * Callback to indicate progress on multiple sequential downloads. The callback works for a given number of downloads
 * and will split the monitored work on each of them. If less downloads are executed than expected, the callback can
 * finish the work to indicate a proper progress. Though it is recommended to call {@link #finish()} after all work is
 * done in any case.
 */
public class MultipleDownloadCallback extends DownloadCallback {

    private final Map<String, IProgressMonitor> downloads = Maps.newHashMap();
    private boolean downloadSucceeded;
    private long lastTransferred;
    private int finishedWorkUnits;

    private final IProgressMonitor monitor;
    private final int maximumNumberOfDownloads;
    private final int workUnitsPerDownloadTask;
    private int workUnitsRemainder;

    public MultipleDownloadCallback(IProgressMonitor monitor, String message, int totalWorkUnits,
            int maximumNumberOfDownloads) {
        this.monitor = monitor;
        this.maximumNumberOfDownloads = maximumNumberOfDownloads;
        workUnitsPerDownloadTask = totalWorkUnits / maximumNumberOfDownloads;
        workUnitsRemainder = totalWorkUnits % maximumNumberOfDownloads;
        monitor.beginTask(message, totalWorkUnits);
    }

    @Override
    public synchronized void downloadInitiated(String path) {
        int workUnits = workUnitsPerDownloadTask;
        if (workUnitsRemainder >= 1) {
            workUnits++;
        }
        SubProgressMonitor subProgressMonitor = new SubProgressMonitor(monitor, workUnits);
        subProgressMonitor.beginTask(path, workUnits);
        downloads.put(path, subProgressMonitor);
        lastTransferred = 0;
        finishedWorkUnits = 0;
    }

    @Override
    public synchronized void downloadProgressed(String path, long transferred, long total) {
        IProgressMonitor submonitor = downloads.get(path);
        final String message;
        // If no total size is known, total might be -1.
        if (total >= transferred) {
            handleMonitorWorkForDownloadProgress(transferred, total, submonitor);
            message = createProgressMessage(transferred, total);
        } else {
            message = createProgressMessage(transferred);
        }
        submonitor.subTask(message);
    }

    private void handleMonitorWorkForDownloadProgress(long transferred, long total, IProgressMonitor submonitor) {
        long newTransferred = transferred - lastTransferred;
        lastTransferred = transferred;
        int workUnits = calculateWorkUnitsForDownloadProgress(newTransferred, total);
        finishedWorkUnits += workUnits;
        submonitor.worked(workUnits);
    }

    private String createProgressMessage(long transferred) {
        return MessageFormat.format(Messages.JOB_DOWNLOAD_TRANSFERRED_SIZE,
                FileUtils.byteCountToDisplaySize(transferred));
    }

    private String createProgressMessage(long transferred, long total) {
        return MessageFormat.format(Messages.JOB_DOWNLOAD_TRANSFERRED_TOTAL_SIZE,
                FileUtils.byteCountToDisplaySize(transferred), FileUtils.byteCountToDisplaySize(total));
    }

    private int calculateWorkUnitsForDownloadProgress(long newTransferred, long total) {
        double amount = (double) newTransferred / total;
        int workUnits = (int) (workUnitsPerDownloadTask * amount);
        return workUnits;
    }

    @Override
    public synchronized void downloadSucceeded(String path) {
        IProgressMonitor submonitor = downloads.get(path);
        finishMonitorWork(submonitor);
        submonitor.done();
        downloadSucceeded = true;
    }

    @Override
    public synchronized void downloadFailed(String path) {
        IProgressMonitor submonitor = downloads.get(path);
        finishMonitorWork(submonitor);
        submonitor.done();
    }

    private void finishMonitorWork(IProgressMonitor submonitor) {
        workUnfinishedTaskUnits(submonitor);
        if (workUnitsRemainder > 0) {
            workOneRemainderUnit(submonitor);
        }
    }

    private void workUnfinishedTaskUnits(IProgressMonitor submonitor) {
        int unfinishedWorkUnits = workUnitsPerDownloadTask - finishedWorkUnits;
        if (unfinishedWorkUnits > 0) {
            submonitor.worked(unfinishedWorkUnits);
        }
    }

    private void workOneRemainderUnit(IProgressMonitor monitor) {
        monitor.worked(1);
        workUnitsRemainder--;
    }

    public boolean isDownloadSucceeded() {
        return downloadSucceeded;
    }

    public void finish() {
        finishWorkForSkippedTasks();
        finishWorkForRemainder();
    }

    private void finishWorkForSkippedTasks() {
        int skippedDownloadTasks = maximumNumberOfDownloads - downloads.size();
        for (int i = 0; i < skippedDownloadTasks; i++) {
            monitor.worked(workUnitsPerDownloadTask);
        }
    }

    private void finishWorkForRemainder() {
        if (workUnitsRemainder > 0) {
            monitor.worked(workUnitsRemainder);
        }
    }
}
