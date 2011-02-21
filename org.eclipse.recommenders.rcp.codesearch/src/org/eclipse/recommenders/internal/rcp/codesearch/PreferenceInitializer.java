/**
 * Copyright (c) 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.codesearch;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

    public static String getServerURL() {
        return CodesearchPlugin.getDefault().getPreferenceStore().getString(SERVER_URL);
    }

    public static final String SERVER_URL = "server.url";

    @Override
    public void initializeDefaultPreferences() {
        final IPreferenceStore store = CodesearchPlugin.getDefault().getPreferenceStore();
        store.setDefault(SERVER_URL, "http://vandyk.st.informatik.tu-darmstadt.de:9090/codesearch");
    }
}
