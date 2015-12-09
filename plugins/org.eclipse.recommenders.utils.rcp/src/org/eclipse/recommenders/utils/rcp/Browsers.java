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

import static java.text.MessageFormat.format;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;

import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.recommenders.internal.utils.rcp.BrowserDialog;
import org.eclipse.recommenders.internal.utils.rcp.l10n.LogMessages;
import org.eclipse.recommenders.internal.utils.rcp.l10n.Messages;
import org.eclipse.recommenders.utils.Logs;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.osgi.framework.Bundle;

public final class Browsers {

    private static final Bundle BUNDLE = Logs.getBundle(Browsers.class);
    private static final String BUNDLE_SYMBOLIC_NAME = BUNDLE.getSymbolicName();

    private Browsers() {
        // Not meant to be instantiated
    }

    /**
     * Tries to open an URL with the web browser configured in the Eclipse preferences (General &gt; Web Browser). By
     * default, this will open a new editor to display the URL within the Eclipse IDE.
     * <p>
     * If that fails, this method will try the following fallbacks in order:
     * <ol>
     * <li>An external web browser, as configured in the Eclipse preferences</li>
     * <li>An external web browser, as determined by the OS</li>
     * <li>A web browser embedded in a dialog</li>
     * </ol>
     */
    public static void openInDefaultBrowser(URL url) {
        MultiStatus multiStatus = new MultiStatus(BUNDLE_SYMBOLIC_NAME, 0,
                MessageFormat.format(Messages.LOG_ERROR_FAILED_TO_OPEN_DEFAULT_BROWSER, url), null);
        try {
            Status defaultBrowserStatus = openInDefaultBrowserImpl(url);
            multiStatus.add(defaultBrowserStatus);
            if (defaultBrowserStatus.isOK()) {
                return;
            }

            Status externalBrowserStatus = openInExternalBrowserImpl(url);
            multiStatus.add(externalBrowserStatus);
            if (externalBrowserStatus.isOK()) {
                return;
            }

            Status osBrowserStatus = openInOsBrowserImpl(url);
            multiStatus.add(osBrowserStatus);
            if (osBrowserStatus.isOK()) {
                return;
            }

            Status dialogBrowserStatus = openInDialogBrowserImpl(url, SWT.DEFAULT, SWT.DEFAULT);
            multiStatus.add(dialogBrowserStatus);
            if (dialogBrowserStatus.isOK()) {
                return;
            }
        } finally {
            if (!multiStatus.isOK()) {
                Platform.getLog(BUNDLE).log(multiStatus);
            }
        }
    }

    public static void openInDefaultBrowser(String url) {
        try {
            openInDefaultBrowser(new URL(url));
        } catch (MalformedURLException e) {
            Logs.log(LogMessages.ERROR_MALFORMED_URI, e, url);
        }
    }

    /**
     * Tries to open an URL with an external web browser configured in the Eclipse preferences (General &gt; Web
     * Browser).
     * <p>
     * If that fails, this method will try the following fallbacks in order:
     * <ol>
     * <li>An external web browser, as determined by the OS</li>
     * <li>The default web browser, as configured in the Eclipse preferences</li>
     * <li>A web browser embedded in a dialog</li>
     * </ol>
     */
    public static void openInExternalBrowser(URL url) {
        MultiStatus multiStatus = new MultiStatus(BUNDLE_SYMBOLIC_NAME, 0,
                MessageFormat.format(Messages.LOG_ERROR_FAILED_TO_OPEN_EXTERNAL_BROWSER, url), null);
        try {
            Status externalBrowserStatus = openInExternalBrowserImpl(url);
            multiStatus.add(externalBrowserStatus);
            if (externalBrowserStatus.isOK()) {
                return;
            }

            Status osBrowserStatus = openInOsBrowserImpl(url);
            multiStatus.add(osBrowserStatus);
            if (osBrowserStatus.isOK()) {
                return;
            }

            Status defaultBrowserStatus = openInDefaultBrowserImpl(url);
            multiStatus.add(defaultBrowserStatus);
            if (defaultBrowserStatus.isOK()) {
                return;
            }

            Status dialogBrowserStatus = openInDialogBrowserImpl(url, SWT.DEFAULT, SWT.DEFAULT);
            multiStatus.add(dialogBrowserStatus);
            if (dialogBrowserStatus.isOK()) {
                return;
            }
        } finally {
            if (!multiStatus.isOK()) {
                Platform.getLog(BUNDLE).log(multiStatus);
            }
        }
    }

    public static void openInExternalBrowser(String url) {
        try {
            openInExternalBrowser(new URL(url));
        } catch (MalformedURLException e) {
            Logs.log(LogMessages.ERROR_MALFORMED_URI, e, url);
        }
    }

