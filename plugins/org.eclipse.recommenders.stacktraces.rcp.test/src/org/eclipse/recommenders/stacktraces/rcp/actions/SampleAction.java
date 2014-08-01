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
package org.eclipse.recommenders.stacktraces.rcp.actions;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.osgi.framework.FrameworkUtil;

/**
 * Our sample action implements workbench action delegate. The action proxy will be created by the workbench and shown
 * in the UI. When the user tries to use the action, this delegate will be created and execution will be delegated to
 * it.
 *
 * @see IWorkbenchWindowActionDelegate
 */
public class SampleAction implements IWorkbenchWindowActionDelegate {
    private IWorkbenchWindow window;

    /**
     * The constructor.
     */
    public SampleAction() {
    }

    /**
     * The action has been activated. The argument of the method represents the 'real' action sitting in the workbench
     * UI.
     *
     * @see IWorkbenchWindowActionDelegate#run
     */
    @Override
    public void run(IAction action) {
        ILog log = Platform.getLog(FrameworkUtil.getBundle(getClass()));
        Exception exception = new RuntimeException("exception message");
        exception.fillInStackTrace();
        log.log(new Status(IStatus.ERROR, "org.eclipse.sample.id", "some error message", exception));
    }

    /**
     * Selection in the workbench has been changed. We can change the state of the 'real' action here if we want, but
     * this can only happen after the delegate has been created.
     *
     * @see IWorkbenchWindowActionDelegate#selectionChanged
     */
    @Override
    public void selectionChanged(IAction action, ISelection selection) {
    }

    /**
     * We can use this method to dispose of any system resources we previously allocated.
     *
     * @see IWorkbenchWindowActionDelegate#dispose
     */
    @Override
    public void dispose() {
    }

    /**
     * We will cache window object in order to be able to provide parent shell for the message dialog.
     *
     * @see IWorkbenchWindowActionDelegate#init
     */
    @Override
    public void init(IWorkbenchWindow window) {
        this.window = window;
    }
}
