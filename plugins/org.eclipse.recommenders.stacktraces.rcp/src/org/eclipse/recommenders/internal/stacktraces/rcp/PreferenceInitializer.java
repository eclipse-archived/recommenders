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

import java.util.ArrayList;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.ModelFactory;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.SendAction;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.Settings;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

    @Override
    public void initializeDefaultPreferences() {
        IEclipsePreferences s = DefaultScope.INSTANCE.getNode(PLUGIN_ID);
        s.put(PROP_SERVER, SERVER_URL);
        s.put(PROP_NAME, "");
        s.put(PROP_EMAIL, "");
        s.put(PROP_WHITELISTED_PLUGINS, "org.eclipse.;com.codetrails.;org.osgi");
        s.put(PROP_WHITELISTED_PACKAGES,
                "org.eclipse.;org.apache.;java.;javax.;javafx.;sun.;com.sun.;com.codetrails.;org.osgi.;com.google.;");
        s.put(PROP_SEND_ACTION, SendAction.ASK.name());
        s.putBoolean(PROP_ANONYMIZE_STACKTRACES, true);
        s.putBoolean(PROP_ANONYMIZE_MESSAGES, false);
    }

    public static Settings readSettings() {
        ScopedPreferenceStore s = new ScopedPreferenceStore(InstanceScope.INSTANCE, PLUGIN_ID);
        Settings settings = ModelFactory.eINSTANCE.createSettings();
        settings.setName(s.getString(PROP_NAME));
        settings.setEmail(s.getString(PROP_EMAIL));
        settings.setServerUrl(s.getString(PROP_SERVER));
        settings.getWhitelistedPluginIds().addAll(parseWhitelist(s.getString(PROP_WHITELISTED_PLUGINS)));
        settings.getWhitelistedPackages().addAll(parseWhitelist(s.getString(PROP_WHITELISTED_PACKAGES)));
        settings.setAnonymizeMessages(s.getBoolean(PROP_ANONYMIZE_MESSAGES));
        settings.setAnonymizeStrackTraceElements(s.getBoolean(PROP_ANONYMIZE_STACKTRACES));
        settings.setAction(parseSendAction(s.getString(PROP_SEND_ACTION)));
        return settings;
    }

    public static void saveSettings(Settings settings) {
        //
        // XXX: server url and whitelist attributes are not persisted! They shoudn't be changed
        ScopedPreferenceStore s = new ScopedPreferenceStore(InstanceScope.INSTANCE, PLUGIN_ID);
        s.setValue(PROP_NAME, settings.getName());
        s.setValue(PROP_EMAIL, settings.getEmail());
        s.setValue(PROP_ANONYMIZE_STACKTRACES, settings.isAnonymizeStrackTraceElements());
        s.setValue(PROP_ANONYMIZE_MESSAGES, settings.isAnonymizeMessages());
        s.setValue(PROP_SEND_ACTION, settings.getAction().name());
    }

    static ArrayList<String> parseWhitelist(String s) {
        Iterable<String> ids = Splitter.on(';').omitEmptyStrings().trimResults().split(s);
        return Lists.newArrayList(ids);
    }

    static boolean flagFirstAccess = true;

    static SendAction parseSendAction(String mode) {
        try {
            SendAction value = SendAction.valueOf(mode);
            if (flagFirstAccess && value == SendAction.PAUSE) {
                value = SendAction.ASK;
            }
            flagFirstAccess = false;
            return value;
        } catch (Exception e) {
            return SendAction.ASK;
        }
    }
}
