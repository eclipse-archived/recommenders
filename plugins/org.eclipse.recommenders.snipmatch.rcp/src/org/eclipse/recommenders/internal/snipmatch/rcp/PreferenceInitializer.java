/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.snipmatch.rcp;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

    private static final String REPO_FETCH_URL = "https://git.eclipse.org/gitroot/recommenders/org.eclipse.recommenders.snipmatch.snippets.git"; //$NON-NLS-1$
    private static final String REPO_PUSH_URL = "https://git.eclipse.org/r/recommenders/org.eclipse.recommenders.snipmatch.snippets.git"; //$NON-NLS-1$
    private static final String REPO_PUSH_BRANCH = "refs/for"; //$NON-NLS-1$

    @Override
    public void initializeDefaultPreferences() {
        ScopedPreferenceStore store = new ScopedPreferenceStore(DefaultScope.INSTANCE, Constants.BUNDLE_ID);
        store.setDefault(Constants.PREF_SNIPPETS_REPO_FETCH_URL, REPO_FETCH_URL);
        store.setDefault(Constants.PREF_SNIPPETS_REPO_PUSH_URL, REPO_PUSH_URL);
        store.setDefault(Constants.PREF_SNIPPETS_REPO_PUSH_BRANCH, REPO_PUSH_BRANCH);
    }
}
