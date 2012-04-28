/**
 * Copyright (c) 2011 Doug Wightman, Zi Ye, Cheng Chen
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Cheng Chen - initial API and implementation.
 */
package org.eclipse.recommenders.snipmatch.preferences;

import java.io.File;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.recommenders.snipmatch.rcp.SnipMatchPlugin;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
     */
    public void initializeDefaultPreferences() {
        IPreferenceStore store = SnipMatchPlugin.getDefault().getPreferenceStore();
        String defaultPath = System.getProperty("user.home");
        File snipmatchDefaultPath = new File(defaultPath, "/.eclipse/recommenders/snippets/");
        if (!snipmatchDefaultPath.exists() || !snipmatchDefaultPath.isDirectory()) {
            boolean success = snipmatchDefaultPath.mkdirs();
            if (success) {
                defaultPath = snipmatchDefaultPath.getAbsolutePath();
            }
        } else
            defaultPath = snipmatchDefaultPath.getAbsolutePath();

        store.setDefault(PreferenceConstants.SEARCH_MODEL, PreferenceConstants.SEARCH_MODEL_LOCAL);

        store.setDefault(PreferenceConstants.SNIPPETS_STORE_DIR, defaultPath);
        store.setDefault(PreferenceConstants.SNIPPETS_INDEX_FILE,
                new File(defaultPath, "simpleIndex.txt").getAbsolutePath());
        store.setDefault(PreferenceConstants.SNIPPETS_DIR_USE_DEFAULT, true);
    }

}
