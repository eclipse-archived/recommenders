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
     *
     * @return {@code true} if any browser could be opened successfully, {@code false} if not.
     *
     * @since 2.2.6
     */
    public static boolean tryOpenInDefaultBrowser(URL url) {
        MultiStatus multiStatus = new MultiStatus(BUNDLE_SYMBOLIC_NAME, 0,
                MessageFormat.format(Messages.LOG_ERROR_FAILED_TO_OPEN_DEFAULT_BROWSER, url), null);
        try {
            Status defaultBrowserStatus = doOpenInDefaultBrowser(url);
            multiStatus.add(defaultBrowserStatus);
            if (defaultBrowserStatus.isOK()) {
                return true;
            }

            Status externalBrowserStatus = doOpenInExternalBrowser(url);
            multiStatus.add(externalBrowserStatus);
            if (externalBrowserStatus.isOK()) {
                return true;
            }

            Status osBrowserStatus = doOpenInOsBrowser(url);
            multiStatus.add(osBrowserStatus);
            if (osBrowserStatus.isOK()) {
                return true;
            }

            Status dialogBrowserStatus = doOpenInDialogBrowser(url, SWT.DEFAULT, SWT.DEFAULT);
            multiStatus.add(dialogBrowserStatus);
            if (dialogBrowserStatus.isOK()) {
                return true;
            }

            return false;
        } finally {
            if (!multiStatus.isOK()) {
                Platform.getLog(BUNDLE).log(multiStatus);
            }
        }
    }

    /**
     * @deprecated Use {@link #tryOpenInDefaultBrowser(URL)} instead
     */
    @Deprecated
    public static void openInDefaultBrowser(URL url) {
        tryOpenInDefaultBrowser(url);
    }

    public static boolean tryOpenInDefaultBrowser(String url) {
        try {
            return tryOpenInDefaultBrowser(new URL(url));
        } catch (MalformedURLException e) {
            Logs.log(LogMessages.ERROR_MALFORMED_URI, e, url);
            return false;
        }
    }

    /**
     * @deprecated Use {@link #tryOpenInDefaultBrowser(String)} instead
     */
    @Deprecated
    public static void openInDefaultBrowser(String url) {
        tryOpenInDefaultBrowser(url);
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
     *
     * @return {@code true} if any browser could be opened successfully, {@code false} if not.
     *
     * @since 2.2.6
     */
    public static boolean tryOpenInExternalBrowser(URL url) {
        MultiStatus multiStatus = new MultiStatus(BUNDLE_SYMBOLIC_NAME, 0,
                MessageFormat.format(Messages.LOG_ERROR_FAILED_TO_OPEN_EXTERNAL_BROWSER, url), null);
        try {
            Status externalBrowserStatus = doOpenInExternalBrowser(url);
            multiStatus.add(externalBrowserStatus);
            if (externalBrowserStatus.isOK()) {
                return true;
            }

            Status osBrowserStatus = doOpenInOsBrowser(url);
            multiStatus.add(osBrowserStatus);
            if (osBrowserStatus.isOK()) {
                return true;
            }

            Status defaultBrowserStatus = doOpenInDefaultBrowser(url);
            multiStatus.add(defaultBrowserStatus);
            if (defaultBrowserStatus.isOK()) {
                return true;
            }

            Status dialogBrowserStatus = doOpenInDialogBrowser(url, SWT.DEFAULT, SWT.DEFAULT);
            multiStatus.add(dialogBrowserStatus);
            if (dialogBrowserStatus.isOK()) {
                return true;
            }

            return false;
        } finally {
            if (!multiStatus.isOK()) {
                Platform.getLog(BUNDLE).log(multiStatus);
            }
        }
    }

    /**
     * @deprecated Use {@link #tryOpenInExternalBrowser(URL)} instead
     */
    @Deprecated
    public static void openInExternalBrowser(URL url) {
        tryOpenInExternalBrowser(url);
    }

    public static boolean tryOpenInExternalBrowser(String url) {
        try {
            return tryOpenInExternalBrowser(new URL(url));
        } catch (MalformedURLException e) {
            Logs.log(LogMessages.ERROR_MALFORMED_URI, e, url);
            return false;
        }
    }

    /**
     * @deprecated Use {@link #tryOpenInExternalBrowser(String)} instead
     */
    @Deprecated
    public static void openInExternalBrowser(String url) {
        tryOpenInExternalBrowser(url);
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
     *
     * @return {@code true} if any browser could be opened successfully, {@code false} if not.
     *
     * @since 2.2.6
     */
    public static boolean tryOpenInDialogBrowser(URL url, int width, int height) {
        MultiStatus multiStatus = new MultiStatus(BUNDLE_SYMBOLIC_NAME, 0,
                MessageFormat.format(Messages.LOG_ERROR_FAILED_TO_OPEN_DIALOG_BROWSER, url), null);
        try {
            Status dialogBrowserStatus = doOpenInDialogBrowser(url, width, height);
            multiStatus.add(dialogBrowserStatus);
            if (dialogBrowserStatus.isOK()) {
                return true;
            }

            Status externalBrowserStatus = doOpenInExternalBrowser(url);
            multiStatus.add(externalBrowserStatus);
            if (externalBrowserStatus.isOK()) {
                return true;
            }

            Status osBrowserStatus = doOpenInOsBrowser(url);
            multiStatus.add(osBrowserStatus);
            if (osBrowserStatus.isOK()) {
                return true;
            }

            Status defaultBrowserStatus = doOpenInDefaultBrowser(url);
            multiStatus.add(defaultBrowserStatus);
            if (defaultBrowserStatus.isOK()) {
                return true;
            }

            return false;
        } finally {
            if (!multiStatus.isOK()) {
                Platform.getLog(BUNDLE).log(multiStatus);
            }
        }
    }

    /**
     * @deprecated Use {@link #tryOpenInDialogBrowser(URL, int, int)} instead
     */
    @Deprecated
    public static void openInDialogBrowser(URL url, int width, int height) {
        tryOpenInDialogBrowser(url, width, height);
    }

    public static boolean tryOpenInDialogBrowser(URL url) {
        return tryOpenInDialogBrowser(url, SWT.DEFAULT, SWT.DEFAULT);
    }

    /**
     * @deprecated Use {@link #tryOpenInDialogBrowser(URL)} instead
     */
    @Deprecated
    public static void openInDialogBrowser(URL url) {
        tryOpenInDialogBrowser(url);
    }

    public static boolean tryOpenInDialogBrowser(String url) {
        return tryOpenInDialogBrowser(url, SWT.DEFAULT, SWT.DEFAULT);
    }

    /**
     * @deprecated Use {@link #tryOpenInDialogBrowser(String)} instead
     */
    @Deprecated
    public static void openInDialogBrowser(String url) {
        tryOpenInDialogBrowser(url);
    }

    public static boolean tryOpenInDialogBrowser(String url, int width, int height) {
        try {
            return tryOpenInDialogBrowser(new URL(url), width, height);
        } catch (MalformedURLException e) {
            Logs.log(LogMessages.ERROR_MALFORMED_URI, e, url);
            return false;
        }
    }

    /**
     * @deprecated Use {@link #tryOpenInDialogBrowser(String, int, int)} instead
     */
    @Deprecated
    public static void openInDialogBrowser(String url, int width, int height) {
        tryOpenInDialogBrowser(url, width, height);
    }

    private static Status doOpenInDefaultBrowser(URL url) {
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

    private static Status doOpenInExternalBrowser(URL url) {
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

    private static Status doOpenInOsBrowser(URL url) {
        boolean success = Program.launch(url.toExternalForm());
        if (success) {
            return new Status(Status.OK, BUNDLE_SYMBOLIC_NAME,
                    format(Messages.LOG_INFO_SUCESSFULLY_OPENED_OS_BROWSER, url));
        } else {
            return new Status(Status.ERROR, BUNDLE_SYMBOLIC_NAME,
                    format(Messages.LOG_ERROR_FAILED_TO_OPEN_OS_BROWSER, url));
        }
    }

    private static Status doOpenInDialogBrowser(URL url, int width, int height) {
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
                tryOpenInExternalBrowser(event.text);
            }
        });
    }
}
