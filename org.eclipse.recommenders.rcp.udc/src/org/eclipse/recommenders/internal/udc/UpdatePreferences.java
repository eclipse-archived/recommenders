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

public class UpdatePreferences {

    public static void setUpdateModels(final boolean doUpdate) {
        PreferenceUtil.getPluginNode().putBoolean(PreferenceKeys.updateModels, doUpdate);
    }

    public static boolean isUpdateModels() {
        return PreferenceUtil.getPluginNode().getBoolean(PreferenceKeys.updateModels, true);
    }

    public static void setUpdateInterval(final int interval) {
        PreferenceUtil.getPluginNode().putInt(PreferenceKeys.updateInterval, interval);
    }

    public static int getUpdateInterval() {
        return PreferenceUtil.getPluginNode().getInt(PreferenceKeys.updateInterval, 7);
    }

    public static void setLastUpdateDate(final long date) {
        PreferenceUtil.getPluginNode().putLong(PreferenceKeys.lastUpdateDate, date);
    }

    public static long getLastUpdateDate() {
        return PreferenceUtil.getPluginNode().getLong(PreferenceKeys.lastUpdateDate, 0);
    }

}
