/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.extdoc.rcp.providers.javadoc;

import java.util.concurrent.CountDownLatch;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressAdapter;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public final class BrowserSizeWorkaround {

    public static final int MILLIS_UNTIL_RESCALE = 500;

    public static final int MINIMUM_HEIGHT = 1;

    private final Browser browser;

    private final CountDownLatch latch;

    private final Composite parent;

    private final Shell helperShell;

    public BrowserSizeWorkaround(final Browser browser, final CountDownLatch latch) {
        this.latch = latch;

        this.browser = browser;
        browser.setJavascriptEnabled(true);
        registerProgressListener();
        this.parent = browser.getParent();

        // only .x is relevant:
        Point preferredBrowserSize = null;
        for (Composite parent = browser; parent != null; parent = parent.getParent()) {
            final Point size = parent.getSize();
            if (size.x > 0) {
                preferredBrowserSize = size;
                break;
            }
        }
        helperShell = new Shell();
        helperShell.setLayout(new GridLayout());
        helperShell.setSize(preferredBrowserSize);
        browser.setParent(helperShell);
        browser.setLayoutData(new GridData(preferredBrowserSize.x, MINIMUM_HEIGHT));
        helperShell.layout(true);
    }

    private void recalculateAndSetHeight() {
        Display.getDefault().asyncExec(new RescaleAction());
    }

    private void registerProgressListener() {
        browser.addProgressListener(new ProgressAdapter() {
            boolean mustRender = false;

            @Override
            public synchronized void completed(final ProgressEvent event) {
                final Display current = Display.getCurrent();

                // for unknown reasons, the completed method is called twice.
                // Once on begin, and once after the file load was finished.
                // Thus, we simply consider every second event only.
                if (mustRender) {
                    // try {
                    // // Thread.sleep(MILLIS_UNTIL_RESCALE);
                    // } catch (final InterruptedException e) {
                    // }
                    recalculateAndSetHeight();
                }
                mustRender = !mustRender;
            }
        });
    }

    private final class RescaleAction implements Runnable {

        @Override
        public void run() {
            if (browser.isDisposed()) {
                return;
            }
            final Point size = browser.getSize();
            final GridData layoutData = (GridData) browser.getLayoutData();
            final String script = "function getDocHeight() { var D = document; return Math.max( Math.max(D.body.scrollHeight, D.documentElement.scrollHeight), Math.max(D.body.offsetHeight, D.documentElement.offsetHeight),Math.max(D.body.clientHeight, D.documentElement.clientHeight));} return getDocHeight();";
            Double result = (Double) browser.evaluate(script);
            if (result == null) {
                // terminate re-layout operation if browser
                // widget fails to compute its size
                result = 100d;
            }
            final int height = (int) Math.ceil(result.doubleValue());
            // gridData.heightHint = height;
            // gridData.minimumHeight = height;
            final Point computeSize = browser.computeSize(size.x, height);
            layoutData.heightHint = height;
            layoutData.widthHint = size.x;
            browser.setSize(computeSize);
            browser.pack(true);
            browser.redraw();
            browser.setParent(parent);
            // browser.setSize(new Point(size.x, height / 2));
            // layoutParents(browser);
            latch.countDown();

        }
    }

    /**
     * @param composite
     *            The composite for which all children will be disposed.
     */
    public static void disposeChildren(final Composite composite) {
        if (composite.isDisposed()) {
            return;
        }
        for (final Control child : composite.getChildren()) {
            child.dispose();
        }
    }

    public static void layoutParents(final Composite composite) {
        for (Composite parent = composite; parent != null; parent = parent.getParent()) {
            if (parent instanceof ScrolledComposite) {
                final int newWidth = parent.getSize().x;
                final Point newSize = parent.computeSize(newWidth, SWT.DEFAULT);
                parent.setSize(newSize);
                final Composite theParentsParent = parent.getParent();
                theParentsParent.layout(new Control[] { composite });
                break;
            }
        }
    }
}
