/**
 * Copyright (c) 2011 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henß - initial API and implementation.
 */
package org.eclipse.recommenders.tests.completion.rcp.calls;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.recommenders.internal.completion.rcp.calls.preferences.CallPreferencePage;
import org.mockito.Mockito;

public class CallsPreferenceStoreMock {

    public static IPreferenceStore create() {
        final IPreferenceStore store = Mockito.mock(IPreferenceStore.class);
        Mockito.when(store.getInt(CallPreferencePage.ID_MAX_PROPOSALS)).thenReturn(7);
        Mockito.when(store.getInt(CallPreferencePage.ID_MIN_PROBABILITY)).thenReturn(1);
        return store;
    }

}
