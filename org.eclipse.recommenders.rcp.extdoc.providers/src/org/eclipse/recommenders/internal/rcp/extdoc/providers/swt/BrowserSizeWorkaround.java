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
package org.eclipse.recommenders.internal.rcp.extdoc.providers.swt;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.recommenders.rcp.extdoc.UiUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;

public final class BrowserSizeWorkaround {

    private static final int MINIMUM_HEIGHT = 1;

    private final Browser browser;
    private GridData gridData;

    public BrowserSizeWorkaround(final Browser browser) {
        this.browser = browser;
        browser.setJavascriptEnabled(true);

        ensureParentHasGridLayout();
        initializeGridData();

        registerProgressListener();
        registerLocationListener();
    }

    private void ensureParentHasGridLayout() {
        if (!(browser.getParent().getLayout() instanceof GridLayout)) {
            throw new IllegalStateException(
                    "Browser size workaround requires that the parent composite of the browser widget uses a GridLayout.");
        }
    }

    private void initializeGridData() {
        gridData = GridDataFactory.fillDefaults().grab(true, false).hint(SWT.DEFAULT, MINIMUM_HEIGHT)
                .minSize(SWT.DEFAULT, MINIMUM_HEIGHT).create();
        browser.setLayoutData(gridData);
    }

    private void recalculateAndSetHeight() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                } catch (final InterruptedException e) {
                    throw new IllegalStateException(e);
                }
                Display.getDefault().asyncExec(new RescaleAction());
            }
        }).start();
    }

    private void setHeightAndTriggerLayout(final int height) {
        gridData.heightHint = height;
        gridData.minimumHeight = height;
        UiUtils.layoutParents(browser.getParent());
    }

    private void registerProgressListener() {
        browser.addProgressListener(new ProgressListener() {
            @Override
            public void completed(final ProgressEvent event) {
                recalculateAndSetHeight();
            }

            @Override
            public void changed(final ProgressEvent event) {
            }
        });
    }

    private void registerLocationListener() {
        browser.addLocationListener(new LocationListener() {
            @Override
            public void changing(final LocationEvent event) {
                setHeightAndTriggerLayout(MINIMUM_HEIGHT);
            }

            @Override
            public void changed(final LocationEvent event) {
            }
        });
    }

    private final class RescaleAction implements Runnable {

        @Override
        public void run() {
            if (!browser.isDisposed()) {
                final Object result = browser
                        .evaluate("function getDocHeight() { var D = document; return Math.max( Math.max(D.body.scrollHeight, D.documentElement.scrollHeight), Math.max(D.body.offsetHeight, D.documentElement.offsetHeight),Math.max(D.body.clientHeight, D.documentElement.clientHeight));} return getDocHeight();");

                if (result == null) {
                    // terminate re-layout operation if browser widget fails to
                    // compute its size
                    return;
                }
                final int height = (int) Math.ceil(((Double) result).doubleValue());
                setHeightAndTriggerLayout(height);
            }
        }
    }
}
