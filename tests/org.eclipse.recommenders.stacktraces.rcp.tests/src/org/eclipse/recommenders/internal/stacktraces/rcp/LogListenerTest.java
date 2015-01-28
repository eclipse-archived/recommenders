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

import static com.google.common.collect.Lists.newArrayList;
import static org.eclipse.recommenders.internal.stacktraces.rcp.Constants.SYSPROP_ECLIPSE_BUILD_ID;
import static org.eclipse.recommenders.internal.stacktraces.rcp.model.SendAction.*;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.*;
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
import org.eclipse.recommenders.internal.stacktraces.rcp.model.ModelFactory;
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

import com.google.common.base.Optional;

public class LogListenerTest {

    private static final String TEST_PLUGIN_ID = "org.eclipse.recommenders.stacktraces.rcp.tests";
    private static final String ANY_THIRD_PARTY_PLUGIN_ID = "any.third.party.plugin.id";
    private LogListener sut;

    private static Status createErrorStatus() {
        Exception e1 = new RuntimeException();
        StackTraceElement[] trace = ErrorReportsDTOs.createStacktraceForClasses("A", "D", "C");
        e1.setStackTrace(trace);
        return new Status(IStatus.ERROR, TEST_PLUGIN_ID, "test message", e1);
    }

    // mockito can only mock visible & non-final classes
    protected class TestHistory extends History {
        public TestHistory(Settings settings) {
            super(settings);
        }

        @Override
        protected Directory createIndexDirectory() throws IOException {
            return new RAMDirectory();
        }
    }

    @Rule
    public RetainSystemProperties retainSystemProperties = new RetainSystemProperties();
    private History history;
    private Settings settings;

    @Before
    public void setUp() throws Exception {
        // Flag to bypass the runtime workbench test check:
        System.setProperty(SYSPROP_ECLIPSE_BUILD_ID, "unit-tests");

        settings = ModelFactory.eINSTANCE.createSettings();
        settings.setConfigured(true);
        settings.setWhitelistedPluginIds(newArrayList(TEST_PLUGIN_ID));
        settings.setWhitelistedPackages(newArrayList("java"));
        history = spy(new TestHistory(settings));
        history.startAsync();
        history.awaitRunning();
        // not called on spy, so call manually
        history.startUp();

        sut = spy(new LogListener(history, settings));
        // safety: do not send errors during tests
        doNothing().when(sut).checkAndSendWithDialog(Mockito.any(ErrorReport.class));
        doNothing().when(sut).sendStatus(Mockito.any(ErrorReport.class));

        // Mockito.when(sut.readStacktracesRcpPreferences()).thenAnswer(new Answer<StacktracesRcpPreferences>() {
        // @Override
        // public StacktracesRcpPreferences answer(InvocationOnMock invocation) throws Throwable {
        // StacktracesRcpPreferences StacktracesRcpPreferences = (StacktracesRcpPreferences) invocation
        // .callRealMethod();
        // if (StacktracesRcpPreferencesOverrider != null) {
        // StacktracesRcpPreferencesOverrider.override(StacktracesRcpPreferences);
        // }
        // // don't open initial config dialog
        // StacktracesRcpPreferences.setConfigured(true);
        // return StacktracesRcpPreferences;
        // }
        // });
    }

    @Test
    public void testStatusUnmodified() {
        settings.setAction(SendAction.SILENT);
        Status empty = new Status(IStatus.ERROR, TEST_PLUGIN_ID, "has no stacktrace");
        Status empty2 = new Status(IStatus.ERROR, TEST_PLUGIN_ID, "has no stacktrace");

        sut.logging(empty, "");
        assertTrue(EqualsBuilder.reflectionEquals(empty, empty2));
    }

    @Test
    public void testNoInsertDebugStacktraceOnIgnoreMode() {
        settings.setAction(SendAction.IGNORE);
        Status empty = new Status(IStatus.ERROR, TEST_PLUGIN_ID, "has no stacktrace");
        Assert.assertThat(empty.getException(), nullValue());

        sut.logging(empty, "");

        assertThat(empty.getException(), nullValue());
    }

    @Test
    public void testInsertDebugStacktrace() {

        settings.setAction(SendAction.SILENT);
        Status empty = new Status(IStatus.ERROR, TEST_PLUGIN_ID, "has no stacktrace");

        sut.logging(empty, "");

        ArgumentCaptor<ErrorReport> captor = ArgumentCaptor.forClass(ErrorReport.class);
        verify(sut).sendStatus(captor.capture());
        ErrorReport sendReport = captor.getValue();
        assertThat(sendReport.getStatus().getException(), not(nullValue()));
    }

