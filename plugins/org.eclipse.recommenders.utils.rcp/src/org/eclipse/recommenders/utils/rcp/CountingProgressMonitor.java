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
package org.eclipse.recommenders.utils.rcp;

import org.eclipse.core.runtime.IProgressMonitor;

public class CountingProgressMonitor implements IProgressMonitor {

    private final IProgressMonitor monitor;
    public int plannedWork = 0;
    public int actualWork = 0;

    public CountingProgressMonitor(final IProgressMonitor monitor) {
        this.monitor = monitor;
    }

    @Override
    public void beginTask(final String name, final int plannedTotalWork) {
        monitor.beginTask(name, plannedTotalWork);
        plannedWork = plannedTotalWork;
    }

    @Override
    public void done() {
        monitor.done();
    }

    @Override
    public void internalWorked(final double work) {
        monitor.internalWorked(work);
    }

    @Override
    public boolean isCanceled() {
        return monitor.isCanceled();
    }

    @Override
    public void setCanceled(final boolean value) {
        monitor.setCanceled(value);
    }

    @Override
    public void setTaskName(final String name) {
        monitor.setTaskName(name);
    }

    @Override
    public void subTask(final String name) {
        monitor.subTask(name);
    }

    @Override
    public void worked(final int work) {
        actualWork += work;
        monitor.worked(work);
    }

    public void worked() {
        worked(1);
    }
}
