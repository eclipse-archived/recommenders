/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Dorn - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp;

import static org.eclipse.recommenders.internal.rcp.Constants.*;

import javax.inject.Inject;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SurveyPreferencePage extends org.eclipse.jface.preference.PreferencePage implements
        IWorkbenchPreferencePage {

    private static final Logger LOG = LoggerFactory.getLogger(SurveyPreferencePage.class);

    private final RcpPreferences prefs;

    @Inject
    public SurveyPreferencePage(RcpPreferences prefs) {
        this.prefs = prefs;
    }

    @Override
    public void init(IWorkbench workbench) {
        setDescription(Messages.DIALOG_MESSAGE_SURVEY);
    }

    @Override
    protected Control createContents(final Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(GridLayoutFactory.swtDefaults().margins(0, 0).create());
        Link surveyLink = new Link(container, SWT.NONE | SWT.WRAP);
        surveyLink
                .setLayoutData(GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BEGINNING).grab(true, false)
                        .hint(convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH), SWT.DEFAULT)
                        .create());
        surveyLink.setText(Messages.LINK_LABEL_TAKE_SURVEY);
        surveyLink.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent event) {
                try {
                    IWebBrowser browser = PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser();
                    browser.openURL(SURVEY_URL);
                    prefs.setSurveyTaken(true);
                } catch (PartInitException e) {
                    LOG.error("Failed to open browser for taking the survey", e); //$NON-NLS-1$
                }
            }
        });
        return container;
    }
}
