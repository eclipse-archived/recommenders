/**
 * Copyright (c) 2011 Andreas Frankenberger.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API and implementation.
 */
package org.eclipse.recommenders.internal.udc;

/**
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API and implementation.
 */
import java.util.ArrayList;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.recommenders.internal.udc.ui.preferences.PackagePreferences;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

public class PreferenceUtil {
    public static String[] getExcludeExpressions() {
        return getPreferencesArray(PackagePreferences.excludExpressions);
    }

    public static String[] getIncludeExpressions() {
        return getPreferencesArray(PackagePreferences.includExpressions);
    }

    public static void setExpressions(final String[] expressions, final String nodeId) {
        setPreferencesArray(nodeId, expressions);
    }

    public static String[] getEnabledLibraries() {
        return getPreferencesArray(PreferenceKeys.enabledLibraries);
    }

    private static void setPreferencesArray(final String nodeId, final String[] preferences) {
        final Preferences pref = getPluginNode().node(nodeId);
        try {
            pref.clear();
        } catch (final BackingStoreException e) {
            throw new RuntimeException(e);
        }
        for (final String preference : preferences) {
            if (preference == null) {
                throw new RuntimeException("preferences array must not contain null pointers: "
                        + preferences.toString());
            }
            pref.put(preference, preference);
        }
        try {
            pref.flush();
        } catch (final BackingStoreException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setEnabledLibraries(final String[] libraries) {
        setPreferencesArray(PreferenceKeys.enabledLibraries, libraries);
    }

    private static String[] getPreferencesArray(final String nodeId) {
        final Preferences pref = new InstanceScope().getNode(Activator.PLUGIN_ID).node(nodeId);
        final ArrayList<String> result = new ArrayList<String>();
        try {
            for (final String key : pref.keys()) {
                result.add(pref.get(key, ""));
            }
        } catch (final BackingStoreException e) {
            throw new RuntimeException(e);
        }
        return result.toArray(new String[result.size()]);
    }

    public static boolean isDepesonalisationRequired() {
        return getPluginNode().getBoolean(PreferenceKeys.depersonalizeData, false);
    }

    public static void setDepersonalisationRequired(final boolean required) {
        getPluginNode().putBoolean(PreferenceKeys.depersonalizeData, required);
    }

    static IPreferenceStore getPreferenceStore() {
        return Activator.getDefault().getPreferenceStore();
    }

    static boolean needToOpenUploadWizard() {
        return UploadPreferences.isOpenUploadWizard();
    }

    public static IEclipsePreferences getPluginNode() {
        return new InstanceScope().getNode(Activator.PLUGIN_ID);
    }

    public static long getLastTimeOpenedUploadWizard() {
        return getPluginNode().getLong(PreferenceKeys.lastTimeOpenedUploadWizard, 0);
    }
}
