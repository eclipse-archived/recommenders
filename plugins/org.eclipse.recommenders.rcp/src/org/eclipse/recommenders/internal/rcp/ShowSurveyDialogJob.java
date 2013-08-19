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

import static org.eclipse.recommenders.internal.rcp.Constants.*;

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

    Logger log = LoggerFactory.getLogger(getClass());

    @Inject
    RcpPreferences prefs;

    @Inject
    Provider<IWebBrowser> browser;

    public ShowSurveyDialogJob() {
        super("");
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
        prefs.setFirstActivationDate(firstActivationDate);
        long timeSinceFirstActivation = currentTime - firstActivationDate;
        return timeSinceFirstActivation > SURVEY_MILLIS_BEFORE_SHOW_DIALOG;
    }

    @Override
    public IStatus runInUIThread(IProgressMonitor monitor) {
        new PreferenceLinkDialog(getDisplay().getActiveShell(), RcpPlugin.DIALOG_TITLE, null, SURVEY_DESCRIPTION,
                MessageDialog.QUESTION, new String[] { "Yes, Take the survey", "No, Thank you" }, 0,
                SURVEY_PREFERENCES_HINT, SURVEY_PREFERENCE_PAGE_ID) {

            @Override
            protected void buttonPressed(int buttonId) {
                if (buttonId == 0) {
                    prefs.setSurveyTaken(true);
                    try {
                        browser.get().openURL(SURVEY_URL);
                    } catch (PartInitException e) {
                        log.error("Exception occured while opening survey dialog", e);
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
