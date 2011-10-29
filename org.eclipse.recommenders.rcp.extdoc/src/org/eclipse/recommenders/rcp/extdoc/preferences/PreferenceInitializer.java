/**
 * Copyright (c) 2011 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.rcp.extdoc.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.recommenders.rcp.extdoc.ExtDocPlugin;

public final class PreferenceInitializer extends AbstractPreferenceInitializer {

    private static final String SERVER_URL = "http://recommenders1.st.informatik.tu-darmstadt.de/extdoc/";

    @Override
    public void initializeDefaultPreferences() {
        final IPreferenceStore preferenceStore = ExtDocPlugin.preferenceStore();

        preferenceStore.setDefault(PreferenceConstants.WEBSERVICE_HOST, SERVER_URL);
        preferenceStore.setDefault(PreferenceConstants.USERNAME, "Anonymous");
    }
}
