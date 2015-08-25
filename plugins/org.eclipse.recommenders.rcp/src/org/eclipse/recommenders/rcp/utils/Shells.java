/**
 * Copyright (c) 2015 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API
 */
package org.eclipse.recommenders.rcp.utils;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.google.common.base.Optional;

@Deprecated
public final class Shells {

    private Shells() {
        // Not meant to be instantiated
    }

    public static Optional<Shell> getActiveWindowShell() {
        IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (workbenchWindow != null) {
            Shell shell = workbenchWindow.getShell();
            return Optional.fromNullable(shell);
        }
        return Optional.absent();
    }

    public static Display getDisplay() {
        return PlatformUI.getWorkbench().getDisplay();
    }
}
