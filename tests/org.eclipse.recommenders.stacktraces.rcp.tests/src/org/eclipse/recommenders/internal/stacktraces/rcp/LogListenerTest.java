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
import static org.hamcrest.Matchers.greaterThan;
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
    private SettingsOverrider settingsOverrider;

    private static Status createErrorStatus() {
        Exception e1 = new RuntimeException();
        e1.fillInStackTrace();
        return new Status(IStatus.ERROR, TEST_PLUGIN_ID, "test message", e1);
    }

    private interface SettingsOverrider {
        void override(Settings settings);
    }

    @Before
    public void init() {
        sut = spy(new LogListener());
        doNothing().when(sut).checkAndSendWithDialog(Mockito.any(ErrorReport.class));
        // safety: do not send errors during tests
        doNothing().when(sut).sendStatus(Mockito.any(ErrorReport.class));
        Mockito.when(sut.readSettings()).thenAnswer(new Answer<Settings>() {
            @Override
            public Settings answer(InvocationOnMock invocation) throws Throwable {
                Settings settings = (Settings) invocation.callRealMethod();
                if (settingsOverrider != null) {
                    settingsOverrider.override(settings);
                }
                return settings;
            }
        });
    }

    @Test
    public void testInsertDebugStacktraceOnAskMode() {
        settingsOverrider = new SettingsOverrider() {
            @Override
            public void override(Settings settings) {
                settings.setAction(SendAction.ASK);
            }
        };
        Status empty = new Status(IStatus.ERROR, TEST_PLUGIN_ID, "has no stacktrace");
        Assert.assertThat(empty.getException(), nullValue());

        sut.logging(empty, "");

        assertThat(empty.getException(), notNullValue());
        assertThat(empty.getException().getStackTrace().length, greaterThan(0));
    }

    @Test
    public void testInsertDebugStacktraceOnSilentMode() {
        settingsOverrider = new SettingsOverrider() {
            @Override
            public void override(Settings settings) {
                settings.setAction(SendAction.SILENT);
            }
        };
        Status empty = new Status(IStatus.ERROR, TEST_PLUGIN_ID, "has no stacktrace");
        Assert.assertThat(empty.getException(), nullValue());

        sut.logging(empty, "");

        assertThat(empty.getException(), notNullValue());
        assertThat(empty.getException().getStackTrace().length, greaterThan(0));
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
    public void testNoInsertDebugStacktraceOnPauseDayMode() {
        settingsOverrider = new SettingsOverrider() {
            @Override
            public void override(Settings settings) {
                settings.setAction(SendAction.PAUSE_DAY);
            }
        };
        Status empty = new Status(IStatus.ERROR, TEST_PLUGIN_ID, "has no stacktrace");
        Assert.assertThat(empty.getException(), nullValue());

        sut.logging(empty, "");

        assertThat(empty.getException(), nullValue());
    }

    @Test
    public void testNoInsertDebugStacktraceOnPauseRestartMode() {
        settingsOverrider = new SettingsOverrider() {
            @Override
            public void override(Settings settings) {
                settings.setAction(SendAction.PAUSE_RESTART);
            }
        };
        Status empty = new Status(IStatus.ERROR, TEST_PLUGIN_ID, "has no stacktrace");
        Assert.assertThat(empty.getException(), nullValue());

        sut.logging(empty, "");

        assertThat(empty.getException(), nullValue());
    }

    @Test
    public void testIgnoreInfo() {
        Status status = new Status(IStatus.INFO, TEST_PLUGIN_ID, "a message");

        sut.logging(status, "");

        verify(sut, Mockito.never()).sendStatus(Mockito.any(ErrorReport.class));
    }

    @Test
    public void testIgnoreCancel() {
        Status status = new Status(IStatus.CANCEL, TEST_PLUGIN_ID, "a message");

        sut.logging(status, "");

        verify(sut, Mockito.never()).sendStatus(Mockito.any(ErrorReport.class));
    }

    @Test
    public void testIgnoreOk() {
        Status status = new Status(IStatus.OK, TEST_PLUGIN_ID, "a message");

        sut.logging(status, "");

        verify(sut, Mockito.never()).sendStatus(Mockito.any(ErrorReport.class));
    }

    @Test
    public void testIgnoreWarning() {
        Status status = new Status(IStatus.WARNING, TEST_PLUGIN_ID, "a message");

        sut.logging(status, "");

        verify(sut, Mockito.never()).sendStatus(Mockito.any(ErrorReport.class));
    }

    @Test
    public void testSendIfSilentMode() {
        settingsOverrider = new SettingsOverrider() {
            @Override
            public void override(Settings settings) {
                settings.setAction(SendAction.SILENT);
            }
        };
        Status status = new Status(IStatus.ERROR, TEST_PLUGIN_ID, "test message");

        sut.logging(status, "");

        verify(sut, times(1)).sendStatus(Mockito.any(ErrorReport.class));
    }

    @Test
    public void testNoCheckIfSilentMode() {
        settingsOverrider = new SettingsOverrider() {
            @Override
            public void override(Settings settings) {
                settings.setAction(SendAction.SILENT);
            }
        };
        Status status = new Status(IStatus.ERROR, TEST_PLUGIN_ID, "test message");

        sut.logging(status, "");

        verify(sut, never()).checkAndSendWithDialog(Mockito.any(ErrorReport.class));
    }

    @Test
    public void testCheckIfAskMode() {
        settingsOverrider = new SettingsOverrider() {
            @Override
            public void override(Settings settings) {
                settings.setAction(SendAction.ASK);
            }
        };
        Status status = new Status(IStatus.ERROR, TEST_PLUGIN_ID, "test message");

        sut.logging(status, "");

        verify(sut, times(1)).checkAndSendWithDialog(Mockito.any(ErrorReport.class));
    }

    @Test
    public void testUnknownPluginsIgnored() {
        settingsOverrider = new SettingsOverrider() {
            @Override
            public void override(Settings settings) {
                settings.setAction(SendAction.SILENT);
            }
        };
        Status status = new Status(IStatus.ERROR, ANY_THIRD_PARTY_PLUGIN_ID, "any message");

        sut.logging(status, "");

        verify(sut, never()).sendStatus(Mockito.any(ErrorReport.class));
    }

    @Test
    public void testIgnore() {
        Status status = new Status(IStatus.ERROR, TEST_PLUGIN_ID, "test message");
        settingsOverrider = new SettingsOverrider() {
            @Override
            public void override(Settings settings) {
                settings.setAction(SendAction.IGNORE);
            }
        };

        sut.logging(status, "");

        verify(sut, never()).checkAndSendWithDialog(Mockito.any(ErrorReport.class));
    }

    @Test
    public void testNoCheckOnPauseDay() {
        Status status = new Status(IStatus.ERROR, TEST_PLUGIN_ID, "test message");
        settingsOverrider = new SettingsOverrider() {
            @Override
            public void override(Settings settings) {
                settings.setAction(SendAction.PAUSE_DAY);
            }
        };

        sut.logging(status, "");

        verify(sut, never()).checkAndSendWithDialog(Mockito.any(ErrorReport.class));
    }

    @Test
    public void testNoSendOnPauseDay() {
        Status status = new Status(IStatus.ERROR, TEST_PLUGIN_ID, "test message");
        settingsOverrider = new SettingsOverrider() {
            @Override
            public void override(Settings settings) {
                settings.setAction(SendAction.PAUSE_DAY);
            }
        };

        sut.logging(status, "");

        verify(sut, never()).sendStatus(Mockito.any(ErrorReport.class));
    }

    @Test
    public void testNoCheckPauseRestart() {
        Status status = new Status(IStatus.ERROR, TEST_PLUGIN_ID, "test message");
        settingsOverrider = new SettingsOverrider() {
            @Override
            public void override(Settings settings) {
                settings.setAction(SendAction.PAUSE_RESTART);
            }
        };

        sut.logging(status, "");

        verify(sut, never()).checkAndSendWithDialog(Mockito.any(ErrorReport.class));
    }

    @Test
    public void testNoSendOnPauseRestart() {
        Status status = new Status(IStatus.ERROR, TEST_PLUGIN_ID, "test message");
        settingsOverrider = new SettingsOverrider() {
            @Override
            public void override(Settings settings) {
                settings.setAction(SendAction.PAUSE_RESTART);
            }
        };

        sut.logging(status, "");

        verify(sut, never()).sendStatus(Mockito.any(ErrorReport.class));
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
            }
        };

        sut.logging(createErrorStatus(), "");
        sut.logging(createErrorStatus(), "");

        verify(sut, times(2)).checkAndSendWithDialog(Mockito.any(ErrorReport.class));
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
        settingsOverrider = new SettingsOverrider() {
            @Override
            public void override(Settings settings) {
                settings.setAction(SendAction.SILENT);
            }
        };
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
}
