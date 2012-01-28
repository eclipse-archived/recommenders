package org.eclipse.recommenders.utils;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class Executors {

    private Executors() {
        // just don't...
    }

    public static ThreadPoolExecutor coreThreadsTimoutExecutor(final int numberOfThreads, final int threadPriority,
            final String threadNamePrefix) {
        final ThreadFactory factory =
                new ThreadFactoryBuilder().setPriority(threadPriority).setNameFormat(threadNamePrefix + "%d").build();
        final ThreadPoolExecutor pool =
                new ThreadPoolExecutor(numberOfThreads, numberOfThreads, 100L, MILLISECONDS,
                        new LinkedBlockingQueue<Runnable>(), factory);
        pool.allowCoreThreadTimeOut(true);
        return pool;
    }
}
