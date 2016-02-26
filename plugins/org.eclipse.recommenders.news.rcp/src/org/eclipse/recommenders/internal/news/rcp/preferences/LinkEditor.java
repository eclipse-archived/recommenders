/**
 * Copyright (c) 2015 Pawel Nowak.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.recommenders.internal.news.rcp.preferences;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.dialogs.PreferencesUtil;

public final class LinkEditor extends FieldEditor {

    public LinkEditor(String message, String preferencePageId, Composite parent) {
        // this super constructor call use message and preference page id as preference name and label text, because it
        // calls doFillIntoGrid() method where those variables are needed
        // and in this particular field editor preference name and label text are useless.
        super(message, preferencePageId, parent);
    }

    @Override
    protected void adjustForNumColumns(int numColumns) {
    }

    @Override
    protected void doFillIntoGrid(final Composite parent, int numColumns) {
        Link notificationsLink = new Link(parent, SWT.NONE | SWT.WRAP);
        notificationsLink.setLayoutData(GridDataFactory.swtDefaults().span(2, 1).align(SWT.FILL, SWT.BEGINNING)
                .grab(true, false).hint(super.convertHorizontalDLUsToPixels(notificationsLink,
                        IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH), SWT.DEFAULT)
                .create());
        notificationsLink.setText(getPreferenceName());
        notificationsLink.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent event) {
                PreferencesUtil.createPreferenceDialogOn(parent.getShell(), getLabelText(), null, null);
            }
        });
    }

    @Override
    protected void doLoad() {
    }

    @Override
    protected void doLoadDefault() {
    }

    @Override
    protected void doStore() {
    }

    @Override
    public int getNumberOfControls() {
        return 0;
    }
}
