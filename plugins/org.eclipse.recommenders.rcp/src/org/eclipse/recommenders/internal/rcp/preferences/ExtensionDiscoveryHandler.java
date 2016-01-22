/**
 * Copyright (c) 2015 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Yasser Aziza - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.preferences;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.recommenders.rcp.utils.Dialogs;
import org.eclipse.recommenders.utils.rcp.preferences.AbstractLinkContributionPage;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

public class ExtensionDiscoveryHandler extends AbstractHandler {

    @Override
    public Object execute(final ExecutionEvent event) throws ExecutionException {
        Shell shell = HandlerUtil.getActiveShell(event);
        Display display = shell != null ? shell.getDisplay() : PlatformUI.getWorkbench().getDisplay();

        display.asyncExec(new Runnable() {

            @Override
            public void run() {
                Dialogs.newExtensionsDiscoveryDialog(event.getParameter(AbstractLinkContributionPage.COMMAND_HREF_ID))
                        .open();
            }
        });
        return null;
    }
}