    @Test
    public void testBundlesAddedToDebugStacktrace() {
        settings.setAction(SendAction.SILENT);
        Status empty = new Status(IStatus.ERROR, TEST_PLUGIN_ID, "has no stacktrace");

        sut.logging(empty, "");

        ArgumentCaptor<ErrorReport> captor = ArgumentCaptor.forClass(ErrorReport.class);
        verify(sut).sendStatus(captor.capture());
        ErrorReport sendReport = captor.getValue();
        assertThat(sendReport.getPresentBundles(), not(empty()));
    }

    @Test
    @Ignore
    public void testUnavailableShell() {
        // only for this test: use all ui-features and StacktracesRcpPreferences
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
        settings.setAction(SendAction.SILENT);
        Status status = new Status(IStatus.ERROR, TEST_PLUGIN_ID, "test message");

        sut.logging(status, "");

        verify(sut, times(1)).sendStatus(Mockito.any(ErrorReport.class));
    }

    @Test
    public void testUseHistory() {
        settings.setAction(SendAction.SILENT);
        settings.setSkipSimilarErrors(true);
        Status status = new Status(IStatus.ERROR, TEST_PLUGIN_ID, "test message");

        sut.logging(status, "");

        verify(history, times(1)).seen(Mockito.any(ErrorReport.class));
    }

    @Test
    public void testNoCheckIfSilentMode() {
        settings.setAction(SendAction.SILENT);
        Status status = new Status(IStatus.ERROR, TEST_PLUGIN_ID, "test message");

        sut.logging(status, "");

        verify(sut, never()).checkAndSendWithDialog(Mockito.any(ErrorReport.class));
    }

    @Test
    public void testCheckIfAskMode() {
        settings.setAction(ASK);
        settings.setSkipSimilarErrors(true);
        Status status = new Status(IStatus.ERROR, TEST_PLUGIN_ID, "test message");

        sut.logging(status, "");

        verify(sut, times(1)).checkAndSendWithDialog(Mockito.any(ErrorReport.class));
    }

    @Test
    public void testIfSkipReportsTrue() {
        settings.setAction(SendAction.SILENT);
        System.setProperty(Constants.SYSPROP_SKIP_REPORTS, "true");
        Status status = new Status(IStatus.ERROR, TEST_PLUGIN_ID, "test message");

        sut.logging(status, "");

        verifyNoErrorReportSend();
    }

    @Test
    public void testIfSkipReportsFalse() {
        settings.setAction(ASK);
        System.setProperty(Constants.SYSPROP_SKIP_REPORTS, "false");
        Status status = new Status(IStatus.ERROR, TEST_PLUGIN_ID, "test message");

        sut.logging(status, "");

        verify(sut, times(1)).checkAndSendWithDialog(Mockito.any(ErrorReport.class));
    }

    @Test
    public void testUnknownPluginsIgnored() {
        settings.setAction(SendAction.SILENT);
        Status status = new Status(IStatus.ERROR, ANY_THIRD_PARTY_PLUGIN_ID, "any message");

        sut.logging(status, "");

        verifyNoErrorReportSend();
    }

    @Test
    public void testIgnore() {
        Status status = new Status(IStatus.ERROR, TEST_PLUGIN_ID, "test message");
        settings.setAction(SendAction.IGNORE);

        sut.logging(status, "");

        verifyNoErrorReportSend();
    }

    @Test
    public void testSkipSimilarErrors() {
        settings.setSkipSimilarErrors(true);
        settings.setAction(ASK);

        Status s1 = createErrorStatus();
        Status s2 = createErrorStatus();
        sut.logging(s1, "");
        sut.logging(s2, "");

        verify(sut, times(1)).checkAndSendWithDialog(Mockito.any(ErrorReport.class));
    }

    @Test
    public void testNoSkippingSimilarErrors() {
        settings.setSkipSimilarErrors(false);
        settings.setAction(SILENT);

        sut.logging(createErrorStatus(), "");
        sut.logging(createErrorStatus(), "");

        verify(sut, times(2)).sendStatus(Mockito.any(ErrorReport.class));
    }

    @Test
    public void testSilentSendsErrors() {

        settings.setSkipSimilarErrors(false);
        settings.setAction(SILENT);

        sut.logging(createErrorStatus(), "");
        sut.logging(createErrorStatus(), "");

        verify(sut, times(2)).sendStatus(Mockito.any(ErrorReport.class));

    }

    @Test
    public void testNoReportOfSourceFiles() {
        settings.setAction(SILENT);
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
        settings.setAction(SILENT);
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
