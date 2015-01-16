/**
 * Copyright (c) 2015 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Daniel Haftstein - initial API
 */
package org.eclipse.recommenders.internal.stacktraces.rcp;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.google.common.base.Optional;

class Shells {

    /**
     * Return the modal shell that is currently open. If there isn't one then absent is returned.
     *
     * @param excludeShell
     *            A shell to exclude from the search. May be <code>null</code>.
     */
    public static Optional<Shell> getModalShellExcluding(Shell excludeShell) {
        // initial implementation in org.eclipse.equinox.internal.p2.ui.discovery.util.WorkbenchUtil
        IWorkbench workbench = PlatformUI.getWorkbench();
        Shell[] shells = workbench.getDisplay().getShells();
        int modal = SWT.APPLICATION_MODAL | SWT.SYSTEM_MODAL | SWT.PRIMARY_MODAL;
        for (Shell openShell : shells) {
            if (openShell.equals(excludeShell)) {
                continue;
            }
            if (openShell.isVisible()) {
                int style = openShell.getStyle();
                if ((style & modal) != 0) {
                    return Optional.of(openShell);
                }
            }
        }
        return Optional.absent();
    }

    public static Optional<Shell> getWorkbenchWindowShell() {
        IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (workbenchWindow != null) {
            Shell shell = workbenchWindow.getShell();
            if (shell != null) {
                return Optional.of(shell);
            }
        }
        return Optional.absent();
    }
}
