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
package org.eclipse.recommenders.internal.news.rcp.notifications;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.recommenders.internal.news.rcp.Messages;
import org.eclipse.recommenders.rcp.utils.Shells;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class NoNewsNotificationPopup extends AbstractNotificationPopup {

    private static final int DELAY_CLOSE_MS = 4000;

    public NoNewsNotificationPopup() {
        super(Shells.getDisplay());
        setFadingEnabled(true);
        setDelayClose(DELAY_CLOSE_MS);
    }

    @Override
    protected void createContentArea(Composite composite) {
        super.createContentArea(composite);
        composite.setLayout(new GridLayout(1, true));

        Label noNewFeedsTitle = new Label(composite, SWT.NONE);
        GridDataFactory.fillDefaults().hint(AbstractNotificationPopup.MAX_WIDTH, SWT.DEFAULT).applyTo(noNewFeedsTitle);
        noNewFeedsTitle.setFont(CommonFonts.BOLD);
        noNewFeedsTitle.setText(Messages.LABEL_NO_NEW_FEEDS);
        return;
    }

    @Override
    protected String getPopupShellTitle() {
        return Messages.NOTIFICATION_TITLE;
    }
}
