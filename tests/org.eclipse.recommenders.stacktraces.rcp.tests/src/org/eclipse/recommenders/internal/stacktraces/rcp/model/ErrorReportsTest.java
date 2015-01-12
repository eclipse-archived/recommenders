/**
 * Copyright (c) 2014 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Daniel Haftstein - initial tests.
 */
package org.eclipse.recommenders.internal.stacktraces.rcp.model;

import static org.eclipse.recommenders.internal.stacktraces.rcp.model.ErrorReports.newErrorReport;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.ErrorReports.AnonymizeStacktraceVisitor;
import org.junit.Assert;
import org.junit.Test;

public class ErrorReportsTest {
    private static final List<String> PREFIX_WHITELIST = Arrays.asList("sun.", "java.", "javax.", "org.eclipse.");

    private static final String WHITELISTED_CLASSNAME = "java.lang.RuntimeException";
    private static final String NOT_WHITELISTED_CLASSNAME = "foo.bar.FooBarException";

    private static final String WHITELISTED_CLASSNAME_2 = "java.lang.String";
    private static final String WHITELISTED_METHODNAME_2 = "trim";

    private static final String NOT_WHITELISTED_CLASSNAME_2 = "foo.bar.AnyClass";
    private static final String NOT_WHITELISTED_METHODNAME_2 = "foo";

    private static String ANONYMIZED_TAG = "HIDDEN";

    private static Settings settings;

    private static ErrorReport createTestEvent() {
        RuntimeException cause = newRuntimeException("cause");
        Exception exception = new RuntimeException("exception message", cause);
        exception.fillInStackTrace();
        IStatus status = new Status(IStatus.ERROR, "org.eclipse.recommenders.stacktraces", "some error message",
                exception);

        settings = ModelFactory.eINSTANCE.createSettings();
        return newErrorReport(status, settings);
    }

    private static Throwable createThrowable(String className) {
        Throwable throwable = ModelFactory.eINSTANCE.createThrowable();
        throwable.setClassName(className);
        return throwable;
    }

    private static StackTraceElement createStackTraceElementDto(String className, String methodName) {
        StackTraceElement element = ModelFactory.eINSTANCE.createStackTraceElement();
        element.setClassName(className);
        element.setMethodName(methodName);
        element.setFileName("file.java");
        return element;
    }

    private static java.lang.StackTraceElement createStackTraceElement(String declaringClass) {
        return new java.lang.StackTraceElement(declaringClass, "anyMethod", "Classname.java", -1);
    }

    private static java.lang.StackTraceElement[] createStackTrace(String... declaringClasses) {
        java.lang.StackTraceElement[] stackTraceElements = new java.lang.StackTraceElement[declaringClasses.length];
        for (int i = 0; i < declaringClasses.length; i++) {
            stackTraceElements[i] = createStackTraceElement(declaringClasses[i]);
        }
        return stackTraceElements;
    }

    @Test
    public void testClearEventMessage() {
        ErrorReport event = createTestEvent();

        ErrorReports.clearMessages(event);

        assertThat(event.getStatus().getMessage(), is(ANONYMIZED_TAG));
    }

    @Test
    public void testClearThrowableMessage() {
        ErrorReport event = createTestEvent();
        ErrorReports.clearMessages(event);
        assertThat(event.getStatus().getException().getMessage(), is(ANONYMIZED_TAG));
    }

    @Test
    public void testAnonymizeThrowableDtoClassname() {
        Throwable throwable = createThrowable(NOT_WHITELISTED_CLASSNAME);
        throwable.accept(new AnonymizeStacktraceVisitor(PREFIX_WHITELIST));
        assertThat(throwable.getClassName(), is(ANONYMIZED_TAG));
    }

    @Test
    public void testAnonymizeThrowableDtoWhitelistedClassname() {
        Throwable throwable = createThrowable(WHITELISTED_CLASSNAME);
        throwable.accept(new AnonymizeStacktraceVisitor(PREFIX_WHITELIST));
        assertThat(throwable.getClassName(), is(WHITELISTED_CLASSNAME));
    }

