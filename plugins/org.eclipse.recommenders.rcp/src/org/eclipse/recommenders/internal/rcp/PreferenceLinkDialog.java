/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Sewe - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.google.common.base.Preconditions;

public class PreferenceLinkDialog extends MessageDialog {

    private final String linkText;
    private final String preferencePageId;

    public PreferenceLinkDialog(Shell parent, String title, Image titleImage, String message, int imageType,
            String[] buttonLabels, int defaultIndex, String linkText, String preferencePageId) {
        super(parent, title, titleImage, message, imageType, buttonLabels, defaultIndex);
        Preconditions.checkNotNull(linkText);
        Preconditions.checkNotNull(preferencePageId);
        this.linkText = linkText;
        this.preferencePageId = preferencePageId;
    }

    /*
     * Adapted from org.eclipse.jdt.internal.ui.text.java.CompletionProposalComputerRegistry#informUser.
     */
    @Override
    protected Control createCustomArea(Composite parent) {
        Link link = new Link(parent, SWT.NONE);
        link.setText(linkText);
        link.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                cancelPressed();
                PreferencesUtil.createPreferenceDialogOn(getShell(), preferencePageId, null, null).open();
            }
        });
        link.setLayoutData(GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).grab(true, false)
                .hint(getMinimumMessageWidth(), SWT.DEFAULT).create());
        return link;
    }
}
