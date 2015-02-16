/**
 * Copyright (c) 2015 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.news;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.recommenders.rcp.utils.BrowserUtils;
import org.eclipse.recommenders.rcp.utils.Shells;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;

public class NewsNotificationPopup extends AbstractNotificationPopup {

    private static final int DELAY_CLOSE_MS = 4000;
    private String message;

    public NewsNotificationPopup(String link) {
        super(Shells.getDisplay());
        setFadingEnabled(true);
        setDelayClose(DELAY_CLOSE_MS);
        this.message = link;
    }

    @Override
    protected void createContentArea(Composite composite) {
        super.createContentArea(composite);
        composite.setLayout(new GridLayout(1, true));
        Link link = new Link(composite, SWT.WRAP);
        link.setText(message);
        link.setLayoutData(GridDataFactory.fillDefaults().hint(AbstractNotificationPopup.MAX_WIDTH, SWT.DEFAULT)
                .create());
        link.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                BrowserUtils.openInExternalBrowser(e.text);
            }
        });

        Link optout = new Link(composite, SWT.WRAP);
        optout.setText("Click <a>here</a> to turn off news notifications.");
        optout.setLayoutData(GridDataFactory.fillDefaults().hint(AbstractNotificationPopup.MAX_WIDTH, SWT.DEFAULT)
                .create());
        optout.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                IEclipsePreferences prefs = CheckForProjectNewsJob.getPreferences();
                prefs.putBoolean(CheckForProjectNewsJob.NEWS_ENABLED, false);
                close();
            }
        });
    }

    @Override
    protected String getPopupShellTitle() {
        return "New Updates on Code Recommenders";
    }
}
