/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marcel Bruch - initial API and implementation
 */
package org.eclipse.recommenders.internal.rcp;

import static org.eclipse.recommenders.internal.rcp.Constants.*;

import javax.inject.Inject;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.di.extensions.Preference;

public class RcpPreferences {

    @Inject
    @Preference
    protected IEclipsePreferences prefs;

    @Inject
    @Preference(PREF_SURVEY_TAKEN)
    public boolean surveyTaken;

    @Inject
    @Preference(PREF_SURVEY_FIRST_ACTIVATION_DATE)
    public long firstActivationDate;

    @Inject
    @Preference(PREF_SURVEY_OPT_OUT)
    public boolean surveyOptOut;

    @Inject
    @Preference(PREF_SURVEY_NUMBER_OF_ACTIVATIONS)
    public int numberOfActivations;

    public void setSurveyOptOut(boolean value) {
        prefs.putBoolean(PREF_SURVEY_OPT_OUT, value);
    }

    public void setNumberOfActivations(int value) {
        prefs.putInt(PREF_SURVEY_NUMBER_OF_ACTIVATIONS, value);
    }

    public void setSurveyTaken(boolean value) {
        prefs.putBoolean(PREF_SURVEY_TAKEN, value);
    }

    public void setFirstActivationDate(long date) {
        prefs.putLong(PREF_SURVEY_FIRST_ACTIVATION_DATE, date);
    }

}
