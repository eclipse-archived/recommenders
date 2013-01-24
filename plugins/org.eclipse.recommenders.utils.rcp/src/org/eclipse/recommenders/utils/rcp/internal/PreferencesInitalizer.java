/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.utils.rcp.internal;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.recommenders.utils.rcp.UUIDHelper;

public class PreferencesInitalizer extends AbstractPreferenceInitializer {

    public static String PROP_UUID = "uuid";

    @Override
    public void initializeDefaultPreferences() {
        final IPreferenceStore preferenceStore = RecommendersUtilsPlugin.getDefault().getPreferenceStore();
        preferenceStore.setDefault(PROP_UUID, UUIDHelper.generateGlobalUUID());
    }
}
