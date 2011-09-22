/**
 * Copyright (c) 2011 Andreas Frankenberger.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API and implementation.
 */
package org.eclipse.recommenders.internal.udc.ui.wizard.uploadrequest;

/**
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API and implementation.
 */
import java.net.URL;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.recommenders.internal.udc.Activator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.PlatformUI;

public class SelectActionPage extends WizardPage {
    private static final String recommendersProjectUrl = "http://www.eclipse.org/recommenders/";
    private static final int PREFERRED_WIDTH = 500;

    enum Action {
        UploadNow, UploadAlways, DontUpload, UploadNever;
    }

    SelectionListener updateButtonsSelectionListener = new SelectionAdapter() {
        @Override
        public void widgetSelected(final org.eclipse.swt.events.SelectionEvent e) {
            SelectActionPage.this.getWizard().getContainer().updateButtons();
        };
    };

    private Button uploadNowButton;
    private Button uploadAlwaysButton;
    private Button dontUploadButton;
    private Button uploadNeverButton;

    protected SelectActionPage() {
        super("dummyname");
        setTitle("Code Recommenders data upload");
        setMessage("It's time to share some knowledge");
    }

    @Override
    public void createControl(final Composite parent) {
        final Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);
        composite.setLayout(new GridLayout(1, false));

        createInformationTextSection(composite);
        createUploadNowSection(composite);
        createUploadAlwaysSection(composite);
        createDontUploadSection(composite);
        createUploadNeverSection(composite);
    }

    private void createInformationTextSection(final Composite composite) {
        final Link lblNewLabel = new Link(composite, SWT.WRAP);
        lblNewLabel
                .setText("To improve <a>Code Recommenders</a> for existing and new frameworks "
                        + "we need your assistance. To learn about which data is shared see the <a>privacy statement</a> pages.");
        lblNewLabel.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).hint(PREFERRED_WIDTH, SWT.DEFAULT)
                .create());
        lblNewLabel.addSelectionListener(createLinkClickedListener());
    }

    private SelectionListener createLinkClickedListener() {
        return new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                try {
                    PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser()
                            .openURL(new URL(recommendersProjectUrl));
                } catch (final Exception e1) {
                    Activator.getDefault().getLog()
                            .log(new Status(Status.ERROR, Activator.PLUGIN_ID, "Error opening browser", e1));
                }
            }
        };
    }

    private void createUploadNeverSection(final Composite composite) {
        uploadNeverButton = createButton(composite, "Upload Never");

        final String text = "Never upload the usage data.";
        createDescriptionLabel(composite, text);
    }

    private void createDescriptionLabel(final Composite composite, final String text) {
        final Label label = new Label(composite, SWT.WRAP);
        final GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        gd.horizontalIndent = 16;
        gd.widthHint = PREFERRED_WIDTH;
        label.setLayoutData(gd);
        label.setText(text);
    }

    private void createDontUploadSection(final Composite composite) {
        dontUploadButton = createButton(composite, "Don't Upload");

        createDescriptionLabel(composite, "Do not upload usage data at this time. "
                + "You will be asked to do the upload later.");
    }

    private void createUploadAlwaysSection(final Composite composite) {
        uploadAlwaysButton = createButton(composite, "Upload Always");

        createDescriptionLabel(composite,
                "Upload the usage data now. Don't ask next time: just do the upload in the background. "
                        + "Note that you can change this setting the preferences.");
    }

    private void createUploadNowSection(final Composite composite) {
        uploadNowButton = createButton(composite, "Upload Now");
        uploadNowButton.addSelectionListener(updateButtonsSelectionListener);

        createDescriptionLabel(composite, "Upload the usage data now. Ask before uploading again.");
    }

    private Button createButton(final Composite parent, final String text) {
        final Button button = new Button(parent, SWT.RADIO);
        final GridData gd_uploadNowButton = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_uploadNowButton.verticalIndent = 5;
        button.setLayoutData(gd_uploadNowButton);
        button.setText(text);
        button.addSelectionListener(updateButtonsSelectionListener);
        return button;
    }

    public Action getAction() {
        if (uploadNeverButton.getSelection()) {
            return Action.UploadNever;
        }
        if (dontUploadButton.getSelection()) {
            return Action.DontUpload;
        }
        if (uploadAlwaysButton.getSelection()) {
            return Action.UploadAlways;
        }
        return Action.UploadNow;
    }

}
