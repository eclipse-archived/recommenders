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

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.number.OrderingComparisons.greaterThan;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.ErrorReport;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.SendAction;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.Settings;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class LogListenerTest {

    private static final String TEST_PLUGIN_ID = "org.eclipse.recommenders.stacktraces.rcp.tests";
    private static final String ANY_THIRD_PARTY_PLUGIN_ID = "any.third.party.plugin.id";
    private LogListener sut;

    @Before
    public void init() {
        sut = spy(new LogListener());
        doNothing().when(sut).checkAndSend(Mockito.any(ErrorReport.class));
        // safety: do not send errors during tests
        doNothing().when(sut).sendStatus(Mockito.any(ErrorReport.class));
    }

    @Test
    public void testInsertDebugStacktrace() {
        Status empty = new Status(IStatus.ERROR, "debug", "has no stacktrace");
        Assert.assertThat(empty.getException(), nullValue());

        LogListener.insertDebugStacktraceIfEmpty(empty);

        assertThat(empty.getException(), notNullValue());
        assertThat(empty.getException().getStackTrace().length, greaterThan(0));
    }

    @Test
    public void testIgnoreInfo() {
        Status status = new Status(IStatus.INFO, ANY_THIRD_PARTY_PLUGIN_ID, "a message");

        sut.logging(status, "");

        verify(sut, Mockito.never()).checkAndSend(Mockito.any(ErrorReport.class));
    }

    @Test
    public void testIgnoreCancel() {
        Status status = new Status(IStatus.CANCEL, ANY_THIRD_PARTY_PLUGIN_ID, "a message");

        sut.logging(status, "");

        verify(sut, Mockito.never()).checkAndSend(Mockito.any(ErrorReport.class));
    }

    @Test
    public void testIgnoreOk() {
        Status status = new Status(IStatus.OK, TEST_PLUGIN_ID, "a message");

        sut.logging(status, "");

        verify(sut, Mockito.never()).checkAndSend(Mockito.any(ErrorReport.class));
    }

    @Test
    public void testIgnoreWarning() {
        Status status = new Status(IStatus.WARNING, TEST_PLUGIN_ID, "a message");

        sut.logging(status, "");

        verify(sut, Mockito.never()).checkAndSend(Mockito.any(ErrorReport.class));
    }

    @Test
    public void testEclipsePluginsHandled() {
        Status status = new Status(IStatus.ERROR, TEST_PLUGIN_ID, "test message");

        sut.logging(status, "");

        verify(sut, times(1)).checkAndSend(Mockito.any(ErrorReport.class));
    }

    @Test
    public void testUnknownPluginsIgnored() {
        Status status = new Status(IStatus.ERROR, ANY_THIRD_PARTY_PLUGIN_ID, "any message");

        sut.logging(status, "");

        verify(sut, never()).checkAndSend(Mockito.any(ErrorReport.class));
    }

    @Test
    public void testIgnore() {
        Status status = new Status(IStatus.ERROR, TEST_PLUGIN_ID, "test message");
        Mockito.when(sut.readSettings()).thenAnswer(new Answer<Settings>() {

            @Override
            public Settings answer(InvocationOnMock invocation) throws Throwable {
                Settings settings = (Settings) invocation.callRealMethod();
                settings.setAction(SendAction.IGNORE);
                return settings;
            }

        });

        sut.logging(status, "");

        verify(sut, never()).checkAndSend(Mockito.any(ErrorReport.class));
    }

    @Test
    public void testPauseDay() {
        Status status = new Status(IStatus.ERROR, TEST_PLUGIN_ID, "test message");
        Mockito.when(sut.readSettings()).thenAnswer(new Answer<Settings>() {

            @Override
            public Settings answer(InvocationOnMock invocation) throws Throwable {
                Settings settings = (Settings) invocation.callRealMethod();
                settings.setAction(SendAction.PAUSE_DAY);
                return settings;
            }

        });

        sut.logging(status, "");

        verify(sut, never()).checkAndSend(Mockito.any(ErrorReport.class));
    }

    @Test
    public void testPauseRestart() {
        Status status = new Status(IStatus.ERROR, TEST_PLUGIN_ID, "test message");
        Mockito.when(sut.readSettings()).thenAnswer(new Answer<Settings>() {

            @Override
            public Settings answer(InvocationOnMock invocation) throws Throwable {
                Settings settings = (Settings) invocation.callRealMethod();
                settings.setAction(SendAction.PAUSE_RESTART);
                return settings;
            }

        });

        sut.logging(status, "");

        verify(sut, never()).checkAndSend(Mockito.any(ErrorReport.class));
    }

    @Test
    public void testSkipSimilarErrorsSetting() {
        Mockito.when(sut.readSettings()).thenAnswer(new Answer<Settings>() {

            @Override
            public Settings answer(InvocationOnMock invocation) throws Throwable {
                Settings settings = (Settings) invocation.callRealMethod();
                settings.setSkipSimilarErrors(true);
                return settings;
            }

        });

        sut.logging(createErrorStatus(), "");
        sut.logging(createErrorStatus(), "");

        verify(sut, times(1)).checkAndSend(Mockito.any(ErrorReport.class));
    }

    @Test
    public void testNoSkippingSimilarErrorsSetting() {
        Mockito.when(sut.readSettings()).thenAnswer(new Answer<Settings>() {

            @Override
            public Settings answer(InvocationOnMock invocation) throws Throwable {
                Settings settings = (Settings) invocation.callRealMethod();
                settings.setSkipSimilarErrors(false);
                return settings;
            }

        });

        sut.logging(createErrorStatus(), "");
        sut.logging(createErrorStatus(), "");

        verify(sut, times(2)).checkAndSend(Mockito.any(ErrorReport.class));
    }

    @Test
    public void testSilentSendsErrors() {
        sut = spy(new LogListener());
        // checkAndSend included instead to default sut
        // safety: do not send errors during tests
        doNothing().when(sut).sendStatus(Mockito.any(ErrorReport.class));

        Mockito.when(sut.readSettings()).thenAnswer(new Answer<Settings>() {

            @Override
            public Settings answer(InvocationOnMock invocation) throws Throwable {
                Settings settings = (Settings) invocation.callRealMethod();
                settings.setAction(SendAction.SILENT);
                settings.setSkipSimilarErrors(false);
                return settings;
            }

        });

        sut.logging(createErrorStatus(), "");
        sut.logging(createErrorStatus(), "");

        verify(sut, times(2)).sendStatus(Mockito.any(ErrorReport.class));

    }

    @Test
    public void testNoReportOfSourceFiles() {
        Mockito.when(sut.readSettings()).thenAnswer(new Answer<Settings>() {

            @Override
            public Settings answer(InvocationOnMock invocation) throws Throwable {
                Settings settings = (Settings) invocation.callRealMethod();
                settings.setAction(SendAction.SILENT);
                return settings;
            }

        });
        String sourceDataMessage = "Exception occurred during compilation unit conversion:\n"
                + "----------------------------------- SOURCE BEGIN -------------------------------------\n"
                + "package some.package;\n" + "\n" + "import static some.import.method;\n"
                + "import static some.other.import;\n";
        Status status = new Status(IStatus.ERROR, TEST_PLUGIN_ID, sourceDataMessage, new RuntimeException());
        ArgumentCaptor<ErrorReport> captor = ArgumentCaptor.forClass(ErrorReport.class);
        sut.logging(status, "");
        verify(sut).sendStatus(captor.capture());
        Assert.assertEquals("source file contents removed", captor.getValue().getStatus().getMessage());
    }

    public Status createErrorStatus() {
        Exception e1 = new RuntimeException();
        e1.fillInStackTrace();
        return new Status(IStatus.ERROR, TEST_PLUGIN_ID, "test message", e1);
    }
}
