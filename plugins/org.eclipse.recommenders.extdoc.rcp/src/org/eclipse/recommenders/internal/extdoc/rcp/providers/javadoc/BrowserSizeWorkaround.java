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

import org.eclipse.jface.layout.GridDataFactory;
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

public final class BrowserSizeWorkaround {

    public static final int MILLIS_UNTIL_RESCALE = 500;

    public static final int MINIMUM_HEIGHT = 1;

    private final Browser browser;
    private GridData gridData;

    private final CountDownLatch latch;

    public BrowserSizeWorkaround(final Browser browser, final CountDownLatch latch) {
        this.browser = browser;
        this.latch = latch;
        browser.setJavascriptEnabled(true);

        ensureParentHasGridLayout();
        initializeGridData();
        // set size to minimum: This is required to determine the size correctly
        // after the input is set:
        layoutParents(browser);

        registerProgressListener();
    }

    private void ensureParentHasGridLayout() {
        if (!(browser.getParent().getLayout() instanceof GridLayout)) {
            throw new IllegalStateException(
                    "Browser size workaround requires that the parent composite of the browser widget uses a GridLayout.");
        }
    }

    private void initializeGridData() {
        gridData = GridDataFactory.fillDefaults().grab(true, false).create();
        browser.setLayoutData(gridData);
    }

    private void recalculateAndSetHeight() {
        Display.getDefault().asyncExec(new RescaleAction());
    }

    private void registerProgressListener() {
        browser.addProgressListener(new ProgressAdapter() {
            boolean mustRender = false;

            @Override
            public void completed(final ProgressEvent event) {
                // for unknown reasons, the completed method is called twice.
                // Once on begin, and once after the file load was finished.
                // Thus, we simply consider every second event only.
                if (mustRender) {
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
            final String script = "function getDocHeight() { var D = document; return Math.max( Math.max(D.body.scrollHeight, D.documentElement.scrollHeight), Math.max(D.body.offsetHeight, D.documentElement.offsetHeight),Math.max(D.body.clientHeight, D.documentElement.clientHeight));} return getDocHeight();";
            Double result = (Double) browser.evaluate(script);
            if (result == null) {
                // terminate re-layout operation if browser
                // widget fails to compute its size
                result = 100d;
            }
            final int height = (int) Math.ceil(result.doubleValue());
            gridData.heightHint = height;
            gridData.minimumHeight = height;
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
                theParentsParent.layout(new Control[] { composite }, SWT.DEFER);
                break;
            }
        }
    }
}
