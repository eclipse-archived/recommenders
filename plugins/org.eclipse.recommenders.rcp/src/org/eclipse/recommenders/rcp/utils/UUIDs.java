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

import static com.google.common.base.Optional.*;
import static com.google.common.collect.Iterators.*;
import static java.net.NetworkInterface.getNetworkInterfaces;
import static org.eclipse.recommenders.internal.rcp.Constants.PREF_UUID;
import static org.eclipse.recommenders.internal.rcp.LogMessages.FAILED_TO_GENERATE_UUID;
import static org.eclipse.recommenders.utils.Logs.log;

import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.UUID;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.recommenders.internal.rcp.RcpPlugin;

import com.google.common.base.Optional;
import com.google.common.base.Strings;

public final class UUIDs {

    private UUIDs() {
        throw new IllegalStateException("Not meant to be instantiated"); //$NON-NLS-1$
    }

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
        final RcpPlugin plugin = RcpPlugin.getDefault();
        final IPreferenceStore prefStore = plugin.getPreferenceStore();
        final String uuid = prefStore.getString(PREF_UUID);
        if (Strings.isNullOrEmpty(uuid)) {
            return Optional.absent();
        }
        return Optional.fromNullable(uuid);
    }

    private static void storeUUID(final String uuid) {
        final RcpPlugin plugin = RcpPlugin.getDefault();
        final IPreferenceStore prefStore = plugin.getPreferenceStore();
        prefStore.putValue(PREF_UUID, uuid);
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
            for (final NetworkInterface net : toArray(forEnumeration(e), NetworkInterface.class)) {
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
            log(FAILED_TO_GENERATE_UUID, e);
        }
        return absent();
    }

    public static String newUuid() {
        return UUID.randomUUID().toString();
    }
}
