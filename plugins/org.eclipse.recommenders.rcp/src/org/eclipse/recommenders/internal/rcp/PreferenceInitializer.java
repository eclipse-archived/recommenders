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
package org.eclipse.recommenders.internal.rcp;

import static org.eclipse.core.runtime.preferences.InstanceScope.INSTANCE;
import static org.eclipse.recommenders.internal.rcp.Constants.*;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

public final class PreferenceInitializer extends AbstractPreferenceInitializer {

    @Override
    public void initializeDefaultPreferences() {
        IEclipsePreferences node = INSTANCE.getNode(BUNDLE_NAME);
        if (node.getLong(PREF_SURVEY_FIRST_ACTIVATION_DATE, -1) == -1) {
            node.putLong(PREF_SURVEY_FIRST_ACTIVATION_DATE, System.currentTimeMillis());
        }
    }
}
