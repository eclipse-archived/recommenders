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
package org.eclipse.recommenders.internal.stacktraces.rcp;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.ErrorReport;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.ErrorReports;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.ErrorReports.AnonymizeStacktraceVisitor;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.ModelFactory;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.Settings;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.StackTraceElement;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.Throwable;
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
        return ErrorReports.newErrorReport(status, settings);
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

    private static RuntimeException newRuntimeException(String message) {
        RuntimeException cause = new RuntimeException(message);
        cause.fillInStackTrace();
        return cause;
    }

}
