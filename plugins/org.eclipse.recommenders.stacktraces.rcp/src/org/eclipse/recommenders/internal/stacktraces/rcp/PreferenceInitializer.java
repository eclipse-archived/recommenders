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
import static org.eclipse.recommenders.utils.Logs.log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.ModelFactory;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.RememberSendAction;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.SendAction;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.Settings;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

    private static final long MS_PER_DAY = TimeUnit.DAYS.toMillis(1);
    private static boolean flagFirstAccess = true;

    @Override
    public void initializeDefaultPreferences() {
        IEclipsePreferences s = DefaultScope.INSTANCE.getNode(PLUGIN_ID);
        s.put(PROP_SERVER, SERVER_URL);
        s.put(PROP_NAME, "");
        s.put(PROP_EMAIL, "");
        s.putBoolean(PROP_SKIP_SIMILAR_ERRORS, true);
        s.putBoolean(PROP_CONFIGURED, false);
        s.put(PROP_WHITELISTED_PLUGINS, Constants.WHITELISTED_PLUGINS);
        s.put(PROP_WHITELISTED_PACKAGES, Constants.WHITELISTED_PACKAGES);
        s.put(PROP_SEND_ACTION, SendAction.ASK.name());
        s.put(PROP_REMEMBER_SEND_ACTION, RememberSendAction.NONE.name());
        s.putBoolean(PROP_ANONYMIZE_STACKTRACES, true);
        s.putBoolean(PROP_ANONYMIZE_MESSAGES, false);
    }

    public static Settings readSettings() {
        ScopedPreferenceStore s = new ScopedPreferenceStore(InstanceScope.INSTANCE, PLUGIN_ID);
        Settings settings = ModelFactory.eINSTANCE.createSettings();
        settings.setConfigured(s.getBoolean(PROP_CONFIGURED));
        settings.setName(s.getString(PROP_NAME));
        settings.setEmail(s.getString(PROP_EMAIL));
        settings.setSkipSimilarErrors(s.getBoolean(PROP_SKIP_SIMILAR_ERRORS));
        settings.setServerUrl(s.getString(PROP_SERVER));
        settings.getWhitelistedPluginIds().addAll(parseWhitelist(s.getString(PROP_WHITELISTED_PLUGINS)));
        settings.getWhitelistedPackages().addAll(parseWhitelist(s.getString(PROP_WHITELISTED_PACKAGES)));
        settings.setAnonymizeMessages(s.getBoolean(PROP_ANONYMIZE_MESSAGES));
        settings.setAnonymizeStrackTraceElements(s.getBoolean(PROP_ANONYMIZE_STACKTRACES));
        RememberSendAction rememberSendAction = parseRememberSendAction(s.getString(PROP_REMEMBER_SEND_ACTION));
        settings.setRememberSendAction(rememberSendAction);
        settings.setAction(parseSendAction(s.getString(PROP_SEND_ACTION),
                s.getLong(PROP_REMEMBER_SETTING_PERIOD_START), rememberSendAction));
        flagFirstAccess = false;
        return settings;
    }

    public static void saveSettings(Settings settings) {
        //
        // XXX: server url and whitelist attributes are not persisted! They shoudn't be changed
        ScopedPreferenceStore s = new ScopedPreferenceStore(InstanceScope.INSTANCE, PLUGIN_ID);
        s.setValue(PROP_CONFIGURED, settings.isConfigured());
        s.setValue(PROP_NAME, settings.getName());
        s.setValue(PROP_EMAIL, settings.getEmail());
        s.setValue(PROP_SKIP_SIMILAR_ERRORS, settings.isSkipSimilarErrors());
        s.setValue(PROP_ANONYMIZE_STACKTRACES, settings.isAnonymizeStrackTraceElements());
        s.setValue(PROP_ANONYMIZE_MESSAGES, settings.isAnonymizeMessages());
        s.setValue(PROP_SEND_ACTION, settings.getAction().name());
        s.setValue(PROP_REMEMBER_SEND_ACTION, settings.getRememberSendAction().name());
        s.setValue(PROP_REMEMBER_SETTING_PERIOD_START, settings.getRememberSendActionPeriodStart());
        try {
            s.save();
        } catch (IOException e) {
            log(LogMessages.SAVE_PREFERENCES_FAILED, e);
        }
    }

    static ArrayList<String> parseWhitelist(String s) {
        Iterable<String> ids = Splitter.on(';').omitEmptyStrings().trimResults().split(s);
        return Lists.newArrayList(ids);
    }

    private static RememberSendAction parseRememberSendAction(String rememberAction) {
        try {
            return RememberSendAction.valueOf(rememberAction);
        } catch (IllegalArgumentException e) {
            log(LogMessages.FAILED_TO_PARSE_SEND_MODE, rememberAction, RememberSendAction.NONE);
            return RememberSendAction.NONE;
        }
    }

    static SendAction parseSendAction(String mode, long pauseTimestamp, RememberSendAction rememberSendAction) {
        try {
            SendAction action = SendAction.valueOf(mode);
            if (isRememberingPeriodElapsed(pauseTimestamp, rememberSendAction)) {
                log(LogMessages.PAUSE_PERIOD_ELAPSED);
                return SendAction.ASK;
            } else {
                return action;
            }
        } catch (IllegalArgumentException e) {
            log(LogMessages.FAILED_TO_PARSE_SEND_MODE, mode, SendAction.ASK);
            return SendAction.ASK;
        }
    }

    private static boolean isRememberingPeriodElapsed(long pauseTimestamp, RememberSendAction rememberSendAction) {
        if (rememberSendAction == RememberSendAction.RESTART && flagFirstAccess) {
            return true;
        } else if (rememberSendAction == RememberSendAction.HOURS_24) {
            long elapsedTime = System.currentTimeMillis() - pauseTimestamp;
            boolean isDayElapsed = elapsedTime >= MS_PER_DAY;
            return isDayElapsed;
        }
        return false;
    }
}
