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

import org.apache.commons.lang3.SystemUtils;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.recommenders.internal.stacktraces.rcp.StacktracesRcpPreferences.Mode;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

    @Override
    public void initializeDefaultPreferences() {

        IEclipsePreferences s = DefaultScope.INSTANCE.getNode("org.eclipse.recommenders.stacktraces.rcp");
        s.put("server", "http://recommenders.eclipse.org/stats/stacktraces/0.2.0/new/");
        // s.put("server", "http://localhost:9002/new/");
        s.put("name", SystemUtils.USER_NAME);
        s.put("email", "me@example.org");
        s.put("mode", Mode.ASK.name());
        s.putBoolean("anonymize stacktraces", false);
        s.putBoolean("clear messages", false);
    }

}
