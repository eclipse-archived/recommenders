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
package org.eclipse.recommenders.utils.rcp;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.fromNullable;
import static java.net.NetworkInterface.getNetworkInterfaces;
import static org.eclipse.recommenders.utils.GenericEnumerationUtils.iterable;

import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.UUID;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.recommenders.utils.rcp.internal.PreferencesInitalizer;
import org.eclipse.recommenders.utils.rcp.internal.RecommendersUtilsPlugin;

import com.google.common.base.Optional;
import com.google.common.base.Strings;

public class UUIDHelper {

    public static String getUUID() {
        final Optional<String> uuid = lookupUUIDFromStore();
        if (uuid.isPresent()) {
            return uuid.get();
        }
        final String newUuid = generateGlobalUUID();
        storeUUID(newUuid);
        return newUuid;
    }

    private static Optional<String> lookupUUIDFromStore() {
        final RecommendersUtilsPlugin plugin = RecommendersUtilsPlugin.getDefault();
        final IPreferenceStore prefStore = plugin.getPreferenceStore();
        final String uuid = prefStore.getString(PreferencesInitalizer.PROP_UUID);
        if (Strings.isNullOrEmpty(uuid)) {
            return Optional.absent();
        }
        return Optional.fromNullable(uuid);
    }

    private static void storeUUID(final String uuid) {
        final RecommendersUtilsPlugin plugin = RecommendersUtilsPlugin.getDefault();
        final IPreferenceStore prefStore = plugin.getPreferenceStore();
        prefStore.putValue(PreferencesInitalizer.PROP_UUID, uuid);
    }

    public static String generateGlobalUUID() {
        final Optional<String> uuid = generateUUIDFromMacAddress();
        if (!uuid.isPresent()) {
            return UUID.randomUUID().toString();
        }
        return uuid.get();
    }

    private static Optional<String> generateUUIDFromMacAddress() {
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
                return fromNullable(uuid);
            }
        } catch (final Exception e) {
            // this is odd:
            e.printStackTrace();
        }
        return absent();
    }

    public static String generateUID() {
        return UUID.randomUUID().toString();
    }
}
