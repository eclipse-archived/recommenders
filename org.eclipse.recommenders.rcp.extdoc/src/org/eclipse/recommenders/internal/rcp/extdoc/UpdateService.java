/**
 * Copyright (c) 2011 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.extdoc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.recommenders.rcp.extdoc.ExtDocPlugin;
import org.eclipse.recommenders.rcp.extdoc.SwtFactory;
import org.eclipse.recommenders.rcp.extdoc.UiUtils;
import org.eclipse.swt.widgets.Composite;

public final class UpdateService {

    private final ExecutorService pool = Executors.newCachedThreadPool();
    private final Map<IUpdateJob, Callable<IUpdateJob>> jobs = new HashMap<IUpdateJob, Callable<IUpdateJob>>();

    public void schedule(final IUpdateJob job) {
        jobs.put(job, new Callable<IUpdateJob>() {

            @Override
            public IUpdateJob call() throws Exception {
                job.run();
                return job;
            }
        });
    }

    public void invokeAll() {
        try {
            final List<Future<IUpdateJob>> futures = pool.invokeAll(jobs.values(), 2, TimeUnit.SECONDS);
            for (final Future<IUpdateJob> future : futures) {
                try {
                    final IUpdateJob job = future.get();
                    job.finishSuccessful();
                    jobs.remove(future.get());
                } catch (final CancellationException e) {
                } catch (final ExecutionException e) {
                    ExtDocPlugin.logException(e);
                }
            }
        } catch (final InterruptedException e) {
            ExtDocPlugin.logException(e);
        }
        for (final IUpdateJob job : jobs.keySet()) {
            job.handleTimeout();
        }
        jobs.clear();
    }

    public interface IUpdateJob extends Runnable {

        void finishSuccessful();

        void handleTimeout();

    }

    public abstract static class AbstractUpdateJob implements IUpdateJob {

        protected final void displayTimeoutMessage(final Composite composite) {
            UiUtils.disposeChildren(composite);
            SwtFactory.createLabel(composite, "Provider timed out. Please review your network status.", true);
            UiUtils.layoutParents(composite);
        }
    }
}
