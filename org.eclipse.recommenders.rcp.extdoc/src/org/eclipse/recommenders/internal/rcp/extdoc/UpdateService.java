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
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.recommenders.rcp.extdoc.ExtDocPlugin;

public class UpdateService {

    private final ExecutorService pool = Executors.newCachedThreadPool();
    private final Map<UpdateJob, Callable<UpdateJob>> jobs = new HashMap<UpdateJob, Callable<UpdateJob>>();

    public void schedule(final UpdateJob job) {
        jobs.put(job, new Callable<UpdateJob>() {

            @Override
            public UpdateJob call() throws Exception {
                job.run();
                return job;
            }
        });
    }

    public void invokeAll() {
        try {
            for (final Future<UpdateJob> future : pool.invokeAll(jobs.values(), 5, TimeUnit.SECONDS)) {
                try {
                    final UpdateJob job = future.get();
                    job.handleSuccessful();
                    jobs.remove(future.get());
                } catch (final CancellationException e) {
                } catch (final ExecutionException e) {
                    ExtDocPlugin.logException(e);
                }
            }
        } catch (final InterruptedException e) {
            ExtDocPlugin.logException(e);
        }
        for (final UpdateJob job : jobs.keySet()) {
            job.handleCancellation();
        }
        jobs.clear();
    }

    public static interface UpdateJob extends Runnable {

        void handleSuccessful();

        void handleCancellation();

    }
}
