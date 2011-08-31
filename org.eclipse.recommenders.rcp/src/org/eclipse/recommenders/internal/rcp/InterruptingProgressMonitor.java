/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.recommenders.rcp.utils.CountingProgressMonitor;

public class InterruptingProgressMonitor implements IProgressMonitor {

    private static ScheduledExecutorService executor = Executors.newScheduledThreadPool(4);
    private final CountingProgressMonitor reporting_only_monitor;
    private boolean done;
    private Thread currentThread;

    @Override
    public void beginTask(final String name, final int plannedTotalWork) {
        currentThread = Thread.currentThread();
        reporting_only_monitor.beginTask(name, plannedTotalWork);
        executor.schedule(new Runnable() {

            @Override
            public void run() {

                if (!InterruptingProgressMonitor.this.done) {
                    InterruptingProgressMonitor.this.setCanceled(true);
                }
            }
        }, 2, TimeUnit.SECONDS);

    }

    @Override
    public void done() {
        this.done = true;
        // dont call done on enclosing monitor!
        // clear flag
        Thread.interrupted();
    }

    @Override
    public void internalWorked(final double work) {
        reporting_only_monitor.internalWorked(work);
    }

    @Override
    public boolean isCanceled() {
        return reporting_only_monitor.isCanceled() || Thread.currentThread().isInterrupted();
    }

    @Override
    public void setCanceled(final boolean value) {
        if (value) {
            currentThread.interrupt();
        }
    }

    @Override
    public void setTaskName(final String name) {
        reporting_only_monitor.setTaskName(name);
    }

    @Override
    public void subTask(final String name) {
        reporting_only_monitor.subTask(name);
    }

    @Override
    public void worked(final int work) {
        reporting_only_monitor.worked(work);
    }

    public void worked() {
        reporting_only_monitor.worked();
    }

    @Override
    public int hashCode() {
        return reporting_only_monitor.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return reporting_only_monitor.equals(obj);
    }

    @Override
    public String toString() {
        return reporting_only_monitor.toString();
    }

    public InterruptingProgressMonitor(final CountingProgressMonitor monitor) {
        this.reporting_only_monitor = monitor;
    }

}
