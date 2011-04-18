/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.rcp.utils;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.UUID;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.recommenders.rcp.utils.internal.PreferencesInitalizer;
import org.eclipse.recommenders.rcp.utils.internal.RecommendersUtilsPlugin;

public class UUIDHelper {

    public static String getUUID() {
        String uuid = lookupUUIDFromStore();
        if (uuid == null) {
            uuid = generateGlobalUUID();
            storeUUID(uuid);
        }
        return uuid;
    }

    private static String lookupUUIDFromStore() {
        final RecommendersUtilsPlugin plugin = RecommendersUtilsPlugin.getDefault();
        final IPreferenceStore prefStore = plugin.getPreferenceStore();
        final String uuid = prefStore.getString(PreferencesInitalizer.UUID);
        return uuid.isEmpty() ? null : uuid;
    }

    private static void storeUUID(final String uuid) {
        final RecommendersUtilsPlugin plugin = RecommendersUtilsPlugin.getDefault();
        final IPreferenceStore prefStore = plugin.getPreferenceStore();
        prefStore.putValue(PreferencesInitalizer.UUID, uuid);
    }

    public static String generateGlobalUUID() {
        String uuid = generateUUIDFromMacAddress();
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
        }
        return uuid;
    }

    private static String generateUUIDFromMacAddress() {
        String uuid = null;
        try {
            final Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
            if (e.hasMoreElements()) {
                final NetworkInterface net = e.nextElement();
                final byte[] mac = net.getHardwareAddress();
                uuid = UUID.nameUUIDFromBytes(mac).toString();
            }
        } catch (final SocketException e) {
            e.printStackTrace();
        }
        return uuid;
    }

    public static String generateUID() {
        return UUID.randomUUID().toString();
    }
}
