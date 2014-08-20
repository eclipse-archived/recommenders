/**
 * Copyright (c) 2010, 2014 Darmstadt University of Technology. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Johannes Dorn - initial API and implementation.
 */
package org.eclipse.recommenders.internal.completion.rcp;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.util.Collections;
import java.util.Set;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.recommenders.completion.rcp.processable.SessionProcessorDescriptor;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

@SuppressWarnings("restriction")
public class CompletionRcpPreferencesTest {

    private static final SessionProcessorDescriptor FIRST_DESCRIPTOR = new SessionProcessorDescriptor("first", "name",
            "description", null, 1, true, null, null);

    private static final SessionProcessorDescriptor SECOND_DESCRIPTOR = new SessionProcessorDescriptor("second",
            "name", "description", null, 2, false, null, null);

    private IPreferenceStore store;

    private CompletionRcpPreferences sut;

    @Before
    public void setUp() {
        sut = new CompletionRcpPreferences(ImmutableSet.of(FIRST_DESCRIPTOR, SECOND_DESCRIPTOR));

        store = new ScopedPreferenceStore(InstanceScope.INSTANCE, Constants.BUNDLE_NAME);
        String preferenceString = "first;!second";
        store.setValue(Constants.PREF_SESSIONPROCESSORS, preferenceString);
        sut.setEnabledSessionProcessorString(preferenceString);
    }

    @Test
    public void testAvailableProcessors() {
        Set<SessionProcessorDescriptor> availableSessionProcessors = sut.getAvailableSessionProcessors();
        assertThat(availableSessionProcessors, hasItems(FIRST_DESCRIPTOR, SECOND_DESCRIPTOR));
        assertThat(availableSessionProcessors.size(), is(2));
    }

    @Test
    public void testEnabledProcessors() {
        Set<SessionProcessorDescriptor> enabledSessionProcessors = sut.getEnabledSessionProcessors();
        assertThat(enabledSessionProcessors, hasItems(FIRST_DESCRIPTOR));
        assertThat(enabledSessionProcessors.size(), is(1));
    }

    @Test
    public void testEnabledProcessorsNotInPreferencesString() {
        sut.setEnabledSessionProcessorString("second");
        Set<SessionProcessorDescriptor> enabledSessionProcessors = sut.getEnabledSessionProcessors();
        assertThat(enabledSessionProcessors, hasItems(FIRST_DESCRIPTOR, SECOND_DESCRIPTOR));
        assertThat(enabledSessionProcessors.size(), is(2));
    }

    @Test
    public void testGetProcessor() {
        SessionProcessorDescriptor foundDescriptor = sut.getSessionProcessorDescriptor(FIRST_DESCRIPTOR.getId());
        assertThat(foundDescriptor, is(equalTo(FIRST_DESCRIPTOR)));

        SessionProcessorDescriptor foundDescriptor2 = sut.getSessionProcessorDescriptor(SECOND_DESCRIPTOR.getId());
        assertThat(foundDescriptor2, is(equalTo(SECOND_DESCRIPTOR)));
    }

    @Test
    public void testSetSessionProcessorEnabled() {
        sut.setSessionProcessorEnabled(ImmutableList.of(SECOND_DESCRIPTOR),
                Collections.<SessionProcessorDescriptor>emptyList());

        sut.setEnabledSessionProcessorString(store.getString(Constants.PREF_SESSIONPROCESSORS));

        Set<SessionProcessorDescriptor> enabledSessionProcessors = sut.getEnabledSessionProcessors();

        assertThat(enabledSessionProcessors, hasItems(FIRST_DESCRIPTOR, SECOND_DESCRIPTOR));
        assertThat(enabledSessionProcessors.size(), is(2));
        assertThat(sut.isEnabled(FIRST_DESCRIPTOR), is(true));
        assertThat(sut.isEnabled(SECOND_DESCRIPTOR), is(true));
    }

    @Test
    public void testSetSessionProcessorDisabled() {
        sut.setSessionProcessorEnabled(Collections.<SessionProcessorDescriptor>emptyList(),
                ImmutableList.of(FIRST_DESCRIPTOR));

        sut.setEnabledSessionProcessorString(store.getString(Constants.PREF_SESSIONPROCESSORS));

        Set<SessionProcessorDescriptor> enabledSessionProcessors = sut.getEnabledSessionProcessors();

        assertThat(enabledSessionProcessors.isEmpty(), is(true));
        assertThat(sut.isEnabled(FIRST_DESCRIPTOR), is(false));
        assertThat(sut.isEnabled(SECOND_DESCRIPTOR), is(false));
    }

    @Test
    public void testInvertSessionProcessorEnablement() {
        sut.setSessionProcessorEnabled(ImmutableList.of(SECOND_DESCRIPTOR), ImmutableList.of(FIRST_DESCRIPTOR));

        sut.setEnabledSessionProcessorString(store.getString(Constants.PREF_SESSIONPROCESSORS));

        Set<SessionProcessorDescriptor> enabledSessionProcessors = sut.getEnabledSessionProcessors();

        assertThat(enabledSessionProcessors, hasItems(SECOND_DESCRIPTOR));
        assertThat(enabledSessionProcessors.size(), is(1));
        assertThat(sut.isEnabled(FIRST_DESCRIPTOR), is(false));
        assertThat(sut.isEnabled(SECOND_DESCRIPTOR), is(true));
    }
}
