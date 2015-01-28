/**
 * Copyright (c) 2014 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch, Daniel Haftstein - initial API and implementation.
 */
package org.eclipse.recommenders.internal.stacktraces.rcp;

import static org.eclipse.recommenders.internal.stacktraces.rcp.Constants.*;
import static org.eclipse.recommenders.internal.stacktraces.rcp.LogMessages.*;
import static org.eclipse.recommenders.internal.stacktraces.rcp.model.ErrorReports.*;
import static org.eclipse.recommenders.utils.Logs.log;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.property.Properties;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.ErrorReport;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.SendAction;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.Settings;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.Status;
import org.eclipse.recommenders.utils.Checks;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.PlatformUI;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;

public class LogListener implements ILogListener, IStartup {

    private IObservableList queueUI;
    // careful! do never make any modifications to this list! It's a means to access the queued reports outside the UI
    // thread. TODO is there a better way?
    private ArrayList<ErrorReport> queueRO;
    // private Settings settings;
    private StandInStacktraceProvider stacktraceProvider = new StandInStacktraceProvider();
    private History history;
    private Settings settings;

    private final Semaphore sendDialogLock = new Semaphore(1);
    private final Semaphore configureDialogLock = new Semaphore(1);

    public LogListener() {
        Display.getDefault().syncExec(new Runnable() {

            @Override
            public void run() {
                queueRO = Lists.newArrayList();
                queueUI = Properties.selfList(ErrorReport.class).observe(queueRO);
            }
        });
    }

    @VisibleForTesting
    public LogListener(History history, Settings settings) {
        this();
        this.history = history;
        this.settings = settings;
    }

    @Override
    public void earlyStartup() {
        settings = PreferenceInitializer.getDefault();
        Platform.addLogListener(this);
        try {
            history = new History(settings);
            history.startAsync();
        } catch (Exception e1) {
            log(HISTORY_START_FAILED);
        }
        PlatformUI.getWorkbench().addWorkbenchListener(new IWorkbenchListener() {

            @Override
            public boolean preShutdown(IWorkbench workbench, boolean forced) {
                try {
                    history.stopAsync();
                    history.awaitTerminated();
                } catch (Exception e) {
                    log(HISTORY_STOP_FAILED);
                }
                return true;
            }

            @Override
            public void postShutdown(IWorkbench workbench) {
            }
        });
    }

    @Override
    public void logging(final IStatus status, String nouse) {
        try {
            if (!isReportingAllowedInEnvironment() || !isErrorSeverity(status) || isWorkbenchClosing()
                    || !isHistoryRunning() || isQueueLimitReached()) {
                return;
            }
            if (!configureDialogLock.tryAcquire()) {
                return;
            }
            try {
                if (!settings.isConfigured()) {
                    firstConfiguration();
                }
            } finally {
                configureDialogLock.release();
            }

            if (!settings.isConfigured()) {
                log(LogMessages.FIRST_CONFIGURATION_FAILED);
                return;
            }
            if (!hasPluginIdWhitelistedPrefix(status, settings.getWhitelistedPluginIds())) {
                return;
            }
            SendAction sendAction = settings.getAction();
            if (!isSendingAllowedOnAction(sendAction)) {
                return;
            }
            final ErrorReport report = newErrorReport(status, settings);
            if (filterEmptyUiMonitoring(report)) {
                return;
            }
            stacktraceProvider.insertStandInStacktraceIfEmpty(report.getStatus());
            guessInvolvedPlugins(report);
            if (alreadyQueued(report) || seenSameOrSimilarErrorBefore(report)) {
                return;
            }
            addForSending(report);

            if (sendAction == SendAction.ASK) {
                checkAndSendWithDialog(report);
            } else if (sendAction == SendAction.SILENT) {
                sendAndClear();
            }
        } catch (Exception e) {
            log(REPORTING_ERROR, e);
        }
    }

    private boolean isQueueLimitReached() {
        return queueRO.size() >= 20;
    }

    private boolean filterEmptyUiMonitoring(ErrorReport report) {
        Status status = report.getStatus();
        if ("org.eclipse.ui.monitoring".equals(status.getPluginId())) {
            return status.getChildren().isEmpty();
        }
        return false;
    }

