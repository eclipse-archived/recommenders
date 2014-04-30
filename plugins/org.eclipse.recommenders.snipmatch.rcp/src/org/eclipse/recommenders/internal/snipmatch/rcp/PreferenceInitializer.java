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

    private static final String REPO_URL = "http://git.eclipse.org/gitroot/recommenders.incubator/org.eclipse.recommenders.snipmatch.snippets.git";

    @Override
    public void initializeDefaultPreferences() {
        ScopedPreferenceStore store = new ScopedPreferenceStore(DefaultScope.INSTANCE, Constants.BUNDLE_ID);
        store.setDefault(Constants.P_SNIPPETS_REPO, REPO_URL);
    }
}
