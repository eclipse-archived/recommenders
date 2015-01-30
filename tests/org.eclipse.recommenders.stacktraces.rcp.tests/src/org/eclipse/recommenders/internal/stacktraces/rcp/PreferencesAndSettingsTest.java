/**
 * Copyright (c) 2014 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Daniel Haftstein - initial tests.
 */
package org.eclipse.recommenders.internal.stacktraces.rcp;

import static org.eclipse.recommenders.internal.stacktraces.rcp.Constants.PLUGIN_ID;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.SendAction;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.Settings;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.junit.Before;
import org.junit.Test;

public class PreferencesAndSettingsTest {

    private Settings settings;
    private ScopedPreferenceStore store;

    @Before
    public void setUp() {
        settings = PreferenceInitializer.getDefault();
        store = new ScopedPreferenceStore(InstanceScope.INSTANCE, PLUGIN_ID);
    }

    @Test
    public void testSettingsUpdateStore() {
        String sendActionBefore = store.getString(Constants.PROP_SEND_ACTION);
        settings.setAction(SendAction.IGNORE);
        assertThat(store.getString(Constants.PROP_SEND_ACTION), not(is(sendActionBefore)));
    }

    @Test
    public void testStoreUpdateSettings() {
        SendAction sendActionBefore = settings.getAction();
        store.setValue(Constants.PROP_SEND_ACTION, SendAction.ASK.name());
        assertThat(settings.getAction(), not(is(sendActionBefore)));
    }

    @Test
    public void testSameSettingsInstance() {
        assertThat(settings, is(sameInstance(PreferenceInitializer.getDefault())));
    }

}
