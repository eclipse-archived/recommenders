/**
 * Copyright (c) 2015 Pawel Nowak.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Based on org.eclipse.recommenders.rcp.utils.BrowserUtils
 */
package org.eclipse.recommenders.internal.news.rcp;

import java.net.URL;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;

public class BrowserUtils {

    private BrowserUtils() {
        throw new IllegalStateException("Not meant to be instantiated"); //$NON-NLS-1$
    }

    public static void openInDefaultBrowser(URL url) {
        try {
            IWebBrowser defaultBrowser = PlatformUI.getWorkbench().getBrowserSupport().createBrowser(null);
            defaultBrowser.openURL(new URL(url.toExternalForm()));
        } catch (Exception e) {
            // via BrowserUtils: Ignore failure; this method is best effort.
        }
    }
}
