/**
 * Copyright (c) 2010, 2014 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - manual test code.
 */
package org.eclipse.recommenders.stacktraces.rcp.actions;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.osgi.framework.FrameworkUtil;

public class LogErrorsAction implements IWorkbenchWindowActionDelegate {
    private int counter;

    @Override
    public void run(IAction action) {
        System.setProperty("eclipse.buildId", "unit-tests");
        Job job = new Job("test exceptions") {

            @Override
            public IStatus run(IProgressMonitor monitor) {
                System.setProperty("eclipse.buildId", "unit-tests");
                // Display.getDefault().syncExec(new Runnable() {
                // @Override
                // public void run() {
                //
                // ConfigurationDialog wizard = new ConfigurationDialog(PlatformUI.getWorkbench()
                // .getActiveWorkbenchWindow().getShell(), PreferenceInitializer.readSettings());
                // wizard.setBlockOnOpen(true);
                // wizard.open();
                //
                // }
                // });
                //
                // if (true) {
                // return Status.OK_STATUS;
                // }
                IStatus[] children = new IStatus[3];
                ILog log = Platform.getLog(FrameworkUtil.getBundle(getClass()));
                for (int i = 0; i < children.length; i++) {
                    RuntimeException cause = new IllegalArgumentException("cause" + i);
                    cause.fillInStackTrace();
                    Exception exception = new RuntimeException("exception message", cause);
                    exception.fillInStackTrace();
                    for (int j = 3; j >= 0; j--) {
                        Exception e = new RuntimeException("exception " + j, exception);
                        e.fillInStackTrace();
                        exception = e;
                    }
                    children[i] = new Status(IStatus.ERROR, "org.eclipse.recommenders.stacktraces.rcp",
                            "status error message " + ++counter, exception);
                    try {
                        Thread.sleep(750);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                log.log(new MultiStatus("org.eclipse.recommenders.stacktraces.rcp", IStatus.ERROR, children,
                        "status error message", new RuntimeException()));
                return Status.OK_STATUS;
            }
        };
        job.schedule();
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
    }

    @Override
    public void dispose() {
    }

    @Override
    public void init(IWorkbenchWindow window) {
    }

}
