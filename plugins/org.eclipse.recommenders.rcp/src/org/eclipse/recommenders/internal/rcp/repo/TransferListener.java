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

import static java.lang.String.format;
import static org.apache.commons.io.FileUtils.byteCountToDisplaySize;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.recommenders.rcp.l10n.Messages;
import org.sonatype.aether.transfer.AbstractTransferListener;
import org.sonatype.aether.transfer.TransferEvent;
import org.sonatype.aether.transfer.TransferResource;

public class TransferListener extends AbstractTransferListener {

    private final IProgressMonitor monitor;
    private Map<TransferResource, Long> downloads = new ConcurrentHashMap<TransferResource, Long>();

    public TransferListener(IProgressMonitor monitor) {
        this.monitor = monitor;
    }

    @Override
    public void transferInitiated(TransferEvent event) {
        String resourceName = event.getResource().getRepositoryUrl() + event.getResource().getResourceName();
        if (event.getRequestType() == TransferEvent.RequestType.PUT) {
            monitor.subTask(format(Messages.TASK_UPLOADING, resourceName));
        } else {
            monitor.subTask(format(Messages.TASK_DOWNLOADING, resourceName));
        }
    }

    @Override
    public void transferStarted(TransferEvent event) throws org.sonatype.aether.transfer.TransferCancelledException {
    };

    @Override
    public void transferProgressed(TransferEvent event) {
        TransferResource resource = event.getResource();
        downloads.put(resource, Long.valueOf(event.getTransferredBytes()));
        for (Map.Entry<TransferResource, Long> entry : downloads.entrySet()) {
            long total = entry.getKey().getContentLength();
            long complete = entry.getValue().longValue();
            monitor.subTask(getStatus(complete, total));
        }
    }

    @Override
    public void transferSucceeded(TransferEvent event) {
        TransferResource resource = event.getResource();
        downloads.remove(resource);
        monitor.subTask(String.format(Messages.STATUS_DOWNLOAD_FINISHED, resource.getResourceName()));
    }

    private String getStatus(long complete, long total) {
        String status = byteCountToDisplaySize(complete);
        if (total > 0)
            status += "/" + byteCountToDisplaySize(total); //$NON-NLS-1$
        return status;
    }

    @Override
    public void transferFailed(TransferEvent event) {
        monitor.subTask(String.format(Messages.STATUS_DOWNLOAD_FAILED, event.getException().getLocalizedMessage()));
    }

    @Override
    public void transferCorrupted(TransferEvent event) {
        monitor.subTask(String.format(Messages.STATUS_DOWNLOAD_CORRUPTED, event.getException().getLocalizedMessage()));
    }
}
