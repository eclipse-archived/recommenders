/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Dorn, Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp;

import static java.util.concurrent.TimeUnit.*;
import static org.eclipse.recommenders.rcp.utils.PreferencesHelper.createLinkLabelToPreferencePage;

import java.net.URL;

import org.eclipse.recommenders.utils.Urls;

public class Constants {

    /**
     * Bundle symbolic name of the o.e.r.rcp bundle.
     */
    public static final String BUNDLE_NAME = "org.eclipse.recommenders.rcp";

    /**
     * Date the plugin was first activated. Used determine the earliest time before the survey dialog may be displayed.
     */
    public static final String P_SURVEY_FIRST_ACTIVATION_DATE = "first_activation_date";

    /**
     * Number of plugin activations. Used determine when the survey dialog may be displayed.
     */
    public static final String P_SURVEY_NUMBER_OF_ACTIVATIONS = "activation_count";

    /**
     * Whether the user has elected not to take the survey.
     */
    public static final String P_SURVEY_OPT_OUT = "survey_opt_out";

    /**
     * Whether the survey dialog has already been displayed to the user.
     */
    public static final String P_SURVEY_TAKEN = "survey_already_displayed";

    /**
     * Number of plugin activations before survey dialog may be displayed.
     */
    public static final int SURVEY_ACTIVATIONS_BEFORE_SHOW_DIALOG = 20;

    /**
     * The description of the survey.
     */
    public static final String SURVEY_DESCRIPTION = "To help us develop Code Recommenders to your needs, we ask you for a few moments of your time to fill out our user survey.";

    /**
     * Number of minutes since first plugin activation before survey dialog may be displayed.
     */
    public static final long SURVEY_MILLIS_BEFORE_SHOW_DIALOG = DAYS.toMillis(7);

    /**
     * The ID of the Survey preference page
     */
    public static final String SURVEY_PREFERENCE_PAGE_ID = "org.eclipse.recommenders.rcp.survey.preferencepage";

    /**
     * A hint to inform the user to go to the preferences if he wants to take the survey later.
     */
    public static final String SURVEY_PREFERENCES_HINT = "This dialog will not appear again. If you want to take the survey at a later date, you can always go to <a>"
            + createLinkLabelToPreferencePage(SURVEY_PREFERENCE_PAGE_ID) + "</a>. Do you want to take the survey now?";

    /**
     * Delay after which the show user survey popup job is run (after plugin activation). It should not run immediately
     * to block right on startup.
     */
    public static final long SURVEY_SHOW_DIALOG_JOB_DELAY_MINUTES = MINUTES.toMillis(5);

    /**
     * The URL of the survey.
     */
    public static final URL SURVEY_URL = Urls
            .toUrl("https://docs.google.com/a/codetrails.com/forms/d/1SqzZh1trpzS6UNEMjVWCvQTzGTBvjBFV-ZdwPuAwm5o/viewform");

}
