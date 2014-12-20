/**
 * Copyright (c) 2014 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.stacktraces.rcp;

import java.util.List;
import java.util.Set;

import org.eclipse.recommenders.internal.stacktraces.rcp.model.ErrorReports.StatusFilterSetting;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

public final class Constants {

    private Constants() {
        throw new IllegalStateException("Not meant to be instantiated"); //$NON-NLS-1$
    }

    static final String PLUGIN_ID = "org.eclipse.recommenders.stacktraces.rcp";

    static final String PREF_PAGE_ID = "org.eclipse.recommenders.stacktraces.rcp.preferencePages.errorReporting";

    /**
     * Specifying '-Dorg.eclipse.recommenders.stacktraces.rcp.skipReports=true' as vmarg in eclipse launch
     * configurations lets the log listener skip automated error reporting.
     */
    static final String SYSPROP_SKIP_REPORTS = PLUGIN_ID + ".skipReports";
    static final String SYSPROP_ECLIPSE_BUILD_ID = "eclipse.buildId";

    static final String PROP_NAME = "name";
    static final String PROP_EMAIL = "email";
    static final String PROP_ANONYMIZE_STACKTRACES = "anonymize-stacktraces";
    static final String PROP_ANONYMIZE_MESSAGES = "anonymize-messages";
    static final String PROP_CONFIGURED = "configured";
    static final String PROP_SEND_ACTION = "send-action";
    static final String PROP_REMEMBER_SEND_ACTION = "remember-send-action";
    static final String PROP_REMEMBER_SETTING_PERIOD_START = "remember-setting-period-start";
    static final String PROP_SKIP_SIMILAR_ERRORS = "skip-similar-errors";
    static final String PROP_WHITELISTED_PLUGINS = "whitelisted-plugins";
    static final String PROP_WHITELISTED_PACKAGES = "whitelisted-packages";
    static final String PROP_SERVER = "server-url";

    static final String HELP_URL = "https://docs.google.com/document/d/14vRLXcgSwy0rEbpJArsR_FftOJW1SjWUAmZuzc2O8YI/pub";
    static final String FEEDBACK_FORM_URL = "https://docs.google.com/a/codetrails.com/forms/d/1wd9AzydLv_TMa7ZBXHO7zQIhZjZCJRNMed-6J4fVNsc/viewform";
    static final String SERVER_URL = getServerUrl();

    private static String getServerUrl() {
        return System.getProperty(PLUGIN_ID + "." + PROP_SERVER,
                "https://dev.eclipse.org/recommenders/community/confess/0.5/reports/");
    }

    // Cache
    public static final int PREVIOUS_ERROR_CACHE_MAXIMUM_SIZE = 30;
    public static final int PREVIOUS_ERROR_CACHE_EXPIRE_AFTER_ACCESS_MINUTES = 10;

    // Whitelist for sending
    public static final String WHITELISTED_PLUGINS = "org.eclipse.;org.apache.log4j.;com.codetrails.;";
    public static final String WHITELISTED_PACKAGES = "org.eclipse.;;;org.apache.;java.;javax.;javafx.;sun.;com.sun.;com.codetrails.;org.osgi.;com.google.;ch.qos.;org.slf4j.;";

    // Classes removed from top of stand-in-stacktrace
    public static final Set<String> STAND_IN_STACKTRACE_BLACKLIST = ImmutableSet.of("java.security.AccessController",
            "org.eclipse.core.internal.runtime.Log", "org.eclipse.core.internal.runtime.RuntimeLog",
            "org.eclipse.core.internal.runtime.PlatformLogWriter",
            "org.eclipse.osgi.internal.log.ExtendedLogReaderServiceFactory",
            "org.eclipse.osgi.internal.log.ExtendedLogReaderServiceFactory$3",
            "org.eclipse.osgi.internal.log.ExtendedLogServiceFactory",
            "org.eclipse.osgi.internal.log.ExtendedLogServiceImpl", "org.eclipse.osgi.internal.log.LoggerImpl",
            "org.eclipse.recommenders.internal.stacktraces.rcp.StandInStacktraceProvider",
            "org.eclipse.recommenders.internal.stacktraces.rcp.LogListener");

    // values for anonymization
    public static final String HIDDEN = "HIDDEN";
    public static final String SOURCE_BEGIN_MESSAGE = "----------------------------------- SOURCE BEGIN -------------------------------------";
    public static final String SOURCE_FILE_REMOVED = "source file contents removed";

    // Filter settings for known child-stacktraces of a multistatus
    // @formatter:off
    public static final List<StatusFilterSetting> MULTISTATUS_CHILD_STACKTRACES_FILTER_SETTINGS = ImmutableList
            .of(
                    // at java.lang.Object.wait(Object.java:-2)
                    // at java.lang.Object.wait(Object.java:502)
                    // at org.eclipse.osgi.framework.eventmgr.EventManager$EventThread.getNextEvent(EventManager.java:400)
                    // at org.eclipse.osgi.framework.eventmgr.EventManager$EventThread.run(EventManager.java:336)
                    new StatusFilterSetting("org.eclipse.ui.monitoring", "java.lang.Object", "java.lang.Object",
                            "org.eclipse.osgi.framework.eventmgr.EventManager",
                            "org.eclipse.osgi.framework.eventmgr.EventManager"),
                            // at java.lang.Object.wait(Object.java:-2)
                            // at org.eclipse.core.internal.jobs.WorkerPool.sleep(WorkerPool.java:188)
                            // at org.eclipse.core.internal.jobs.WorkerPool.startJob(WorkerPool.java:220)
                            // at org.eclipse.core.internal.jobs.Worker.run(Worker.java:52)
                            new StatusFilterSetting("org.eclipse.ui.monitoring", "java.lang.Object",
                                    "org.eclipse.core.internal.jobs.WorkerPool", "org.eclipse.core.internal.jobs.WorkerPool",
                                    "org.eclipse.core.internal.jobs.Worker"),
                                    // at java.lang.Object.wait(Object.java:-2)
                                    // at java.lang.Object.wait(Object.java:502)
                                    // at org.eclipse.jdt.internal.core.search.processing.JobManager.run(JobManager.java:382)
                                    // at java.lang.Thread.run(Thread.java:745)
                                    new StatusFilterSetting("org.eclipse.ui.monitoring", "java.lang.Object", "java.lang.Object",
                                            "org.eclipse.jdt.internal.core.search.processing.JobManager", "java.lang.Thread"),
                                            // at org.eclipse.pde.internal.core.PluginModelManager.initializeTable(PluginModelManager.java:496)
                                            // at org.eclipse.pde.internal.core.PluginModelManager.targetReloaded(PluginModelManager.java:473)
                                            // at org.eclipse.pde.internal.core.RequiredPluginsInitializer$1.run(RequiredPluginsInitializer.java:34)
                                            // at org.eclipse.core.internal.jobs.Worker.run(Worker.java:55)
                                            new StatusFilterSetting("org.eclipse.ui.monitoring",
                                                    "org.eclipse.pde.internal.core.PluginModelManager",
                                                    "org.eclipse.pde.internal.core.PluginModelManager",
                                                    "org.eclipse.pde.internal.core.RequiredPluginsInitializer",
                                                    "org.eclipse.core.internal.jobs.Worker"),
                                                    // at java.lang.Object.wait(Object.java:-2)
                                                    // at java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:142)
                                                    // at java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:158)
                                                    // at org.eclipse.emf.common.util.CommonUtil$1ReferenceClearingQueuePollingThread.run(CommonUtil.java:70)
                                                    new StatusFilterSetting("org.eclipse.ui.monitoring", "java.lang.Object",
                                                            "java.lang.ref.ReferenceQueue", "java.lang.ref.ReferenceQueue",
                                                            "org.eclipse.emf.common.util.CommonUtil"),
                                                            // at java.lang.Object.wait(Object.java:-2)
                                                            // at org.eclipse.core.internal.jobs.InternalWorker.run(InternalWorker.java:59)
                                                            new StatusFilterSetting("org.eclipse.ui.monitoring", "java.lang.Object",
                                                                    "org.eclipse.core.internal.jobs.InternalWorker"),
                                                                    // at java.lang.Object.wait(Object.java:-2)
                                                                    // at org.eclipse.equinox.internal.util.impl.tpt.timer.TimerImpl.run(TimerImpl.java:141)
                                                                    // at java.lang.Thread.run(Thread.java:745)
                                                                    new StatusFilterSetting("org.eclipse.ui.monitoring",
                                                                            "java.lang.Object",
                                                                            "org.eclipse.equinox.internal.util.impl.tpt.timer.TimerImpl",
                                                                            "java.lang.Thread"),
                                                                            // at java.lang.Object.wait(Object.java:-2)
                                                                            // at java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:142)
                                                                            // at java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:158)
                                                                            // at java.lang.ref.Finalizer$FinalizerThread.run(Finalizer.java:209)
                                                                            new StatusFilterSetting("org.eclipse.ui.monitoring",
                                                                                    "java.lang.Object",
                                                                                    "java.lang.ref.ReferenceQueue",
                                                                                    "java.lang.ref.ReferenceQueue",
                                                                                    "java.lang.ref.Finalizer"),
                                                                                    // at java.lang.Object.wait(Object.java:-2)
                                                                                    // at java.lang.Object.wait(Object.java:502)
                                                                                    // at java.lang.ref.Reference$ReferenceHandler.run(Reference.java:157)
                                                                                    new StatusFilterSetting("org.eclipse.ui.monitoring",
                                                                                            "java.lang.Object",
                                                                                            "java.lang.Object",
                                                                                            "java.lang.ref.Reference$ReferenceHandler"),
                                                                                            // at java.lang.Object.wait(Object.java:-2)
                                                                                            // at java.lang.Object.wait(Object.java:502)
                                                                                            // at org.eclipse.equinox.internal.util.impl.tpt.threadpool.Executor.run(Executor.java:106)
                                                                                            new StatusFilterSetting("org.eclipse.ui.monitoring",
                                                                                                    "java.lang.Object",
                                                                                                    "java.lang.Object",
                                                                                                    "org.eclipse.equinox.internal.util.impl.tpt.threadpool.Executor"),
                                                                                                    // at sun.nio.ch.ServerSocketChannelImpl.accept0(ServerSocketChannelImpl.java:-2)
                                                                                                    // at sun.nio.ch.ServerSocketChannelImpl.accept(ServerSocketChannelImpl.java:241)
                                                                                                    // at org.eclipse.jetty.server.ServerConnector.accept(ServerConnector.java:377)
                                                                                                    // at org.eclipse.jetty.server.AbstractConnector$Acceptor.run(AbstractConnector.java:500)
                                                                                                    // at org.eclipse.jetty.util.thread.QueuedThreadPool.runJob(QueuedThreadPool.java:610)
                                                                                                    // at org.eclipse.jetty.util.thread.QueuedThreadPool$3.run(QueuedThreadPool.java:539)
                                                                                                    // at java.lang.Thread.run(Thread.java:745)
                                                                                                    new StatusFilterSetting("org.eclipse.ui.monitoring",
                                                                                                            "sun.nio.ch.ServerSocketChannelImpl",
                                                                                                            "sun.nio.ch.ServerSocketChannelImpl",
                                                                                                            "org.eclipse.jetty.server.ServerConnector",
                                                                                                            "org.eclipse.jetty.server.AbstractConnector",
                                                                                                            "org.eclipse.jetty.util.thread.QueuedThreadPool",
                                                                                                            "org.eclipse.jetty.util.thread.QueuedThreadPool",
                                                                                                            "java.lang.Thread"),
                                                                                                            // at sun.nio.ch.WindowsSelectorImpl$SubSelector.poll0(WindowsSelectorImpl.java:-2)
                                                                                                            // at sun.nio.ch.WindowsSelectorImpl$SubSelector.poll(WindowsSelectorImpl.java:296)
                                                                                                            // at sun.nio.ch.WindowsSelectorImpl$SubSelector.access$400(WindowsSelectorImpl.java:278)
                                                                                                            // at sun.nio.ch.WindowsSelectorImpl.doSelect(WindowsSelectorImpl.java:159)
                                                                                                            // at sun.nio.ch.SelectorImpl.lockAndDoSelect(SelectorImpl.java:87)
                                                                                                            // at sun.nio.ch.SelectorImpl.select(SelectorImpl.java:98)
                                                                                                            // at sun.nio.ch.SelectorImpl.select(SelectorImpl.java:102)
                                                                                                            // at org.eclipse.jetty.io.SelectorManager$ManagedSelector.select(SelectorManager.java:596)
                                                                                                            // at org.eclipse.jetty.io.SelectorManager$ManagedSelector.run(SelectorManager.java:545)
                                                                                                            // at org.eclipse.jetty.util.thread.NonBlockingThread.run(NonBlockingThread.java:52)
                                                                                                            // at org.eclipse.jetty.util.thread.QueuedThreadPool.runJob(QueuedThreadPool.java:610)
                                                                                                            // at org.eclipse.jetty.util.thread.QueuedThreadPool$3.run(QueuedThreadPool.java:539)
                                                                                                            // at java.lang.Thread.run(Thread.java:745)
                                                                                                            new StatusFilterSetting("org.eclipse.ui.monitoring",
                                                                                                                    "sun.nio.ch.WindowsSelectorImpl",
                                                                                                                    "sun.nio.ch.WindowsSelectorImpl",
                                                                                                                    "sun.nio.ch.WindowsSelectorImpl",
                                                                                                                    "sun.nio.ch.WindowsSelectorImpl",
                                                                                                                    "sun.nio.ch.SelectorImpl",
                                                                                                                    "sun.nio.ch.SelectorImpl",
                                                                                                                    "sun.nio.ch.SelectorImpl",
                                                                                                                    "org.eclipse.jetty.io.SelectorManager",
                                                                                                                    "org.eclipse.jetty.io.SelectorManager",
                                                                                                                    "org.eclipse.jetty.util.thread.NonBlockingThread",
                                                                                                                    "org.eclipse.jetty.util.thread.QueuedThreadPool",
                                                                                                                    "org.eclipse.jetty.util.thread.QueuedThreadPool",
                                                                                                                    "java.lang.Thread"),

                                                                                                                    // at sun.misc.Unsafe.park(Unsafe.java:-2)
                                                                                                                    // at java.util.concurrent.locks.LockSupport.parkNanos(LockSupport.java:215)
                                                                                                                    // at java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.awaitNanos(AbstractQueuedSynchronizer.java:2078)
                                                                                                                    // at java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue.take(ScheduledThreadPoolExecutor.java:1093)
                                                                                                                    // at java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue.take(ScheduledThreadPoolExecutor.java:809)
                                                                                                                    // at java.util.concurrent.ThreadPoolExecutor.getTask(ThreadPoolExecutor.java:1067)
                                                                                                                    // at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1127)
                                                                                                                    // at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)
                                                                                                                    // at java.lang.Thread.run(Thread.java:745)
                                                                                                                    new StatusFilterSetting("org.eclipse.ui.monitoring",
                                                                                                                            "sun.misc.Unsafe",
                                                                                                                            "java.util.concurrent.locks.LockSupport",
                                                                                                                            "java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject",
                                                                                                                            "java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue",
                                                                                                                            "java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue",
                                                                                                                            "java.util.concurrent.ThreadPoolExecutor",
                                                                                                                            "java.util.concurrent.ThreadPoolExecutor",
                                                                                                                            "java.util.concurrent.ThreadPoolExecutor$Worker",
                                                                                                                            "java.lang.Thread"),

                                                                                                                            // at sun.misc.Unsafe.park(Unsafe.java:-2)
                                                                                                                            // at java.util.concurrent.locks.LockSupport.parkNanos(LockSupport.java:215)
                                                                                                                            // at java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.awaitNanos(AbstractQueuedSynchronizer.java:2078)
                                                                                                                            // at java.util.concurrent.LinkedBlockingQueue.poll(LinkedBlockingQueue.java:467)
                                                                                                                            // at java.util.concurrent.ThreadPoolExecutor.getTask(ThreadPoolExecutor.java:1066)
                                                                                                                            // at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1127)
                                                                                                                            // at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)
                                                                                                                            // at java.lang.Thread.run(Thread.java:745)
                                                                                                                            new StatusFilterSetting("org.eclipse.ui.monitoring",
                                                                                                                                    "sun.misc.Unsafe",
                                                                                                                                    "java.util.concurrent.locks.LockSupport",
                                                                                                                                    "java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject",
                                                                                                                                    "java.util.concurrent.LinkedBlockingQueue",
                                                                                                                                    "java.util.concurrent.ThreadPoolExecutor",
                                                                                                                                    "java.util.concurrent.ThreadPoolExecutor",
                                                                                                                                    "java.util.concurrent.ThreadPoolExecutor$Worker",
                                                                                                                                    "java.lang.Thread"),

                                                                                                                                    // at java.lang.Object.wait(Object.java:-2)
                                                                                                                                    // at org.eclipse.jface.text.reconciler.AbstractReconciler$BackgroundThread.run(AbstractReconciler.java:179)
                                                                                                                                    new StatusFilterSetting("org.eclipse.ui.monitoring",
                                                                                                                                            "java.lang.Object",
                                                                                                                                            "org.eclipse.jface.text.reconciler.AbstractReconciler$BackgroundThread"),

                                                                                                                                            // empty  stacktrace
                                                                                                                                            new StatusFilterSetting("org.eclipse.ui.monitoring")
                    );
    // @formatter:on
}
