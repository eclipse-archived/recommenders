/**
 * Copyright (c) 2014 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.stacktraces.rcp;

import static org.eclipse.recommenders.internal.stacktraces.rcp.Constants.*;

import org.apache.commons.lang3.SystemUtils;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.recommenders.internal.stacktraces.rcp.StacktracesRcpPreferences.Mode;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

    @Override
    public void initializeDefaultPreferences() {
        IEclipsePreferences s = DefaultScope.INSTANCE.getNode(PLUGIN_ID);
        s.put(PROP_SERVER, "http://recommenders.eclipse.org/stats/stacktraces/0.3.0/new/");
        // s.put(PROP_SERVER, "http://localhost:9002/new/");
        s.put(PROP_NAME, SystemUtils.USER_NAME);
        s.put(PROP_EMAIL, "me@example.org");
        s.put(PROP_MODE, Mode.ASK.name());
        s.putBoolean(PROP_ANONYMIZE_STACKFRAMES, false);
        s.putBoolean(PROP_CLEAR_MESSAGES, false);
    }

}
