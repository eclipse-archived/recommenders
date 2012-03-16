/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.repo;

import static java.lang.String.format;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.runtime.IProgressMonitor;
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
        String mode = event.getRequestType() == TransferEvent.RequestType.PUT ? "Uploading" : "Downloading";
        String resourceName = event.getResource().getRepositoryUrl() + event.getResource().getResourceName();
        monitor.subTask(format("%s: %s", mode, resourceName));
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
        monitor.subTask("Finished transfer: " + resource.getResourceName());
    }

    private String getStatus(long complete, long total) {
        if (total >= 1024) {
            return toKB(complete) + "/" + toKB(total) + " KB ";
        } else if (total >= 0) {
            return complete + "/" + total + " B ";
        } else if (complete >= 1024) {
            return toKB(complete) + " KB ";
        } else {
            return complete + " B ";
        }
    }

    @Override
    public void transferFailed(TransferEvent event) {
        monitor.subTask("Transfer failed: " + event);
    }

    @Override
    public void transferCorrupted(TransferEvent event) {
        monitor.subTask("Transfer corrupted: " + event.getException().getLocalizedMessage());
    }

    private long toKB(long bytes) {
        return (bytes + 1023) / 1024;
    }
}