    @Test
    public void testAnonymizeStackTraceElementDtoClassnames() {
        StackTraceElement element = createStackTraceElementDto(NOT_WHITELISTED_CLASSNAME_2,
                NOT_WHITELISTED_METHODNAME_2);
        element.accept(new AnonymizeStacktraceVisitor(PREFIX_WHITELIST));
        assertThat(element.getClassName(), is(ANONYMIZED_TAG));
    }

    @Test
    public void testAnonymizeStackTraceElementDtoWhitelistedClassnames() {
        StackTraceElement element = createStackTraceElementDto(WHITELISTED_CLASSNAME, "");
        element.accept(new AnonymizeStacktraceVisitor(PREFIX_WHITELIST));
        assertThat(element.getClassName(), is(WHITELISTED_CLASSNAME));
    }

    @Test
    public void testAnonymizeStackTraceElementMethodname() {
        StackTraceElement element = createStackTraceElementDto(NOT_WHITELISTED_CLASSNAME_2,
                NOT_WHITELISTED_METHODNAME_2);
        element.accept(new AnonymizeStacktraceVisitor(PREFIX_WHITELIST));
        assertThat(element.getMethodName(), is(ANONYMIZED_TAG));
    }

    @Test
    public void testAnonymizeStackTraceElementWhitelistedMethodname() {
        StackTraceElement element = createStackTraceElementDto(WHITELISTED_CLASSNAME_2, WHITELISTED_METHODNAME_2);
        element.accept(new AnonymizeStacktraceVisitor(PREFIX_WHITELIST));
        assertThat(element.getMethodName(), is(WHITELISTED_METHODNAME_2));
    }

    @Test
    public void testFingerprint() {

        Exception cause = newRuntimeException("cause");
        Exception r1 = newRuntimeException("exception message");

        r1.fillInStackTrace();
        Exception r2 = new RuntimeException("exception message", cause);
        r2.fillInStackTrace();

        IStatus s1 = new Status(IStatus.ERROR, "org.eclipse.recommenders.stacktraces", "some error message", r1);
        IStatus s2 = new Status(IStatus.ERROR, "org.eclipse.recommenders.stacktraces", "some error message", r2);

        settings = ModelFactory.eINSTANCE.createSettings();
        settings.getWhitelistedPackages().add("org.");

        org.eclipse.recommenders.internal.stacktraces.rcp.model.Status noCause = ErrorReports.newStatus(s1, settings);
        org.eclipse.recommenders.internal.stacktraces.rcp.model.Status withCause = ErrorReports.newStatus(s2, settings);

        Assert.assertNotEquals(noCause.getFingerprint(), withCause.getFingerprint());
    }

    @Test
    public void testFingerprintNested() {
        Exception root = newRuntimeException("root");
        IStatus s1 = new Status(IStatus.ERROR, "org.eclipse.recommenders.stacktraces", "some error message", root);
        IStatus s2 = new MultiStatus("org.eclipse.recommenders.stacktraces", 0, new IStatus[] { s1 },
                "some error message", root);

        settings = ModelFactory.eINSTANCE.createSettings();
        settings.getWhitelistedPackages().add("org.");

        org.eclipse.recommenders.internal.stacktraces.rcp.model.Status normal = ErrorReports.newStatus(s1, settings);
        org.eclipse.recommenders.internal.stacktraces.rcp.model.Status multi = ErrorReports.newStatus(s2, settings);

        Assert.assertNotEquals(normal.getFingerprint(), multi.getFingerprint());
    }

    @Test
    public void testCoreExceptionHandling() {
        IStatus causingStatus = new Status(IStatus.ERROR, "the.causing.plugin", "first message");
        java.lang.Throwable causingException = new CoreException(causingStatus);
        IStatus causedStatus = new Status(IStatus.WARNING, "some.calling.plugin", "any other message", causingException);
        java.lang.Throwable rootException = new CoreException(causedStatus);
        IStatus rootEvent = new Status(IStatus.ERROR, "org.eclipse.recommenders.stacktraces", "someErrorMessage",
                rootException);
        settings = ModelFactory.eINSTANCE.createSettings();

        org.eclipse.recommenders.internal.stacktraces.rcp.model.Status rootStatus = ErrorReports.newStatus(rootEvent,
                settings);

        org.eclipse.recommenders.internal.stacktraces.rcp.model.Status child = rootStatus.getChildren().get(0);
        org.eclipse.recommenders.internal.stacktraces.rcp.model.Status leaf = child.getChildren().get(0);
        assertThat(child.getPluginId(), is("some.calling.plugin"));
        assertThat(leaf.getPluginId(), is("the.causing.plugin"));
    }

