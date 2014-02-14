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

import static org.eclipse.recommenders.internal.rcp.Constants.SURVEY_ACTIVATIONS_BEFORE_SHOW_DIALOG;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.junit.Before;
import org.junit.Test;

import com.google.inject.Provider;

public class ShowSurveyDialogJobTest {

    private RcpPreferences prefs;
    private ShowSurveyDialogJob sut;

    @Before
    public void before() {
        prefs = new RcpPreferences();
        prefs.prefs = InstanceScope.INSTANCE.getNode("test");
        sut = new ShowSurveyDialogJob(prefs, mock(Provider.class));
    }

    @Test
    public void testSurveyTaken() {
        prefs.surveyTaken = true;
        prefs.surveyOptOut = false;
        prefs.numberOfActivations = SURVEY_ACTIVATIONS_BEFORE_SHOW_DIALOG + 1;
        prefs.firstActivationDate = 1L;

        assertFalse(sut.shouldRun());
    }

    @Test
    public void testSurveyOptOut() {
        prefs.surveyTaken = false;
        prefs.surveyOptOut = true;
        prefs.numberOfActivations = SURVEY_ACTIVATIONS_BEFORE_SHOW_DIALOG + 1;
        prefs.firstActivationDate = 1L;

        assertFalse(sut.shouldRun());
    }

    @Test
    public void testNotEnoughActivations() {
        prefs.surveyTaken = false;
        prefs.surveyOptOut = false;
        prefs.numberOfActivations = 0;
        prefs.firstActivationDate = 1L;

        assertFalse(sut.shouldRun());
    }

    @Test
    public void testNotEnoughTime() {
        prefs.surveyTaken = false;
        prefs.surveyOptOut = false;
        prefs.numberOfActivations = SURVEY_ACTIVATIONS_BEFORE_SHOW_DIALOG + 1;
        prefs.firstActivationDate = System.currentTimeMillis();

        assertFalse(sut.shouldRun());
    }

    @Test
    public void testShowDisplay() {
        prefs.surveyTaken = false;
        prefs.surveyOptOut = false;
        prefs.numberOfActivations = SURVEY_ACTIVATIONS_BEFORE_SHOW_DIALOG + 1;
        prefs.firstActivationDate = 1L;

        assertTrue(sut.shouldRun());
    }
}