    /**
     * Tries to open an URL with a web browser embedded in a dialog.
     * <p>
     * If that fails, this method will try the following fallbacks in order:
     * <ol>
     * <li>An external web browser, as configured in the Eclipse preferences</li>
     * <li>An external web browser, as determined by the OS</li>
     * <li>The default web browser, as configured in the Eclipse preferences</li>
     * </ol>
     */
    public static void openInDialogBrowser(URL url, int width, int height) {
        MultiStatus multiStatus = new MultiStatus(BUNDLE_SYMBOLIC_NAME, 0,
                MessageFormat.format(Messages.LOG_ERROR_FAILED_TO_OPEN_DIALOG_BROWSER, url), null);
        try {
            Status dialogBrowserStatus = openInDialogBrowserImpl(url, width, height);
            multiStatus.add(dialogBrowserStatus);
            if (dialogBrowserStatus.isOK()) {
                return;
            }

            Status externalBrowserStatus = openInExternalBrowserImpl(url);
            multiStatus.add(externalBrowserStatus);
            if (externalBrowserStatus.isOK()) {
                return;
            }

            Status osBrowserStatus = openInOsBrowserImpl(url);
            multiStatus.add(osBrowserStatus);
            if (osBrowserStatus.isOK()) {
                return;
            }

            Status defaultBrowserStatus = openInDefaultBrowserImpl(url);
            multiStatus.add(defaultBrowserStatus);
            if (defaultBrowserStatus.isOK()) {
                return;
            }
        } finally {
            if (!multiStatus.isOK()) {
                Platform.getLog(BUNDLE).log(multiStatus);
            }
        }
    }

    public static void openInDialogBrowser(URL url) {
        openInDialogBrowser(url, SWT.DEFAULT, SWT.DEFAULT);
    }

    public static void openInDialogBrowser(String url) {
        openInDialogBrowser(url, SWT.DEFAULT, SWT.DEFAULT);
    }

    public static void openInDialogBrowser(String url, int width, int height) {
        try {
            openInDialogBrowser(new URL(url), width, height);
        } catch (MalformedURLException e) {
            Logs.log(LogMessages.ERROR_MALFORMED_URI, e, url);
        }
    }

    private static Status openInDefaultBrowserImpl(URL url) {
        try {
            IWebBrowser defaultBrowser = PlatformUI.getWorkbench().getBrowserSupport().createBrowser(null);
            defaultBrowser.openURL(url);
            return new Status(Status.OK, BUNDLE_SYMBOLIC_NAME,
                    format(Messages.LOG_INFO_SUCESSFULLY_OPENED_DEFAULT_BROWSER, url));
        } catch (PartInitException e) {
            return new Status(Status.ERROR, BUNDLE_SYMBOLIC_NAME,
                    format(Messages.LOG_ERROR_FAILED_TO_OPEN_DEFAULT_BROWSER, url), e);
        }
    }

    private static Status openInExternalBrowserImpl(URL url) {
        try {
            IWebBrowser externalBrowser = PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser();
            externalBrowser.openURL(url);
            return new Status(Status.OK, BUNDLE_SYMBOLIC_NAME,
                    format(Messages.LOG_INFO_SUCESSFULLY_OPENED_EXTERNAL_BROWSER, url));
        } catch (PartInitException e) {
            return new Status(Status.ERROR, BUNDLE_SYMBOLIC_NAME,
                    format(Messages.LOG_ERROR_FAILED_TO_OPEN_EXTERNAL_BROWSER, url), e);
        }
    }

    private static Status openInOsBrowserImpl(URL url) {
        boolean success = Program.launch(url.toExternalForm());
        if (success) {
            return new Status(Status.OK, BUNDLE_SYMBOLIC_NAME,
                    format(Messages.LOG_INFO_SUCESSFULLY_OPENED_OS_BROWSER, url));
        } else {
            return new Status(Status.ERROR, BUNDLE_SYMBOLIC_NAME,
                    format(Messages.LOG_ERROR_FAILED_TO_OPEN_OS_BROWSER, url));
        }
    }

    private static Status openInDialogBrowserImpl(URL url, int width, int height) {
        try {
            Shell activeShell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
            BrowserDialog browserDialog;
            if (width == SWT.DEFAULT || height == SWT.DEFAULT) {
                browserDialog = new BrowserDialog(activeShell, url.toExternalForm());
            } else {
                browserDialog = new BrowserDialog(activeShell, url.toExternalForm(), width, height);
            }
            browserDialog.open();
            return new Status(Status.OK, BUNDLE_SYMBOLIC_NAME,
                    format(Messages.LOG_INFO_SUCESSFULLY_OPENED_DIALOG_BROWSER, url));
        } catch (SWTError e) {
            if (e.code == SWT.ERROR_NO_HANDLES) {
                return new Status(Status.ERROR, BUNDLE_SYMBOLIC_NAME,
                        format(Messages.LOG_ERROR_FAILED_TO_OPEN_DIALOG_BROWSER, url), e);
            } else {
                throw e;
            }
        }
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
}
