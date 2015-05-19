/**
 * Copyright (c) 2015 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Sewe - initial API and implementation.
 */
package org.eclipse.recommenders.internal.constructors.rcp;

import static org.eclipse.recommenders.internal.constructors.rcp.Constants.*;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

public final class PreferenceInitializer extends AbstractPreferenceInitializer {

    @Override
    public void initializeDefaultPreferences() {
        IEclipsePreferences node = DefaultScope.INSTANCE.getNode(BUNDLE_ID);
        node.putInt(PREF_MAX_NUMBER_OF_PROPOSALS, 7);
        node.putInt(PREF_MIN_PROPOSAL_PROBABILITY, 1);
        node.putBoolean(PREF_DECORATE_PROPOSAL_ICON, true);
        node.putBoolean(PREF_DECORATE_PROPOSAL_ICON, true);
        node.putBoolean(PREF_DECORATE_PROPOSAL_TEXT, true);
        node.putBoolean(PREF_UPDATE_PROPOSAL_RELEVANCE, true);
    }
}
