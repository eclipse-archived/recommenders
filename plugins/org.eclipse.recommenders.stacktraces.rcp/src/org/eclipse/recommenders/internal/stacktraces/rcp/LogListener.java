/**
 * Copyright (c) 2010, 2014 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.stacktraces.rcp;

import static org.apache.commons.lang3.StringUtils.startsWith;
import static org.eclipse.recommenders.internal.stacktraces.rcp.Stacktraces.createDto;

import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.recommenders.internal.stacktraces.rcp.dto.StackTraceEvent;
import org.eclipse.recommenders.utils.gson.GsonUtil;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.PlatformUI;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class LogListener implements ILogListener, IStartup {

    Cache<String, String> cache = CacheBuilder.newBuilder().maximumSize(10).build();

    IEclipseContext ctx = (IEclipseContext) PlatformUI.getWorkbench().getService(IEclipseContext.class);
    StacktracesRcpPreferences pref = ContextInjectionFactory.make(StacktracesRcpPreferences.class, ctx);

    @Override
    public void logging(IStatus status, String nouse) {
        if (pref.modeIgnore()) {
            return;
        }
        if (!status.matches(IStatus.ERROR)) {
            return;
        }

        if (cache.getIfPresent(status.toString()) != null) {
            // if this / similar error is was sent before, send it right away!
            doSend(status);
            return;
        }
        String pluginId = status.getPlugin();
        if (startsWithRecommendersOrCodetrails(pluginId)) {
            checkAndSend(status);
            return;
        }
        Throwable ex = status.getException();
        if (ex != null) {
            for (StackTraceElement ste : ex.getStackTrace()) {
                if (startsWithRecommendersOrCodetrails(ste.getClassName())) {
                    checkAndSend(status);
                    return;
                }
            }
        }
    }

    private boolean startsWithRecommendersOrCodetrails(String s) {
        return startsWith(s, "org.eclipse.") || startsWith(s, "com.codetrails");
    }

    private void checkAndSend(final IStatus status) {
        if (pref.modeAsk()) {
            StackTraceEvent tmp = createDto(status, pref);
            tmp.name = "[filled on submit]";
            tmp.email = "[filled on submit]";
            int open = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                    new StacktraceWizard(pref, GsonUtil.serialize(tmp))).open();
            if (open != Dialog.OK) {
                return;
            } else if (pref.modeIgnore()) {
                // the user may have chosen to ignore events in the wizard just now. Respect this preference:
                return;
            }
        }
        doSend(status);
    }

    private void doSend(final IStatus status) {
        if (pref.modeIgnore()) {
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
