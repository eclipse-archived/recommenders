/**
 * Copyright (c) 2010, 2014 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Dorn - initial API and implementation.
 */
package org.eclipse.recommenders.internal.snipmatch.rcp;

import static java.text.MessageFormat.format;

import java.text.MessageFormat;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.recommenders.internal.snipmatch.rcp.l10n.Messages;
import org.eclipse.recommenders.utils.rcp.Browsers;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;

public class BranchCheckoutFailureDialog extends MessageDialog {

    private static final String RECOMMENDERS_FAQ_URL = "http://www.eclipse.org/recommenders/manual/#snippet-repository-update-guide"; //$NON-NLS-1$

    public BranchCheckoutFailureDialog(Shell parentShell, String repository, String failedVersion,
            String substituteVersion) {
        super(parentShell, Messages.DIALOG_TITLE_BRANCH_CHECKOUT_FAILURE, null,
                format(Messages.DIALOG_MESSAGE_BRANCH_CHECKOUT_FAILURE, repository, failedVersion, substituteVersion),
                MessageDialog.ERROR, new String[] { IDialogConstants.OK_LABEL }, 0);
    }

    public BranchCheckoutFailureDialog(Shell parentShell, String repository, String failedVersion) {
        super(parentShell, Messages.DIALOG_TITLE_BRANCH_CHECKOUT_FAILURE, null,
                format(Messages.DIALOG_MESSAGE_NO_FORMAT_BRANCH_FAILURE, repository, failedVersion),
                MessageDialog.ERROR, new String[] { IDialogConstants.OK_LABEL }, 0);
    }

    @Override
    protected Control createCustomArea(Composite parent) {
        addLink(parent, Messages.DIALOG_MESSAGE_BRANCH_CHECKOUT_FAILURE_LINK, RECOMMENDERS_FAQ_URL);
        return parent;
    }

    private void addLink(Composite parent, String text, String url) {
        Link link = new Link(parent, SWT.BEGINNING);
        link.setText(MessageFormat.format(text, url));
        link.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                Browsers.tryOpenInExternalBrowser(event.text);
            }
        });
    }
}
