/**
 * Copyright (c) 2014 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial implementation
 */
package org.eclipse.recommenders.internal.stacktraces.rcp;

import static org.eclipse.recommenders.internal.stacktraces.rcp.ReportState.*;

import java.text.MessageFormat;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;

import com.google.common.annotations.VisibleForTesting;

class ThankYouDialog extends org.eclipse.jface.dialogs.TitleAreaDialog {

    public static final Image TITLE_IMAGE = ErrorReportWizard.TITLE_IMAGE_DESC.createImage();

    private final ReportState state;

    ThankYouDialog(Shell parentShell, ReportState state) {
        super(parentShell);
        this.state = state;
        setHelpAvailable(false);
    }

    @Override
    protected boolean isResizable() {
        return true;
    }

    @Override
    protected void configureShell(Shell newShell) {
        newShell.setText(Messages.THANKYOUDIALOG_THANK_YOU);
        super.configureShell(newShell);
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        setTitle(Messages.THANKYOUDIALOG_THANK_YOU);
        setMessage(Messages.THANKYOUDIALOG_RECEIVED_AND_TRACKED);
        setTitleImage(TITLE_IMAGE);

        Label linetop = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
        linetop.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        Composite border = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        border.setLayout(layout);
        border.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());

        Label linebottom = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
        linebottom.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        Composite container = new Composite(border, SWT.NONE);
        container.setLayout(GridLayoutFactory.swtDefaults().create());
        container.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());

        String text = buildText();

        Link link = new Link(container, SWT.WRAP);
        link.setText(text);
        link.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Browsers.openInExternalBrowser(state.getBugUrl().get());
            }
        });
        GridDataFactory.defaultsFor(link).align(GridData.FILL, GridData.BEGINNING).applyTo(link);
        return container;
    }

    @VisibleForTesting
    protected String buildText() {
        StringBuilder text = new StringBuilder();

        if (state.isCreated()) {
            String message = MessageFormat.format(Messages.THANKYOUDIALOG_TRACKED_PLEASE_ADD_TO_CC, getBugURL());
            text.append(message);
        } else {
            String status = state.getStatus().or(UNCONFIRMED);
            if (equals(UNCONFIRMED, status) || equals(NEW, status) || equals(ASSIGNED, status)) {
                text.append(MessageFormat.format(Messages.THANKYOUDIALOG_MATCHED_PLEASE_ADD_TO_CC, getBugURL()));
            } else if (equals(RESOLVED, status) || equals(CLOSED, status)) {

                String resolution = state.getResolved().or(UNKNOWN);
                if (equals(FIXED, resolution)) {
                    text.append(MessageFormat.format(Messages.THANKYOUDIALOG_MARKED_FIXED, getBugURL()));
                } else if (equals(DUPLICATE, resolution)) {
                    text.append(MessageFormat.format(Messages.THANKYOUDIALOG_MARKED_DUPLICATE, getBugURL()));

                } else if (equals(MOVED, resolution)) {
                    text.append(MessageFormat.format(Messages.THANKYOUDIALOG_MARKED_MOVED, getBugURL()));

                } else if (equals(WORKSFORME, resolution)) {
                    text.append(MessageFormat.format(Messages.THANKYOUDIALOG_NOT_ABLE_TO_REPRODUCE_PLEASE_VISIT,
                            getBugURL()));
                } else if (equals(WONTFIX, resolution) || equals(INVALID, resolution)
                        || equals(NOT_ECLIPSE, resolution)) {
                    text.append(MessageFormat.format(Messages.THANKYOUDIALOG_MARKED_NORMAL, getBugURL()));
                } else {
                    text.append(MessageFormat.format(Messages.THANKYOUDIALOG_MARKED_UNKNOWN, resolution, getBugURL()));
                }
            } else {
                text.append(Messages.THANKYOUDIALOG_RECEIVED_UNKNOWN_SERVER_RESPONSE);
            }
        }

        boolean needsinfo = ArrayUtils.contains(state.getKeywords().or(EMPTY_STRINGS), KEYWORD_NEEDINFO);
        if (needsinfo) {
            text.append(Messages.THANKYOUDIALOG_MATCHED_NEED_FURTHER_INFORMATION);
            text.append(MessageFormat.format(Messages.THANKYOUDIALOG_FURTHER_INFORMATION,
                    state.getInformation().or(Messages.THANKYOUDIALOG_NO_FURTHER_INFORMATIONS)));
        }

        text.append(Messages.THANKYOUDIALOG_PLEASE_NOTE_ADDITIONAL_PERMISSIONS);
        text.append(Messages.THANKYOUDIALOG_THANK_YOU_FOR_HELP);
        return text.toString();
    }

    private String getBugURL() {
        return state.getBugUrl().or(Messages.THANKYOUDIALOG_INVALID_SERVER_RESPONSE);
    }

    private static boolean equals(String expected, String actual) {
        return StringUtils.equals(expected, actual);
    }
}
