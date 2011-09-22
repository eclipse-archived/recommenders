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

public class UploadPreferences {

    public static boolean doAskBeforeUploading() {
        return PreferenceUtil.getPluginNode().getBoolean(PreferenceKeys.askBeforeUploading, true);
    }

    public static void setAskBeforeUploading(final boolean ask) {
        PreferenceUtil.getPluginNode().putBoolean(PreferenceKeys.askBeforeUploading, ask);
    }

    public static void setLastTimeOpenedUploadWizard(final long date) {
        PreferenceUtil.getPluginNode().putLong(PreferenceKeys.lastTimeOpenedUploadWizard, date);
    }

    public static void setUploadData(final boolean uploadData) {
        PreferenceUtil.getPluginNode().putBoolean(PreferenceKeys.uploadData, uploadData);
    }

    public static boolean isUploadData() {
        return PreferenceUtil.getPluginNode().getBoolean(PreferenceKeys.uploadData, true);
    }

    public static boolean isUploadOnStartup() {
        return PreferenceUtil.getPluginNode().getBoolean(PreferenceKeys.uploadOnStartup, true);
    }

    public static void setUploadOnStartup(final boolean uploadOnStartup) {
        PreferenceUtil.getPluginNode().putBoolean(PreferenceKeys.uploadOnStartup, uploadOnStartup);
    }

    public static int getUploadIntervalDays() {
        return PreferenceUtil.getPluginNode().getInt(PreferenceKeys.uploadInvervalDay, 3);
    }

    public static void setUploadIntervalDays(final int interval) {
        PreferenceUtil.getPluginNode().putInt(PreferenceKeys.uploadInvervalDay, interval);
    }

    public static int getUploadHour() {
        return PreferenceUtil.getPluginNode().getInt(PreferenceKeys.uploadInvervalHour, 12);
    }

    public static void setUploadHour(final int hour) {
        PreferenceUtil.getPluginNode().putInt(PreferenceKeys.uploadInvervalHour, hour);
    }

    public static boolean isOpenUploadWizard() {
        return PreferenceUtil.getPluginNode().getBoolean(PreferenceKeys.openUploadWizard, true);
    }

    public static void setOpenUploadWizard(final boolean openWizard) {
        PreferenceUtil.getPluginNode().putBoolean(PreferenceKeys.openUploadWizard, openWizard);
    }

    public static long getLastUploadDate() {
        return PreferenceUtil.getPluginNode().getLong(PreferenceKeys.lastTimeUploaded, 0);
    }

    public static void setLastUploadDate(final long date) {
        PreferenceUtil.getPluginNode().putLong(PreferenceKeys.lastTimeUploaded, date);
    }

    public static boolean isUploadAtAnyHour() {
        return PreferenceUtil.getPluginNode().getBoolean(PreferenceKeys.uploadIntervalAnyHour, true);
    }

    public static void setUploadAtAnyHour(final boolean uploadAtAnyHour) {
        PreferenceUtil.getPluginNode().putBoolean(PreferenceKeys.uploadIntervalAnyHour, uploadAtAnyHour);
    }
}