    private boolean alreadyQueued(ErrorReport report) {
        for (ErrorReport r : queueRO) {
            if (EcoreUtil.equals(report, r)) {
                return true;
            }
        }
        return false;
    }

    private boolean isHistoryRunning() {
        if (history == null) {
            return false;
        }
        return history.isRunning();
    }

    private boolean isWorkbenchClosing() {
        return PlatformUI.getWorkbench().isClosing();
    }

    private boolean isReportingAllowedInEnvironment() {
        return !skipSendingReports() && !isRuntimeEclipse();
    }

    private void sendAndClear() {
        sendList();
        clear();
    }

    private void sendList() {
        for (ErrorReport entry : queueRO) {
            ErrorReport report = Checks.cast(entry);
            sendStatus(report);
        }
    }

    private void clear() {
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                queueUI.clear();
            }
        });
    }

    private boolean skipSendingReports() {
        return Boolean.getBoolean(SYSPROP_SKIP_REPORTS);
    }

    private boolean isRuntimeEclipse() {
        return null == System.getProperty(SYSPROP_ECLIPSE_BUILD_ID);
    }

    private boolean isErrorSeverity(final IStatus status) {
        return status.matches(IStatus.ERROR);
    }

    private static boolean hasPluginIdWhitelistedPrefix(IStatus status, List<String> whitelistedIdPrefixes) {
        String pluginId = status.getPlugin();
        for (String id : whitelistedIdPrefixes) {
            if (pluginId.startsWith(id)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSendingAllowedOnAction(SendAction sendAction) {
        return sendAction == SendAction.ASK || sendAction == SendAction.SILENT;
    }

    private boolean seenSameOrSimilarErrorBefore(final ErrorReport report) {
        // for debugging / development mode
        if (!settings.isSkipSimilarErrors()) {
            return false;
        }
        return history.seenSimilar(report) // did we send a similar error before?
                || history.seen(report); // did we send exactly this error before?
    }

    private void firstConfiguration() {
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                try {
                    Optional<Shell> shell = getWorkbenchWindowShell();
                    if (shell.isPresent()) {
                        Configurator.ConfigureWithDialog(settings, shell.get());
                    }
                } catch (Exception e) {
                    log(REPORTING_ERROR, e);
                }
            }
        });
    }

    private void addForSending(final ErrorReport report) {
        Runnable run = new Runnable() {
            @Override
            public void run() {
                queueUI.add(report);
            }
        };
        Display current = Display.getCurrent();
        if (current != null) {
            run.run();
        } else {
            Display.getDefault().asyncExec(run);
        }
    }

    @VisibleForTesting
    protected void checkAndSendWithDialog(final ErrorReport report) {
        // run on UI-thread to ensure that the observable list is not modified from another thread
        // and that the wizard is created on the UI-thread.
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                // we only permit one dialog. If already open, skip this step and add directly to the queue.
                if (!sendDialogLock.tryAcquire()) {
                    return;
                }
                try {
                    Optional<Shell> shell = getWorkbenchWindowShell();
                    if (shell.isPresent()) {
                        ErrorReportDialog reportDialog = new ErrorReportDialog(shell.get(), history, settings, queueUI) {
                            @Override
                            public boolean close() {
                                boolean close = super.close();
                                if (close) {
                                    // when closing, release the lock to let another thread reopen it.
                                    sendDialogLock.release();
                                }
                                return close;
                            }
                        };
                        reportDialog.open();
                    }
                } catch (Exception e) {
                    // if something goes wrong in the internals of eclipse and throws an exception (it did in the past):
                    sendDialogLock.release();
                }
            }
        });
    }

    @VisibleForTesting
    protected void sendStatus(final ErrorReport report) {
        // double safety. This is checked before elsewhere. But just to make sure...
        if (settings.getAction() == SendAction.IGNORE) {
            return;
        }
        new UploadJob(report, history, settings, URI.create(settings.getServerUrl())).schedule();
    }

    @VisibleForTesting
    protected Optional<Shell> getWorkbenchWindowShell() {
        return Shells.getWorkbenchWindowShell();
    }

}
