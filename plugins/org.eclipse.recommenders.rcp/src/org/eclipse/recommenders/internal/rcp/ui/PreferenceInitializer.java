/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.ui;

import static org.eclipse.recommenders.rcp.RecommendersPlugin.P_REPOSITORY_ENABLE_AUTO_DOWNLOAD;
import static org.eclipse.recommenders.rcp.RecommendersPlugin.P_REPOSITORY_URL;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.recommenders.rcp.RecommendersPlugin;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

    private static final String SERVER_URL = "http://vandyk.st.informatik.tu-darmstadt.de/maven";

    @Override
    public void initializeDefaultPreferences() {
        final IPreferenceStore s = RecommendersPlugin.getDefault().getPreferenceStore();
        s.setDefault(P_REPOSITORY_URL, SERVER_URL);
        s.setDefault(P_REPOSITORY_ENABLE_AUTO_DOWNLOAD, true);
    }
}
