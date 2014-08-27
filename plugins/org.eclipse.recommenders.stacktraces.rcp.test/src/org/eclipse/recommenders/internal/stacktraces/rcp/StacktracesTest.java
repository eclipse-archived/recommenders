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
import static org.junit.Assert.*;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.recommenders.internal.stacktraces.rcp.dto.StackTraceElementDto;
import org.eclipse.recommenders.internal.stacktraces.rcp.dto.StackTraceEvent;
import org.eclipse.recommenders.internal.stacktraces.rcp.dto.ThrowableDto;
import org.junit.Test;

public class StacktracesTest {

    private static final String WHITELISTED_CLASSNAME = "java.lang.RuntimeException";
    private static final String NOT_WHITELISTED_CLASSNAME = "foo.bar.FooBarException";

    private static final String WHITELISTED_CLASSNAME_2 = "java.lang.String";
    private static final String WHITELISTED_METHODNAME_2 = "trim";

    private static final String NOT_WHITELISTED_CLASSNAME_2 = "foo.bar.AnyClass";
    private static final String NOT_WHITELISTED_METHODNAME_2 = "foo";

    private static String ANONYMIZED_TAG = "HIDDEN";

    private static StackTraceEvent createTestEvent() {
        RuntimeException cause = new RuntimeException("cause");
        cause.fillInStackTrace();
        Exception exception = new RuntimeException("exception message", cause);
        exception.fillInStackTrace();
        IStatus status = new Status(IStatus.ERROR, "org.eclipse.recommenders.stacktraces", "some error message",
                exception);
        StacktracesRcpPreferences pref = new StacktracesRcpPreferences();

        return Stacktraces.createDto(status, pref);
    }

    private static ThrowableDto createThrowableDto(String className) {
        ThrowableDto throwable = new ThrowableDto();
        throwable.classname = className;
        return throwable;
    }

    private static StackTraceElementDto createStackTraceElementDto(String className, String methodName) {
        StackTraceElementDto element = new StackTraceElementDto();
        element.classname = className;
        element.methodname = methodName;
        return element;
    }

    @Test
    public void testClearEventMessage() {
        StackTraceEvent event = createTestEvent();

        Stacktraces.clearMessages(event);

        assertThat(event.message, is(ANONYMIZED_TAG));
    }

    @Test
    public void testClearThrowableMessage() {
        StackTraceEvent event = createTestEvent();

        Stacktraces.clearMessages(event);

        assertThat(event.message, is(ANONYMIZED_TAG));
    }

    @Test
    public void testClearEventChainMessages() {
        StackTraceEvent event = createTestEvent();

        Stacktraces.clearMessages(event);

        assertTrue(event.trace.length > 0);
        for (ThrowableDto dto : event.trace) {
            assertThat(dto.message, is(ANONYMIZED_TAG));
        }
    }

    @Test
    public void testAnonymizeThrowableDtoClassname() {
        ThrowableDto throwable = createThrowableDto(NOT_WHITELISTED_CLASSNAME);

        Stacktraces.anonymizeStackTraceElements(throwable);

        assertThat(throwable.classname, is(ANONYMIZED_TAG));
    }

    @Test
    public void testAnonymizeThrowableDtoWhitelistedClassname() {
        ThrowableDto throwable = createThrowableDto(WHITELISTED_CLASSNAME);

        Stacktraces.anonymizeStackTraceElements(throwable);

        assertThat(throwable.classname, is(WHITELISTED_CLASSNAME));
    }

    @Test
    public void testAnonymizeStackTraceElementDtoClassnames() {
        StackTraceElementDto element = createStackTraceElementDto(NOT_WHITELISTED_CLASSNAME_2,
                NOT_WHITELISTED_METHODNAME_2);

        Stacktraces.anonymizeStackTraceElement(element);

        assertThat(element.classname, is(ANONYMIZED_TAG));
    }

    @Test
    public void testAnonymizeStackTraceElementDtoWhitelistedClassnames() {
        StackTraceElementDto element = createStackTraceElementDto(WHITELISTED_CLASSNAME, "");

        Stacktraces.anonymizeStackTraceElement(element);

        assertThat(element.classname, is(WHITELISTED_CLASSNAME));
    }

    @Test
    public void testAnonymizeStackTraceElementMethodname() {
        StackTraceElementDto element = createStackTraceElementDto(NOT_WHITELISTED_CLASSNAME_2,
                NOT_WHITELISTED_METHODNAME_2);

        Stacktraces.anonymizeStackTraceElement(element);

        assertThat(element.methodname, is(ANONYMIZED_TAG));
    }

    @Test
    public void testAnonymizeStackTraceElementWhitelistedMethodname() {
        StackTraceElementDto element = createStackTraceElementDto(WHITELISTED_CLASSNAME_2, WHITELISTED_METHODNAME_2);

        Stacktraces.anonymizeStackTraceElement(element);

        assertThat(element.methodname, is(WHITELISTED_METHODNAME_2));
    }
}