    @Test
    public void testMultistatusChildFiltering() {
        settings = ModelFactory.eINSTANCE.createSettings();
        settings.getWhitelistedPackages().add("org.");

        // java.lang.Exception: Stack Trace
        // at java.lang.Object.wait(Object.java:-2)
        // at org.eclipse.core.internal.jobs.Semaphore.acquire(Semaphore.java:39)
        // at org.eclipse.core.internal.jobs.JobManager.join(JobManager.java:851)
        // at org.eclipse.core.internal.jobs.InternalJob.join(InternalJob.java:384)
        // at org.eclipse.core.runtime.jobs.Job.join(Job.java:420)
        // ...
        Exception e1 = new Exception("Stack Trace 1");
        e1.setStackTrace(createStackTrace("java.lang.Object", "org.eclipse.core.internal.jobs.Semaphore",
                "org.eclipse.core.internal.jobs.JobManager", "org.eclipse.core.internal.jobs.InternalJob"));
        IStatus s1 = new Status(IStatus.ERROR, "org.eclipse.ui.monitoring",
                "Sample at 08:09:05.634 (+2,698s) Thread 'main' tid=1 (TIMED_WAITING)", e1);

        // java.lang.Exception: Stack Trace
        // at java.lang.Object.wait(Object.java:-2)
        // at java.lang.Object.wait(Object.java:502)
        // at org.eclipse.osgi.framework.eventmgr.EventManager$EventThread.getNextEvent(EventManager.java:400)
        // at org.eclipse.osgi.framework.eventmgr.EventManager$EventThread.run(EventManager.java:336)
        Exception e2 = new Exception("Stack Trace 2");
        e2.setStackTrace(createStackTrace("java.lang.Object", "java.lang.Object",
                "org.eclipse.osgi.framework.eventmgr.EventManager$EventThread",
                "org.eclipse.osgi.framework.eventmgr.EventManager$EventThread"));
        IStatus s2 = new Status(
                IStatus.ERROR,
                "org.eclipse.ui.monitoring",
                "Thread 'Provisioning Event Dispatcher' tid=46 (WAITING) Waiting for: org.eclipse.osgi.framework.eventmgr.EventManager$EventThread@1980ed30",
                e2);

        // java.lang.Exception: Stack Trace
        // at java.lang.Object.wait(Object.java:-2)
        // at org.eclipse.core.internal.jobs.WorkerPool.sleep(WorkerPool.java:188)
        // at org.eclipse.core.internal.jobs.WorkerPool.startJob(WorkerPool.java:220)
        // at org.eclipse.core.internal.jobs.Worker.run(Worker.java:52)
        Exception e3 = new Exception("Stack Trace 3");
        e3.setStackTrace(createStackTrace("java.lang.Object", "org.eclipse.core.internal.jobs.WorkerPool",
                "org.eclipse.core.internal.jobs.WorkerPool", "org.eclipse.core.internal.jobs.Worker"));
        IStatus s3 = new Status(
                IStatus.ERROR,
                "org.eclipse.ui.monitoring",
                "Thread 'Worker-12' tid=44 (TIMED_WAITING) Waiting for: org.eclipse.core.internal.jobs.WorkerPool@34bef503",
                e3);

        // java.lang.Exception: Stack Trace
        // at java.util.zip.ZipFile.open(ZipFile.java:-2)
        // at java.util.zip.ZipFile.<init>(ZipFile.java:220)
        // at java.util.zip.ZipFile.<init>(ZipFile.java:150)
        // at org.eclipse.pde.internal.core.util.ManifestUtils.loadManifest(ManifestUtils.java:88)
        // ...
        Exception e4 = new Exception("Stack Trace 4");
        e4.setStackTrace(createStackTrace("java.util.zip.ZipFile", "java.util.zip.ZipFile", "java.util.zip.ZipFile",
                "org.eclipse.pde.internal.core.util.ManifestUtils"));
        IStatus s4 = new Status(IStatus.ERROR, "org.eclipse.ui.monitoring", "Thread 'Worker-10' tid=42 (RUNNABLE)", e4);

        // java.lang.Exception: Stack Trace
        // at org.eclipse.pde.internal.core.PluginModelManager.initializeTable(PluginModelManager.java:496)
        // at org.eclipse.pde.internal.core.PluginModelManager.targetReloaded(PluginModelManager.java:473)
        // at org.eclipse.pde.internal.core.RequiredPluginsInitializer$1.run(RequiredPluginsInitializer.java:34)
        // at org.eclipse.core.internal.jobs.Worker.run(Worker.java:55)
        Exception e5 = new Exception("Stack Trace 5");
        e5.setStackTrace(createStackTrace("org.eclipse.pde.internal.core.PluginModelManager",
                "org.eclipse.pde.internal.core.PluginModelManager",
                "org.eclipse.pde.internal.core.RequiredPluginsInitializer$1", "org.eclipse.core.internal.jobs.Worker"));
        IStatus s5 = new Status(
                IStatus.ERROR,
                "org.eclipse.ui.monitoring",
                "Thread 'Worker-4' tid=36 (BLOCKED) Waiting for: org.eclipse.pde.internal.core.PluginModelManager@56e13c98Lock ",
                e5);

        IStatus s6 = new Status(IStatus.ERROR, "org.eclipse.any.plugin", "any message", new Exception("Stack Trace 6"));

        IStatus multi = new MultiStatus("org.eclipse.ui.monitoring", 0, new IStatus[] { s1, s2, s3, s4, s5, s6 },
                "UI freeze of 10s at 08:09:02.936", newRuntimeException("stand-in-stacktrace"));
        // 2, 3 and 5 should be filtered, 1,4 and 6 remain
        org.eclipse.recommenders.internal.stacktraces.rcp.model.Status newStatus = ErrorReports.newStatus(multi,
                settings);
        assertThat(newStatus.getChildren().size(), is(3));
        assertThat(newStatus.getChildren().get(0).getException().getMessage(), is("Stack Trace 1"));
        assertThat(newStatus.getChildren().get(1).getException().getMessage(), is("Stack Trace 4"));
        assertThat(newStatus.getChildren().get(2).getException().getMessage(), is("Stack Trace 6"));
    }

