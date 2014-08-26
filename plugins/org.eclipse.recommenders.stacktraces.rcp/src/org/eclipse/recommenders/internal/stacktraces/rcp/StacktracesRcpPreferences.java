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

import static com.google.common.base.Throwables.propagate;

import java.net.URI;
import java.net.URISyntaxException;

import javax.inject.Inject;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.recommenders.utils.Logs;
import org.eclipse.recommenders.utils.Nullable;
import org.osgi.service.prefs.BackingStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("restriction")
public class StacktracesRcpPreferences {

    public static enum Mode {
        IGNORE,
        ASK,
        SILENT;

        @Nullable
        public static Mode parse(String string, Mode fallback) {
            try {
                return Mode.valueOf(string);
            } catch (Exception e) {
                Logs.log(LogMessages.FAILED_TO_PARSE_SEND_MODE, string, fallback);
                return fallback;
            }
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(StacktracesRcpPreferences.class);

    private static final String PROP_MODE = "mode";

    private static final String PROP_NAME = "name";

    private static final String PROP_EMAIL = "email";

    private static final String PROP_ANONYMIZE = "anonymize stacktraces";

    private static final String PROP_CLEAR_MESSAGES = "clear messages";

    private static final Mode MODE_DEFAULT = Mode.ASK;

    @Inject
    @Preference
    private IEclipsePreferences prefs;

    @Inject
    @Preference(PROP_EMAIL)
    private String email;

    @Inject
    @Preference("server")
    private String server;

    @Inject
    @Preference(PROP_NAME)
    private String name;

    private Mode mode;

    @Inject
    @Preference(PROP_ANONYMIZE)
    private boolean anonymize;

    @Inject
    @Preference(PROP_CLEAR_MESSAGES)
    private boolean clearMessages;

    public void setEmail(String text) {
        putString(PROP_EMAIL, text);
    }

    @Inject
    protected void internal_SetMode(@Preference(PROP_MODE) String mode) {
        this.mode = Mode.parse(mode, MODE_DEFAULT);
    }

    public String getEmail() {
        return email;
    }

    public URI getServerUri() {
        try {
            return new URI(server);
        } catch (URISyntaxException e) {
            throw propagate(e);
        }
    }

    public void setName(String text) {
        putString(PROP_NAME, text);
    }

    public String getName() {
        return name;
    }

    public void setMode(Mode newMode) {
        putString(PROP_MODE, newMode.name());
    }

    public Mode getMode() {
        return mode;
    }

    public void setAnonymize(boolean anonymize) {
        putBoolean(PROP_ANONYMIZE, anonymize);
    }

    public boolean shouldAnonymize() {
        return anonymize;
    }

    public void setClearMessages(boolean clearMessages) {
        putBoolean(PROP_CLEAR_MESSAGES, clearMessages);
    }

    public boolean shouldClearMessages() {
        return clearMessages;
    }

    private void putString(String prop, String text) {
        prefs.put(prop, text);
        try {
            prefs.flush();
        } catch (BackingStoreException e) {
            LOG.error("Failed to flush preferences", e); //$NON-NLS-1$
        }
    }

    private void putBoolean(String prop, boolean value) {
        prefs.putBoolean(prop, value);
        try {
            prefs.flush();
        } catch (BackingStoreException e) {
            LOG.error("Failed to flush preferences", e); //$NON-NLS-1$
        }
    }

}
