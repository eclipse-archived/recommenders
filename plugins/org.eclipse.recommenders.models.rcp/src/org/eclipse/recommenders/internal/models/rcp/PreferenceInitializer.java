/**
 * Copyright (c) 2010, 2012 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.models.rcp;

import static org.eclipse.recommenders.internal.models.rcp.Constants.*;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

    public static final String SERVER_URL = "http://download.eclipse.org/recommenders/models/luna-m2/"; //$NON-NLS-1$

    @Override
    public void initializeDefaultPreferences() {
        IEclipsePreferences s = DefaultScope.INSTANCE.getNode(BUNDLE_ID);
        s.put(P_REPOSITORY_URL, SERVER_URL);
        s.put(P_REPOSITORY_URL_LIST, SERVER_URL);
        s.putBoolean(P_REPOSITORY_ENABLE_AUTO_DOWNLOAD, true);
    }
}
