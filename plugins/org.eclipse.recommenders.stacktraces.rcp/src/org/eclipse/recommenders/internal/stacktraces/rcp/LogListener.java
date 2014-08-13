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
import static org.eclipse.recommenders.internal.stacktraces.rcp.Stacktraces.createDto;
import static org.eclipse.recommenders.utils.Checks.cast;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.property.Properties;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.recommenders.internal.stacktraces.rcp.dto.StackTraceEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.PlatformUI;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;

public class LogListener implements ILogListener, IStartup {

    private Cache<String, String> cache = CacheBuilder.newBuilder().maximumSize(10).build();
    private IEclipseContext ctx = (IEclipseContext) PlatformUI.getWorkbench().getService(IEclipseContext.class);
    private StacktracesRcpPreferences pref = ContextInjectionFactory.make(StacktracesRcpPreferences.class, ctx);

    private IObservableList statusList;
    private volatile boolean isDialogOpen;

    public LogListener() {
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                statusList = Properties.selfList(IStatus.class).observe(Lists.newArrayList());
            }
        });
    }

    @Override
    public void logging(final IStatus status, String nouse) {
        if (ignoreAllLogEvents()) {
            return;
        }
        if (!isErrorSeverity(status)) {
            return;
        }
        if (sentSimilarErrorBefore(status)) {
            sendStatus(status);
            return;
        }
        if (hasEclipsePluginId(status) || hasEclipseClassInStackFrames(status)) {
            checkAndSend(status);
            return;
        }
    }

    private boolean ignoreAllLogEvents() {
        return pref.modeIgnore();
    }

    private boolean isErrorSeverity(final IStatus status) {
        return status.matches(IStatus.ERROR);
    }

    private boolean sentSimilarErrorBefore(final IStatus status) {
        return cache.getIfPresent(status.toString()) != null;
    }

    private boolean hasEclipsePluginId(IStatus status) {
        String pluginId = status.getPlugin();
        return startsWithRecommendersOrCodetrails(pluginId);
    }

    private boolean startsWithRecommendersOrCodetrails(String s) {
        return startsWith(s, "org.eclipse.") || startsWith(s, "com.codetrails");
    }

    private boolean hasEclipseClassInStackFrames(IStatus status) {
        Throwable ex = status.getException();
        if (ex != null) {
            for (StackTraceElement ste : ex.getStackTrace()) {
                if (startsWithRecommendersOrCodetrails(ste.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }

    @VisibleForTesting
    protected void checkAndSend(final IStatus status) {
        // run on UI-thread to ensure that the observable list is not modified from another thread
        // and that the wizard is created on the UI-thread.
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                statusList.add(status);
                if (isDialogOpen) {
                    return;
                }
                if (pref.modeAsk()) {
                    StackTraceEvent tmp = createDto(status, pref);
                    // TODO set name and email?
                    tmp.name = "[filled on submit]";
                    tmp.email = "[filled on submit]";
                    isDialogOpen = true;

                    int open = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                            new StacktraceWizard(pref, statusList)).open();
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
        statusList.clear();
        cache.invalidateAll();
    }

    private void sendList() {
        for (Object entry : statusList) {
            IStatus status = cast(entry);
            sendStatus(status);
        }
    }

    private void sendStatus(final IStatus status) {
        if (ignoreAllLogEvents()) {
            // double safety. This is checked before elsewhere. But just to make sure...
            return;
        }

        cache.put(status.toString(), status.toString());
        StackTraceEvent event = createDto(status, pref);
        // System.out.println(event);
        new StacktraceUploadJob(event, pref.getServerUri()).schedule();
    }

    @Override
    public void earlyStartup() {
        Platform.addLogListener(this);
    }
}