    @Test
    public void testMultistatusChildFilteringHandlesEmptyStacktrace() {
        settings = ModelFactory.eINSTANCE.createSettings();
        settings.getWhitelistedPackages().add("org.");

        Exception e1 = new Exception("Stack Trace 1");
        e1.setStackTrace(new java.lang.StackTraceElement[0]);
        IStatus s1 = new Status(IStatus.ERROR, "org.eclipse.ui.monitoring",
                "Thread 'Signal Dispatcher' tid=4 (RUNNABLE)", e1);

        IStatus multi = new MultiStatus("org.eclipse.ui.monitoring", 0, new IStatus[] { s1 },
                "UI freeze of 10s at 08:09:02.936", newRuntimeException("stand-in-stacktrace"));
        org.eclipse.recommenders.internal.stacktraces.rcp.model.Status newStatus = ErrorReports.newStatus(multi,
                settings);
        assertThat(newStatus.getChildren().size(), is(0));
    }

    @Test
    public void testPrettyPrintNullSafe1() {
        ModelFactory mf = ModelFactory.eINSTANCE;
        settings = mf.createSettings();
        ErrorReport report = mf.createErrorReport();
        ErrorReports.prettyPrint(report, settings);

    }

    @Test
    public void testPrettyPrintNullSafe2() {
        ModelFactory mf = ModelFactory.eINSTANCE;
        settings = mf.createSettings();
        ErrorReport report = mf.createErrorReport();
        report.setStatus(mf.createStatus());
        ErrorReports.prettyPrint(report, settings);
    }

    @Test
    public void testPrettyPrintNullSafe3() {
        ModelFactory mf = ModelFactory.eINSTANCE;
        settings = mf.createSettings();
        ErrorReport report = mf.createErrorReport();
        report.setStatus(mf.createStatus());
        report.getStatus().setException(mf.createThrowable());
        ErrorReports.prettyPrint(report, settings);
    }

    private static RuntimeException newRuntimeException(String message) {
        RuntimeException cause = new RuntimeException(message);
        cause.fillInStackTrace();
        return cause;
    }

}
