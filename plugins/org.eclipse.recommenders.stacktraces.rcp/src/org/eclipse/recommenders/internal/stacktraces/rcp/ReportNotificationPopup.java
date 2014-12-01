/**
 * Copyright (c) 2014 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.stacktraces.rcp;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.recommenders.internal.stacktraces.rcp.fadedialog.AbstractNotificationPopup;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Link;

public class ReportNotificationPopup extends AbstractNotificationPopup {

    private static final int DELAY_CLOSE_MS = 2000;
    private String message;
    private String url;

    public ReportNotificationPopup(String link, String url) {
        super(getDisplay());
        setFadingEnabled(true);
        setDelayClose(DELAY_CLOSE_MS);
        this.message = link;
        this.url = url;
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
                if (!isEmpty(url)) {
                    Browsers.openInExternalBrowser(url);
                }
            }
        });
    }

    @Override
    protected String getPopupShellTitle() {
        return "Error Report Notification";
    }

    public static Display getDisplay() {
        Display display = Display.getCurrent();
        // may be null if outside the UI thread
        if (display == null) {
            display = Display.getDefault();
        }
        return display;
    }

}
