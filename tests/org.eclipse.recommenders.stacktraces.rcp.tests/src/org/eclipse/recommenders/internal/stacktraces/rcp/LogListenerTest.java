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

import static org.eclipse.recommenders.internal.stacktraces.rcp.Constants.SYSPROP_ECLIPSE_BUILD_ID;
import static org.eclipse.recommenders.internal.stacktraces.rcp.model.SendAction.*;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.ErrorReport;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.SendAction;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.Settings;
import org.eclipse.recommenders.testing.RetainSystemProperties;
import org.eclipse.swt.widgets.Shell;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.common.base.Optional;

public class LogListenerTest {

    private static final String TEST_PLUGIN_ID = "org.eclipse.recommenders.stacktraces.rcp.tests";
    private static final String ANY_THIRD_PARTY_PLUGIN_ID = "any.third.party.plugin.id";
    private LogListener sut;
    private SettingsOverrider settingsOverrider;

    private static Status createErrorStatus() {
        Exception e1 = new RuntimeException();
        e1.fillInStackTrace();
        return new Status(IStatus.ERROR, TEST_PLUGIN_ID, "test message", e1);
    }

    // mockito can only mock visible & non-final classes
    protected class TestHistory extends History {
        @Override
        protected Directory createIndexDirectory() throws IOException {
            return new RAMDirectory();
        }
    }

    private interface SettingsOverrider {
        void override(Settings settings);
    }

    private static class SendActionSettingsOverrider implements SettingsOverrider {

        private SendAction action;

        public SendActionSettingsOverrider(SendAction action) {
            this.action = action;
        }

        @Override
        public void override(Settings settings) {
            settings.setAction(action);
        }
    }

    @Rule
    public RetainSystemProperties retainSystemProperties = new RetainSystemProperties();
    private History history;

    @Before
    public void setUp() {
        // Flag to bypass the runtime workbench test check:
        System.setProperty(SYSPROP_ECLIPSE_BUILD_ID, "unit-tests");

        history = spy(new TestHistory());
        history.startAsync();
        history.awaitRunning();
        sut = spy(new LogListener(history));
        // safety: do not send errors during tests
        doNothing().when(sut).checkAndSendWithDialog(Mockito.any(ErrorReport.class));
        doNothing().when(sut).sendStatus(Mockito.any(ErrorReport.class));
        Mockito.when(sut.readSettings()).thenAnswer(new Answer<Settings>() {
            @Override
            public Settings answer(InvocationOnMock invocation) throws Throwable {
                Settings settings = (Settings) invocation.callRealMethod();
                if (settingsOverrider != null) {
                    settingsOverrider.override(settings);
                }
                // don't open initial config dialog
                settings.setConfigured(true);
                return settings;
            }
        });
    }

    @Test
    public void testStatusUnmodified() {
        settingsOverrider = new SettingsOverrider() {
            @Override
            public void override(Settings settings) {
                settings.setAction(SendAction.SILENT);
            }
        };
        Status empty = new Status(IStatus.ERROR, TEST_PLUGIN_ID, "has no stacktrace");
        Status empty2 = new Status(IStatus.ERROR, TEST_PLUGIN_ID, "has no stacktrace");

        sut.logging(empty, "");
        assertTrue(EqualsBuilder.reflectionEquals(empty, empty2));
    }

    @Test
    public void testNoInsertDebugStacktraceOnIgnoreMode() {
        settingsOverrider = new SettingsOverrider() {
            @Override
            public void override(Settings settings) {
                settings.setAction(SendAction.IGNORE);
            }
        };
        Status empty = new Status(IStatus.ERROR, TEST_PLUGIN_ID, "has no stacktrace");
        Assert.assertThat(empty.getException(), nullValue());

        sut.logging(empty, "");

        assertThat(empty.getException(), nullValue());
    }

    @Test
    @Ignore
    public void testUnavailableShell() {
        // only for this test: use all ui-features and settings
        // reproduces Bug 448860
        sut = spy(new LogListener());
        doNothing().when(sut).sendStatus(Mockito.any(ErrorReport.class));
        Optional<Shell> absent = Optional.absent();
        when(sut.getWorkbenchWindowShell()).thenReturn(absent);
        Status status = new Status(IStatus.ERROR, TEST_PLUGIN_ID, "test message");
        sut.logging(status, "");
    }

    @Test
    public void testIgnoreInfo() {
        Status status = new Status(IStatus.INFO, TEST_PLUGIN_ID, "a message");

        sut.logging(status, "");

        verifyNoErrorReportSend();
    }

    @Test
    public void testNoReportIfBuildIdUnknown() {
        System.clearProperty(SYSPROP_ECLIPSE_BUILD_ID);
        Status status = new Status(IStatus.ERROR, TEST_PLUGIN_ID, "a message");

        sut.logging(status, "");

        verifyNoErrorReportSend();
    }

    @Test
    public void testIgnoreCancel() {
        Status status = new Status(IStatus.CANCEL, TEST_PLUGIN_ID, "a message");

        sut.logging(status, "");

        verifyNoErrorReportSend();
    }

    @Test
    public void testIgnoreOk() {
        Status status = new Status(IStatus.OK, TEST_PLUGIN_ID, "a message");

        sut.logging(status, "");

        verifyNoErrorReportSend();
    }

    @Test
    public void testIgnoreWarning() {
        Status status = new Status(IStatus.WARNING, TEST_PLUGIN_ID, "a message");

        sut.logging(status, "");

        verifyNoErrorReportSend();
    }

    @Test
    public void testSendIfSilentMode() {
        settingsOverrider = new SendActionSettingsOverrider(SILENT);
        Status status = new Status(IStatus.ERROR, TEST_PLUGIN_ID, "test message");

        sut.logging(status, "");

        verify(sut, times(1)).sendStatus(Mockito.any(ErrorReport.class));
    }

