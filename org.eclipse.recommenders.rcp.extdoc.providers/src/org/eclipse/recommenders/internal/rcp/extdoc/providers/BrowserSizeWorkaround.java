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
package org.eclipse.recommenders.internal.rcp.extdoc.providers;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.recommenders.commons.utils.Throws;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;

public class BrowserSizeWorkaround {

    private final int minimumHeight = 0;
    private final Browser browser;
    private GridData gridData;

    public BrowserSizeWorkaround(final Browser browser) {
        this.browser = browser;
        ensureParentHasGridLayout();
        initializeGridData();
        initializeBrowser();
        registerListener();
    }

    private void ensureParentHasGridLayout() {
        if (!(browser.getParent().getLayout() instanceof GridLayout)) {
            Throws.throwIllegalStateException("Browser size workaround requires that the parent composite of the browser widget uses a GridLayout.");
        }
    }

    private void initializeGridData() {
        gridData = GridDataFactory.fillDefaults().grab(true, false).hint(SWT.DEFAULT, minimumHeight)
                .minSize(SWT.DEFAULT, minimumHeight).create();
        browser.setLayoutData(gridData);
    }

    private void initializeBrowser() {
        browser.setJavascriptEnabled(true);
    }

    private void setHeightAndTriggerLayout(final int height) {
        gridData.heightHint = height;
        gridData.minimumHeight = height;
        browser.getParent().getParent().layout();
    }

    private void recalculateAndSetHeight() {
        Display.getCurrent().asyncExec(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                }
                final Object result = browser
                        .evaluate("function getDocHeight() { var D = document; return Math.max( Math.max(D.body.scrollHeight, D.documentElement.scrollHeight), Math.max(D.body.offsetHeight, D.documentElement.offsetHeight),Math.max(D.body.clientHeight, D.documentElement.clientHeight));} return getDocHeight();");

                if (result == null) {
                    // terminate re-layout operation if browser widget fails to
                    // compute its size
                    return;
                }
                final int height = (int) Math.ceil((Double) result);
                setHeightAndTriggerLayout(height);

            }
        });
    }

    public void switchToMinimumSize() {
        setHeightAndTriggerLayout(minimumHeight);
    }

    private void registerListener() {
        browser.addProgressListener(new ProgressListener() {
            @Override
            public void completed(final ProgressEvent event) {
                recalculateAndSetHeight();
            }

            @Override
            public void changed(final ProgressEvent event) {
            }
        });

        browser.addLocationListener(new LocationListener() {
            @Override
            public void changing(final LocationEvent event) {
                switchToMinimumSize();
            }

            @Override
            public void changed(final LocationEvent event) {
            }
        });
    }
}
