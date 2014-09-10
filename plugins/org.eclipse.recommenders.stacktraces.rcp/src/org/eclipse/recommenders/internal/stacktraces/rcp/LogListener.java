/**
 * Copyright (c) 2014 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 *    Daniel Haftstein - added UI thread safety
 */
package org.eclipse.recommenders.internal.stacktraces.rcp;

import static org.apache.commons.lang3.StringUtils.startsWith;
import static org.eclipse.recommenders.utils.Checks.cast;
import static org.eclipse.recommenders.utils.Logs.log;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.property.Properties;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.ErrorReport;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.ErrorReports;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.SendAction;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.Settings;
import org.eclipse.recommenders.utils.Reflections;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.PlatformUI;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;

public class LogListener implements ILogListener, IStartup {

    private static Method SET_EXCEPTION = Reflections.getDeclaredMethod(Status.class, "setException", Throwable.class)
            .orNull();

    private Cache<String, String> cache = CacheBuilder.newBuilder().maximumSize(30)
            .expireAfterAccess(30, TimeUnit.MINUTES).build();
    private IObservableList errorReports;
    private volatile boolean isDialogOpen;

    private Settings settings;

    public LogListener() {
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                errorReports = Properties.selfList(ErrorReport.class).observe(Lists.newArrayList());
            }
        });
    }

    @Override
    public void logging(final IStatus status, String nouse) {
        readSettings();
        if (ignoreAllLogEvents()) {
            return;
        }
        if (!isErrorSeverity(status) || !isEclipsePluginId(status)) {
            return;
        }
        if (sentSimilarErrorBefore(status)) {
            return;
        }
        insertDebugStacktraceIfEmpty(status);
        checkAndSend(status);
    }

    private void readSettings() {
        settings = PreferenceInitializer.readSettings();
    }

    @VisibleForTesting
    public static void insertDebugStacktraceIfEmpty(final IStatus status) {
        // TODO this code should probably go elsewhere later.
        if (status.getException() == null && status instanceof Status && SET_EXCEPTION != null) {
            Throwable syntetic = new RuntimeException("Debug stacktrace provided by Code Recommenders");
            syntetic.fillInStackTrace();
            try {
                SET_EXCEPTION.invoke(status, syntetic);
            } catch (Exception e) {
                log(LogMessages.LOG_WARNING_REFLECTION_FAILED, e, SET_EXCEPTION);
            }
        }
    }

    private boolean ignoreAllLogEvents() {
        return settings.getAction() == SendAction.IGNORE;
    }

    private boolean isErrorSeverity(final IStatus status) {
        return status.matches(IStatus.ERROR);
    }

    private boolean sentSimilarErrorBefore(final IStatus status) {
        return cache.getIfPresent(status.toString()) != null;
    }

    private boolean isEclipsePluginId(IStatus status) {
        String pluginId = status.getPlugin();
        return startsWithRecommendersOrCodetrails(pluginId);
    }

    // TODO: codetrails id for debugging:
    private boolean startsWithRecommendersOrCodetrails(String s) {
        return startsWith(s, "org.eclipse.") || startsWith(s, "com.codetrails");
    }

    @VisibleForTesting
    protected void checkAndSend(final IStatus status) {
        // run on UI-thread to ensure that the observable list is not modified from another thread
        // and that the wizard is created on the UI-thread.
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                errorReports.add(ErrorReports.newErrorReport(status, settings));
                if (isDialogOpen) {
                    return;
                }
                if (settings.getAction() == SendAction.ASK) {
                    isDialogOpen = true;
                    ErrorReportWizard stacktraceWizard = new ErrorReportWizard(settings, errorReports);
                    WizardDialog wizardDialog = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                            .getShell(), stacktraceWizard);
                    int open = wizardDialog.open();
                    isDialogOpen = false;
                    if (open != Dialog.OK) {
                        clear();
                        return;
                    } else if (ignoreAllLogEvents()) {
                        // the user may have chosen to ignore events in the wizard just now. Respect this preference:
                        return;
                    }
                    sendList();
                    clear();
                }
            }
        });
    }

    private void clear() {
        errorReports.clear();
        cache.invalidateAll();
    }

    private void sendList() {
        for (Object entry : errorReports) {
            ErrorReport report = cast(entry);
            sendStatus(report);
        }
    }

    private void sendStatus(final ErrorReport report) {
        if (ignoreAllLogEvents()) {
            // double safety. This is checked before elsewhere. But just to make sure...
            return;
        }

        cache.put(report.toString(), report.toString());
        new UploadJob(report, settings, URI.create(settings.getServerUrl())).schedule();
    }

    @Override
    public void earlyStartup() {
        Platform.addLogListener(this);
    }
}
