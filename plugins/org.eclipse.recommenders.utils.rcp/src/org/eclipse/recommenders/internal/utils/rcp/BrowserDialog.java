/**
 * Copyright (c) 2015 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Simon Laffoy - initial API and implementation.
 */
package org.eclipse.recommenders.internal.utils.rcp;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.TitleEvent;
import org.eclipse.swt.browser.TitleListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class BrowserDialog extends Dialog {

    private static final Composite NULL_COMPOSITE = null;

    private final String url;
    private final int width;
    private final int height;

    public BrowserDialog(Shell parentShell, String url) {
        this(parentShell, url, 1000, 500);
    }

    public BrowserDialog(Shell parentShell, String url, int width, int height) {
        super(parentShell);
        this.url = url;
        this.width = width;
        this.height = height;
    }

    @Override
    protected void configureShell(Shell newShell) {
        newShell.setSize(width, height);
        super.configureShell(newShell);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        GridLayoutFactory.fillDefaults().spacing(1, 1).margins(0, 0).numColumns(1).applyTo(container);
        GridDataFactory.swtDefaults().grab(true, true).applyTo(container);

        Browser browser = new Browser(container, SWT.NONE);
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).hint(width, height).grab(true, true).applyTo(browser);

        browser.setUrl(url);
        browser.setVisible(true);

        browser.addTitleListener(new TitleListener() {

            @Override
            public void changed(TitleEvent event) {
                getShell().setText(event.title);
            }
        });

        return browser;
    }

    @Override
    protected Control createButtonBar(Composite parent) {
        return NULL_COMPOSITE;
    }
}
