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

import static java.net.NetworkInterface.getNetworkInterfaces;
import static org.eclipse.recommenders.commons.utils.GenericEnumerationUtils.iterable;
import static org.eclipse.recommenders.commons.utils.Option.none;
import static org.eclipse.recommenders.commons.utils.Option.wrap;

import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.UUID;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.recommenders.commons.utils.Option;
import org.eclipse.recommenders.rcp.utils.internal.PreferencesInitalizer;
import org.eclipse.recommenders.rcp.utils.internal.RecommendersUtilsPlugin;

import com.google.common.base.Strings;

public class UUIDHelper {

    public static String getUUID() {
        final Option<String> uuid = lookupUUIDFromStore();
        if (uuid.hasValue()) {
            return uuid.get();
        }
        final String newUuid = generateGlobalUUID();
        storeUUID(newUuid);
        return newUuid;
    }

    private static Option<String> lookupUUIDFromStore() {
        final RecommendersUtilsPlugin plugin = RecommendersUtilsPlugin.getDefault();
        final IPreferenceStore prefStore = plugin.getPreferenceStore();
        final String uuid = prefStore.getString(PreferencesInitalizer.PROP_UUID);
        if (Strings.isNullOrEmpty(uuid)) {
            return Option.none();
        }
        return Option.wrap(uuid);
    }

    private static void storeUUID(final String uuid) {
        final RecommendersUtilsPlugin plugin = RecommendersUtilsPlugin.getDefault();
        final IPreferenceStore prefStore = plugin.getPreferenceStore();
        prefStore.putValue(PreferencesInitalizer.PROP_UUID, uuid);
    }

    public static String generateGlobalUUID() {
        final Option<String> uuid = generateUUIDFromMacAddress();
        if (!uuid.hasValue()) {
            return UUID.randomUUID().toString();
        }
        return uuid.get();
    }

    private static Option<String> generateUUIDFromMacAddress() {
        try {
            final Enumeration<NetworkInterface> e = getNetworkInterfaces();
            for (final NetworkInterface net : iterable(e)) {
                final byte[] mac = net.getHardwareAddress();
                if (ArrayUtils.isEmpty(mac)) {
                    continue;
                }
                final String uuid = UUID.nameUUIDFromBytes(mac).toString();
                if (Strings.isNullOrEmpty(uuid)) {
                    continue;
                }
                return wrap(uuid);
            }
        } catch (final Exception e) {
            // this is odd:
            e.printStackTrace();
        }
        return none();
    }

    public static String generateUID() {
        return UUID.randomUUID().toString();
    }
}
