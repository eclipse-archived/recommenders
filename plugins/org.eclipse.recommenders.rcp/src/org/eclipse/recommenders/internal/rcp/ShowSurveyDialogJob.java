/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Johannes Dorn, Marcel Bruch - initial API and implementation
 */
package org.eclipse.recommenders.internal.rcp;

import static java.text.MessageFormat.format;
import static org.eclipse.recommenders.internal.rcp.Constants.SURVEY_ACTIVATIONS_BEFORE_SHOW_DIALOG;
import static org.eclipse.recommenders.internal.rcp.Constants.SURVEY_MILLIS_BEFORE_SHOW_DIALOG;
import static org.eclipse.recommenders.internal.rcp.Constants.SURVEY_PREFERENCE_PAGE_ID;
import static org.eclipse.recommenders.internal.rcp.Constants.SURVEY_URL;
import static org.eclipse.recommenders.rcp.utils.PreferencesHelper.createLinkLabelToPreferencePage;

import javax.inject.Inject;
import javax.inject.Provider;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.progress.UIJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ShowSurveyDialogJob extends UIJob {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final RcpPreferences prefs;
    private final Provider<IWebBrowser> browser;

    @Inject
    public ShowSurveyDialogJob(RcpPreferences prefs, Provider<IWebBrowser> browser) {
        super(""); //$NON-NLS-1$
        this.prefs = prefs;
        this.browser = browser;
        setSystem(true);
    }

    @Override
    public boolean shouldRun() {
        if (surveyAlreadyTakenOrOptOut()) {
            return false;
        }
        return enoughActivationsForSurvey() && enoughTimeForActiviation();
    }

    private boolean surveyAlreadyTakenOrOptOut() {
        return prefs.surveyTaken || prefs.surveyOptOut;
    }

    private boolean enoughActivationsForSurvey() {
        int numberOfActivations = prefs.numberOfActivations;
        prefs.setNumberOfActivations(++numberOfActivations);
        return numberOfActivations >= SURVEY_ACTIVATIONS_BEFORE_SHOW_DIALOG;
    }

    private boolean enoughTimeForActiviation() {
        long currentTime = System.currentTimeMillis();
        long firstActivationDate = prefs.firstActivationDate;
        long timeSinceFirstActivation = currentTime - firstActivationDate;
        return timeSinceFirstActivation > SURVEY_MILLIS_BEFORE_SHOW_DIALOG;
    }

    @Override
    public IStatus runInUIThread(IProgressMonitor monitor) {
        new PreferenceLinkDialog(getDisplay().getActiveShell(), Messages.DIALOG_TITLE_SURVEY, null,
                Messages.DIALOG_MESSAGE_SURVEY, MessageDialog.QUESTION, new String[] { Messages.BUTTON_LABEL_YES,
                        Messages.BUTTON_LABEL_NO }, 0, format(Messages.LINK_LABEL_TAKE_SURVEY_LATER,
                        createLinkLabelToPreferencePage(SURVEY_PREFERENCE_PAGE_ID)), SURVEY_PREFERENCE_PAGE_ID) {

            @Override
            protected void buttonPressed(int buttonId) {
                if (buttonId == 0) {
                    prefs.setSurveyTaken(true);
                    try {
                        browser.get().openURL(SURVEY_URL);
                    } catch (PartInitException e) {
                        log.error("Exception occured while opening survey dialog", e); //$NON-NLS-1$
                    }
                } else {
                    prefs.setSurveyOptOut(true);
                }
                super.buttonPressed(buttonId);
            }
        }.open();
        return Status.OK_STATUS;
    }
}
