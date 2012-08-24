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
package org.eclipse.recommenders.tests.completion.rcp.chain;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.recommenders.internal.completion.rcp.chain.ui.ChainPreferencePage;
import org.mockito.Mockito;

public class ChainPreferenceStoreMock {

    public static IPreferenceStore create() {
        final IPreferenceStore store = Mockito.mock(IPreferenceStore.class);
        Mockito.when(store.getInt(ChainPreferencePage.ID_MAX_CHAINS)).thenReturn(20);
        Mockito.when(store.getInt(ChainPreferencePage.ID_MAX_DEPTH)).thenReturn(4);
        Mockito.when(store.getInt(ChainPreferencePage.ID_TIMEOUT)).thenReturn(3);
        Mockito.when(store.getInt(ChainPreferencePage.ID_MIN_DEPTH)).thenReturn(1);
        Mockito.when(store.getString(ChainPreferencePage.ID_IGNORE_TYPES)).thenReturn(
                "java.lang.Object" + ChainPreferencePage.IGNORE_TYPES_SEPARATOR + "java.lang.String");
        return store;
    }

}
