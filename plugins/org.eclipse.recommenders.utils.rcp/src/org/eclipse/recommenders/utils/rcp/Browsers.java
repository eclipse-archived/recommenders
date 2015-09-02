/**
 * Copyright (c) 2014 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Dorn - initial API and implementation.
 */
package org.eclipse.recommenders.utils.rcp;

import static org.eclipse.recommenders.internal.utils.rcp.l10n.LogMessages.ERROR_FAILED_TO_OPEN_BROWSER;
import static org.eclipse.recommenders.utils.Logs.log;

import java.net.URL;

import org.eclipse.recommenders.internal.utils.rcp.BrowserDialog;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;

public final class Browsers {

    private Browsers() {
        // Not meant to be instantiated
    }

    /**
     * Tries to open an URL wit the web browser configured in the Eclipse preferences (General &gt; Web Browser). By
     * default, this will open a new editor to display the URL within the Eclipse IDE.
     */
    public static void openInDefaultBrowser(String url) {
        try {
            IWebBrowser defaultBrowser = PlatformUI.getWorkbench().getBrowserSupport().createBrowser(null);
            defaultBrowser.openURL(new URL(url));
        } catch (Exception e) {
            log(ERROR_FAILED_TO_OPEN_BROWSER, e, url);
            // Ignore failure; this method is best effort.
        }
    }

    public static void openInDefaultBrowser(URL url) {
        openInDefaultBrowser(url.toExternalForm());
    }

    /**
     * Tries to open an URL with an external web browser. If one is configure in the Eclipse preferences (General &gt;
     * Web Browser) it will prefer that over the operating system's default browser. If either way to open an external
     * browser does not succeed, this method will this will open a new editor to display the URL within the Eclipse IDE.
     */
    public static void openInExternalBrowser(String url) {
        try {
            IWebBrowser externalBrowser = PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser();
            externalBrowser.openURL(new URL(url));
        } catch (Exception e) {
            if (!Program.launch(url)) {
                openInDefaultBrowser(url); // or log if that fails as well.
            }
        }
    }

    public static void openInExternalBrowser(URL url) {
        openInExternalBrowser(url.toExternalForm());
    }

    /** Augments the supplied link to open it in a web browser when clicked on. */
    public static void addOpenBrowserAction(Link link) {
        link.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                openInExternalBrowser(event.text);
            }
        });
    }

    public static void openInDialogBrowser(String url) {
        BrowserDialog browserDialog = new BrowserDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell(), url);
        browserDialog.open();
    }

    public static void openInDialogBrowser(String url, int width, int height) {
        BrowserDialog browserDialog = new BrowserDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell(), url,
                width, height);
        browserDialog.open();
    }

    public static void openInDialogBrowser(URL url) {
        openInDialogBrowser(url.toExternalForm());
    }
}
