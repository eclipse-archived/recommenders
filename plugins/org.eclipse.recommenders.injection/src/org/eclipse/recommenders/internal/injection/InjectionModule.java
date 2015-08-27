/**
 * Copyright (c) 2015 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Simon Laffoy - initial API and implementation.
 */
package org.eclipse.recommenders.internal.injection;

import static java.lang.Thread.MIN_PRIORITY;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

public class InjectionModule extends AbstractModule {

    @Override
    protected void configure() {
    }

    @Singleton
    @Provides
    public EventBus provideWorkspaceEventBus() {
        final int numberOfCores = Runtime.getRuntime().availableProcessors();
        final ExecutorService pool = coreThreadsTimoutExecutor(numberOfCores + 1, MIN_PRIORITY,
                "Recommenders-Bus-Thread-", //$NON-NLS-1$
                1L, TimeUnit.MINUTES);
        return new AsyncEventBus("Recommenders asychronous Workspace Event Bus", pool); //$NON-NLS-1$
    }

    private static ThreadPoolExecutor coreThreadsTimoutExecutor(final int numberOfThreads, final int threadPriority,
            final String threadNamePrefix, final long timeout, final TimeUnit unit) {
        final ThreadFactory factory = new ThreadFactoryBuilder().setPriority(threadPriority)
                .setNameFormat(threadNamePrefix + "%d").setDaemon(true).build();
        final ThreadPoolExecutor pool = new ThreadPoolExecutor(numberOfThreads, numberOfThreads, timeout, unit,
                new LinkedBlockingQueue<Runnable>(), factory);
        pool.allowCoreThreadTimeOut(true);
        return pool;
    }

}
