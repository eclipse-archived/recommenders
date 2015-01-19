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
package org.eclipse.recommenders.internal.chain.rcp;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

public final class ChainPreferenceInitializer extends AbstractPreferenceInitializer {

    @Override
    public void initializeDefaultPreferences() {
        final IPreferenceStore store = ChainRcpPlugin.getDefault().getPreferenceStore();
        store.setDefault(ChainsPreferencePage.PREF_MAX_CHAINS, 20);
        store.setDefault(ChainsPreferencePage.PREF_MIN_CHAIN_LENGTH, 2);
        store.setDefault(ChainsPreferencePage.PREF_MAX_CHAIN_LENGTH, 4);
        store.setDefault(ChainsPreferencePage.PREF_TIMEOUT, 3);
        store.setDefault(ChainsPreferencePage.PREF_IGNORED_TYPES, "java.lang.Object" //$NON-NLS-1$
                + ChainsPreferencePage.IGNORE_TYPES_SEPARATOR + "java.lang.Class" //$NON-NLS-1$
                + ChainsPreferencePage.IGNORE_TYPES_SEPARATOR + "java.lang.String"); //$NON-NLS-1$
        store.setDefault(ChainsPreferencePage.PREF_ENABLE_QUICK_ASSIST_CHAINS, false);
    }
}
