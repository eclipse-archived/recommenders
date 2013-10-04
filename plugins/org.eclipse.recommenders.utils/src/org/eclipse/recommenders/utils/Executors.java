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
package org.eclipse.recommenders.utils;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

public final class Executors {

    private Executors() {
        // just don't...
    }

    public static ThreadPoolExecutor coreThreadsTimoutExecutor(final int numberOfThreads, final int threadPriority,
            final String threadNamePrefix) {
        final ThreadFactory factory = new ThreadFactoryBuilder().setPriority(threadPriority)
                .setNameFormat(threadNamePrefix + "%d").setDaemon(true).build();
        final ThreadPoolExecutor pool = new ThreadPoolExecutor(numberOfThreads, numberOfThreads, 100L, MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(), factory);

        pool.allowCoreThreadTimeOut(true);
        return pool;
    }

    public static ThreadPoolExecutor coreThreadsTimoutExecutor(final int numberOfThreads, final int threadPriority,
            final String threadNamePrefix, final long timeout, final TimeUnit unit) {
        final ThreadFactory factory = new ThreadFactoryBuilder().setPriority(threadPriority)
                .setNameFormat(threadNamePrefix + "%d").setDaemon(true).build();
        final ThreadPoolExecutor pool = new ThreadPoolExecutor(numberOfThreads, numberOfThreads, timeout, unit,
                new LinkedBlockingQueue<Runnable>(), factory);
        pool.allowCoreThreadTimeOut(true);
        return pool;
    }
}
