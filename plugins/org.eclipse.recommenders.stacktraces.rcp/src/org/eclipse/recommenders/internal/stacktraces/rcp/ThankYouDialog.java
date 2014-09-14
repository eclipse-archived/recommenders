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

class ThankYouDialog extends org.eclipse.jface.dialogs.TitleAreaDialog {

    public static Image TITLE_IMAGE = ErrorReportWizard.TITLE_IMAGE_DESC.createImage();
    private ReportState state;

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
        newShell.setText("Thank you!");
        super.configureShell(newShell);
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        setTitle("Thank you!");
        setMessage("Your report has been received and is now tracked.");
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

        StringBuilder text = new StringBuilder();

        if (state.isCreated()) {
            text.append("Your report is now tracked at: \n\n    <a>")
                    .append(state.getBugUrl().or("invalid server response")).append("</a>.").append("\n\n")
                    .append("To be kept informed please add yourself to cc list of the bug report.");
        } else {
            boolean needsinfo = ArrayUtils.contains(state.getKeywords().or(EMPTY_STRINGS), KEYWORD_NEEDINFO);
            String status = state.getStatus().or(UNCONFIMRED);
            if (equals(status) || equals(NEW, status) || equals(ASSIGNED, status)) {
                if (needsinfo) {
                    text.append(
                            "Your report has been matched against an existing bug report and needs further information. ")
                            .append("Please take a moment to visit the bug and see whether you can provide more details:\n\n")
                            .append("    <a>").append(state.getBugUrl().or("invalid server response")).append("</a>.");
                } else {
                    text.append("Your report has been matched against an existing bug report.").append(
                            "To be kept informed please add yourself to cc list of the bug report.");
                }
            } else if (equals(RESOLVED, status) || equals(CLOSED, status)) {

                String resolution = state.getResolved().or(UNKNOWN);
                if (equals(FIXED, resolution)) {

                    text.append(
                            "Your error has been marked as 'fixed' already. Visit the bug report for further information:\n\n")
                            .append("    <a>").append(state.getBugUrl().or("invalid server response")).append("</a>.");
                } else if (equals(DUPLICATE, resolution)) {
                    text.append(
                            "Your error has been marked as 'duplicate' of another bug report. Please visit the bug report for further information:\n\n")
                            .append("    <a>").append(state.getBugUrl().or("invalid server response")).append("</a>.");

                } else if (equals(MOVED, resolution)) {
                    text.append(
                            "Your error has been marked as 'moved'. Please visit the bug report for further information:\n\n")
                            .append("    <a>").append(state.getBugUrl().or("invalid server response")).append("</a>.");

                } else if (equals(WORKSFORME, resolution)) {
                    text.append(
                            "The development team was not able to reproduce your error yet. Please take a moment to visit the bug and see whether you can provide more details to help us fixing it:\n\n")
                            .append("    <a>").append(state.getBugUrl().or("invalid server response")).append("</a>.");
                } else if (equals(WONTFIX, resolution) || equals(INVALID, resolution)
                        || equals(NOT_ECLIPSE, resolution)) {
                    text.append(
                            "The log event you sent has been marked as a 'normal' log message. If you think your report actually is an error, please comment on its bug report:\n\n")
                            .append("    <a>").append(state.getBugUrl().or("invalid server response")).append("</a>.");
                } else {
                    text.append(
                            "The log event you sent has been marked as a '"
                                    + resolution
                                    + "'. If you think your report actually is an error, please comment on its bug report:\n\n")
                            .append("    <a>").append(state.getBugUrl().or("invalid server response")).append("</a>.");
                }
            } else {
                text.append("Received an unknown server response. PLease raise a bug against the current version of this error reporter.");
            }
        }

        text.append("\n\nThank you for your help.");

        Link link = new Link(container, SWT.WRAP);
        link.setText(text.toString());
        link.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Browsers.openInExternalBrowser(state.getBugUrl().get());
            }
        });
        GridDataFactory.defaultsFor(link).align(GridData.FILL, GridData.BEGINNING).applyTo(link);
        // Label separator = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
        // separator.setLayoutData(GridDataFactory.swtDefaults().align(GridData.FILL, GridData.BEGINNING)
        // .grab(true, false).create());
        return container;
    }

    private boolean equals(String expected, String actual) {
        return StringUtils.equals(expected, actual);
    }
}
