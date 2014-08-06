/**
 * Copyright (c) 2010, 2014 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.stacktraces.rcp;

import static com.google.common.base.Throwables.propagate;

import java.net.URI;
import java.net.URISyntaxException;

import javax.inject.Inject;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.di.extensions.Preference;
import org.osgi.service.prefs.BackingStoreException;

@SuppressWarnings("restriction")
public class StacktracesRcpPreferences {

    public static final String MODE_IGNORE = "ignore";

    public static final String MODE_ASK = "ask";

    public static final String MODE_SILENT = "silent";

    private static final String PROP_MODE = "mode";

    private static final String PROP_NAME = "name";

    private static final String PROP_EMAIL = "email";

    @Inject
    @Preference
    public IEclipsePreferences prefs;

    @Inject
    @Preference(PROP_EMAIL)
    public String email;

    @Inject
    @Preference("server")
    public String server;

    @Inject
    @Preference(PROP_NAME)
    public String name;

    @Inject
    @Preference(PROP_MODE)
    public String mode;

    public boolean modeSilent() {
        return MODE_SILENT.equals(mode);
    }

    public boolean modeAsk() {
        return MODE_ASK.equals(mode);
    }

    public boolean modeIgnore() {
        return MODE_IGNORE.equals(mode);
    }

    public void setMode(String newMode) {
        putString(PROP_MODE, newMode);
    }

    public void setName(String text) {
        putString(PROP_NAME, text);
    }

    private void putString(String prop, String text) {
        prefs.put(prop, text);
        try {
            prefs.flush();
        } catch (BackingStoreException e) {
        }
    }

    public void setEmail(String text) {
        putString(PROP_EMAIL, text);
    }

    public URI getServerUri() {
        try {
            return new URI(server);
        } catch (URISyntaxException e) {
            throw propagate(e);
        }
    }
}
