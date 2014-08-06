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
import org.eclipse.recommenders.stacktraces.StackTraceEvent;
import org.eclipse.recommenders.utils.gson.GsonUtil;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.PlatformUI;

public class LogListener implements ILogListener, IStartup {

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
        String plugin = status.getPlugin();
        if (startsWithRecommendersOrCodetrails(plugin)) {
            send(status);
            return;
        }
        Throwable ex = status.getException();
        if (ex != null) {
            for (StackTraceElement ste : ex.getStackTrace()) {
                if (startsWithRecommendersOrCodetrails(ste.getClassName())) {
                    send(status);
                    return;
                }
            }
        }
    }

    private boolean startsWithRecommendersOrCodetrails(String s) {
        return startsWith(s, "org.eclipse.") || startsWith(s, "com.codetrails");
    }

    private void send(final IStatus status) {
        if (pref.modeIgnore()) {
            // double safety. This is checked before elsewhere. But just to make sure...
            return;
        }

        if (pref.modeAsk()) {
            StackTraceEvent tmp = createDto(status, pref);
            tmp.name = "[filled on submit]";
            tmp.email = "[filled on submit]";
            int open = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                    new StacktraceWizard(pref, GsonUtil.serialize(tmp))).open();
            if (open == Dialog.OK) {
                StackTraceEvent event = createDto(status, pref);
                new StacktraceUploadJob(event, pref.getServerUri()).schedule();
            }
        }
    }

    @Override
    public void earlyStartup() {
        Platform.addLogListener(this);
    }
}
