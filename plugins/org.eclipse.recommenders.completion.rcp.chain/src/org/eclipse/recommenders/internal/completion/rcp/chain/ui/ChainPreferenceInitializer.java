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
package org.eclipse.recommenders.internal.completion.rcp.chain.ui;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.recommenders.internal.completion.rcp.chain.ChainCompletionPlugin;

public final class ChainPreferenceInitializer extends AbstractPreferenceInitializer {

    @Override
    public void initializeDefaultPreferences() {
        final IPreferenceStore store = ChainCompletionPlugin.getDefault().getPreferenceStore();
        store.setDefault(ChainPreferencePage.ID_MAX_CHAINS, 20);
        store.setDefault(ChainPreferencePage.ID_MIN_DEPTH, 2);
        store.setDefault(ChainPreferencePage.ID_MAX_DEPTH, 4);
        store.setDefault(ChainPreferencePage.ID_TIMEOUT, 3);
        store.setDefault(ChainPreferencePage.ID_IGNORE_TYPES, "java.lang.Object"
                + ChainPreferencePage.IGNORE_TYPES_SEPARATOR + "java.lang.String");
    }
}
