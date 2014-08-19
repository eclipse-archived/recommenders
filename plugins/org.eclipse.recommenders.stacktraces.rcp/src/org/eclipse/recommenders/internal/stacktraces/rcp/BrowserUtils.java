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
package org.eclipse.recommenders.internal.stacktraces.rcp;

import java.net.URL;

import org.eclipse.swt.program.Program;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;

public class BrowserUtils {

    /**
     * Tries to open an URL wit the web browser configured in the Eclipse preferences (General &gt; Web Browser). By
     * default, this will open a new editor to display the URL within the Eclipse IDE.
     */
    public static void openInDefaultBrowser(String url) {
        try {
            IWebBrowser defaultBrowser = PlatformUI.getWorkbench().getBrowserSupport().createBrowser(null);
            defaultBrowser.openURL(new URL(url));
        } catch (Exception e) {
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
                openInDefaultBrowser(url);
            }
        }
    }

    public static void openInExternalBrowser(URL url) {
        openInExternalBrowser(url.toExternalForm());
    }
}