    @Test
    public void testUseHistory() {
        settingsOverrider = new SendActionSettingsOverrider(SILENT);
        Status status = new Status(IStatus.ERROR, TEST_PLUGIN_ID, "test message");

        sut.logging(status, "");

        verify(history, times(1)).seen(Mockito.any(ErrorReport.class));
    }

    @Test
    public void testNoCheckIfSilentMode() {
        settingsOverrider = new SendActionSettingsOverrider(SILENT);
        Status status = new Status(IStatus.ERROR, TEST_PLUGIN_ID, "test message");

        sut.logging(status, "");

        verify(sut, never()).checkAndSendWithDialog(Mockito.any(ErrorReport.class));
    }

    @Test
    public void testCheckIfAskMode() {
        settingsOverrider = new SendActionSettingsOverrider(ASK);
        Status status = new Status(IStatus.ERROR, TEST_PLUGIN_ID, "test message");

        sut.logging(status, "");

        verify(sut, times(1)).checkAndSendWithDialog(Mockito.any(ErrorReport.class));
    }

    @Test
    public void testIfSkipReportsTrue() {
        settingsOverrider = new SendActionSettingsOverrider(ASK);
        System.setProperty(Constants.SYSPROP_SKIP_REPORTS, "true");
        Status status = new Status(IStatus.ERROR, TEST_PLUGIN_ID, "test message");

        sut.logging(status, "");

        verifyNoErrorReportSend();
    }

    @Test
    public void testIfSkipReportsFalse() {
        settingsOverrider = new SendActionSettingsOverrider(ASK);
        System.setProperty(Constants.SYSPROP_SKIP_REPORTS, "false");
        Status status = new Status(IStatus.ERROR, TEST_PLUGIN_ID, "test message");

        sut.logging(status, "");

        verify(sut, times(1)).checkAndSendWithDialog(Mockito.any(ErrorReport.class));
    }

    @Test
    public void testUnknownPluginsIgnored() {
        settingsOverrider = new SendActionSettingsOverrider(SILENT);
        Status status = new Status(IStatus.ERROR, ANY_THIRD_PARTY_PLUGIN_ID, "any message");

        sut.logging(status, "");

        verifyNoErrorReportSend();
    }

    @Test
    public void testIgnore() {
        Status status = new Status(IStatus.ERROR, TEST_PLUGIN_ID, "test message");
        settingsOverrider = new SendActionSettingsOverrider(IGNORE);

        sut.logging(status, "");

        verifyNoErrorReportSend();
    }

    @Test
    public void testSkipSimilarErrors() {
        settingsOverrider = new SettingsOverrider() {
            @Override
            public void override(Settings settings) {
                settings.setSkipSimilarErrors(true);
            }
        };

        sut.logging(createErrorStatus(), "");
        sut.logging(createErrorStatus(), "");

        verify(sut, times(1)).checkAndSendWithDialog(Mockito.any(ErrorReport.class));
    }

    @Test
    public void testNoSkippingSimilarErrors() {
        settingsOverrider = new SettingsOverrider() {
            @Override
            public void override(Settings settings) {
                settings.setSkipSimilarErrors(false);
                settings.setAction(SendAction.SILENT);
            }
        };

        sut.logging(createErrorStatus(), "");
        sut.logging(createErrorStatus(), "");

        verify(sut, times(2)).sendStatus(Mockito.any(ErrorReport.class));
    }

    @Test
    public void testSilentSendsErrors() {
        settingsOverrider = new SettingsOverrider() {
            @Override
            public void override(Settings settings) {
                settings.setAction(SendAction.SILENT);
                settings.setSkipSimilarErrors(false);
            }
        };

        sut.logging(createErrorStatus(), "");
        sut.logging(createErrorStatus(), "");

        verify(sut, times(2)).sendStatus(Mockito.any(ErrorReport.class));

    }

    @Test
    public void testNoReportOfSourceFiles() {
        settingsOverrider = new SendActionSettingsOverrider(SILENT);
        String sourceDataMessage = "Exception occurred during compilation unit conversion:\n"
                + "----------------------------------- SOURCE BEGIN -------------------------------------\n"
                + "package some.package;\n" + "\n" + "import static some.import.method;\n"
                + "import static some.other.import;\n";
        Status status = new Status(IStatus.ERROR, TEST_PLUGIN_ID, sourceDataMessage, new RuntimeException());

        sut.logging(status, "");

        ArgumentCaptor<ErrorReport> captor = ArgumentCaptor.forClass(ErrorReport.class);
        verify(sut).sendStatus(captor.capture());
        Assert.assertEquals("source file contents removed", captor.getValue().getStatus().getMessage());
    }

    @Test
    public void testMonitoringStatusWithNoChildsFiltered() throws IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, NoSuchMethodException, SecurityException {
        settingsOverrider = new SendActionSettingsOverrider(SILENT);
        MultiStatus multi = new MultiStatus("org.eclipse.ui.monitoring", 0, "UI freeze of 6,0s at 11:24:59.108",
                new RuntimeException("stand-in-stacktrace"));
        Method method = Status.class.getDeclaredMethod("setSeverity", Integer.TYPE);
        method.setAccessible(true);
        method.invoke(multi, IStatus.ERROR);
        sut.logging(multi, "");
        verifyNoErrorReportSend();
    }

    private void verifyNoErrorReportSend() {
        verify(sut, never()).sendStatus(Mockito.any(ErrorReport.class));
        verify(sut, never()).checkAndSendWithDialog(Mockito.any(ErrorReport.class));
    }
}
