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

import java.net.URL;

import org.eclipse.recommenders.utils.Urls;

public class Constants {

    /**
     * Bundle symbolic name of the o.e.r.rcp bundle.
     */
    public static final String BUNDLE_NAME = "org.eclipse.recommenders.rcp"; //$NON-NLS-1$

    /**
     * Date the plugin was first activated. Used determine the earliest time before the survey dialog may be displayed.
     */
    public static final String PREF_SURVEY_FIRST_ACTIVATION_DATE = "first_activation_date"; //$NON-NLS-1$

    /**
     * Number of plugin activations. Used determine when the survey dialog may be displayed.
     */
    public static final String PREF_SURVEY_NUMBER_OF_ACTIVATIONS = "activation_count"; //$NON-NLS-1$

    /**
     * Whether the user has elected not to take the survey.
     */
    public static final String PREF_SURVEY_OPT_OUT = "survey_opt_out"; //$NON-NLS-1$

    /**
     * Whether the survey dialog has already been displayed to the user.
     */
    public static final String PREF_SURVEY_TAKEN = "survey_already_displayed"; //$NON-NLS-1$

    public static final String PREF_UUID = "recommenders.uuid"; //$NON-NLS-1$

    /**
     * Number of plugin activations before survey dialog may be displayed.
     */
    public static final int SURVEY_ACTIVATIONS_BEFORE_SHOW_DIALOG = 20;

    /**
     * Number of minutes since first plugin activation before survey dialog may be displayed.
     */
    public static final long SURVEY_MILLIS_BEFORE_SHOW_DIALOG = DAYS.toMillis(7);

    /**
     * The ID of the Survey preference page
     */
    public static final String SURVEY_PREFERENCE_PAGE_ID = "org.eclipse.recommenders.rcp.survey.preferencepage"; //$NON-NLS-1$

    /**
     * Delay after which the show user survey popup job is run (after plugin activation). It should not run immediately
     * to block right on startup.
     */
    public static final long SURVEY_SHOW_DIALOG_JOB_DELAY_MILLIS = MINUTES.toMillis(5);

    /**
     * The URL of the survey.
     */
    public static final URL SURVEY_URL = Urls
            .toUrl("https://docs.google.com/a/codetrails.com/forms/d/1SqzZh1trpzS6UNEMjVWCvQTzGTBvjBFV-ZdwPuAwm5o/viewform"); //$NON-NLS-1$
}
