/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marcel Bruch - initial API and implementation
 */
package org.eclipse.recommenders.internal.models.rcp;

import static org.apache.commons.lang3.ArrayUtils.isEquals;
import static org.eclipse.recommenders.internal.models.rcp.Constants.PREF_REPOSITORY_ENABLE_AUTO_DOWNLOAD;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.inject.Inject;

import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.equinox.security.storage.EncodingUtils;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.recommenders.internal.models.rcp.l10n.LogMessages;
import org.eclipse.recommenders.models.rcp.ModelEvents.ModelRepositoryUrlChangedEvent;
import org.eclipse.recommenders.utils.Logs;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.eventbus.EventBus;

@SuppressWarnings("restriction")
public class ModelsRcpPreferences {

    @Inject
    @Preference(PREF_REPOSITORY_ENABLE_AUTO_DOWNLOAD)
    public boolean autoDownloadEnabled;

    public String[] remotes;

    private final EventBus bus;

    static final String URL_SEPARATOR = "\t"; //$NON-NLS-1$

    private final ISecurePreferences securePreferencesRoot;

    @Inject
    public ModelsRcpPreferences(EventBus bus) {
        this.bus = bus;

        ISecurePreferences root = SecurePreferencesFactory.getDefault();
        if (root == null) {
            securePreferencesRoot = null;
        } else {
            securePreferencesRoot = root.node(Constants.BUNDLE_ID);
        }
    }

    @Inject
    public void setRemote(@Preference(Constants.PREF_REPOSITORY_URL_LIST) String newRemote) throws Exception {
        String[] old = remotes;
        remotes = splitRemoteRepositoryString(newRemote);
        if (!isEquals(remotes, old)) {
            bus.post(new ModelRepositoryUrlChangedEvent());
        }
    }

    public void setServerUsername(String serverUri, String username) {
        ISecurePreferences securePreferences = getSecurePreferencesForServer(serverUri, true).orNull();
        if (securePreferences == null) {
            return;
        }
        try {
            if (Strings.isNullOrEmpty(username)) {
                securePreferences.remove(Constants.PREF_REPOSITORY_USERNAME);
            } else {
                securePreferences.put(Constants.PREF_REPOSITORY_USERNAME, username, false);
            }
            securePreferences.flush();
        } catch (StorageException | IOException e) {
            Logs.log(LogMessages.ERROR_FAILED_TO_STORE_SECURE_PREFERENCE, e);
        }
    }

    public Optional<String> getServerUsername(String serverUri) {
        ISecurePreferences securePreferences = getSecurePreferencesForServer(serverUri, false).orNull();
        if (securePreferences == null) {
            return Optional.absent();
        }

        try {
            return Optional.fromNullable(securePreferences.get(Constants.PREF_REPOSITORY_USERNAME, null));
        } catch (StorageException e) {
            Logs.log(LogMessages.ERROR_FAILED_TO_LOAD_SECURE_PREFERENCE, e);
            return Optional.absent();
        }
    }

    public void setServerPassword(String serverUri, String password) {
        ISecurePreferences securePreferences = getSecurePreferencesForServer(serverUri, true).orNull();
        if (securePreferences == null) {
            return;
        }
        try {
            if (Strings.isNullOrEmpty(password)) {
                securePreferences.remove(Constants.PREF_REPOSITORY_PASSWORD);
            } else {
                securePreferences.put(Constants.PREF_REPOSITORY_PASSWORD, password, true);
            }
            securePreferences.flush();
        } catch (StorageException | IOException e) {
            Logs.log(LogMessages.ERROR_FAILED_TO_STORE_SECURE_PREFERENCE, e);
        }
    }

    public Optional<String> getServerPassword(String serverUri) {
        ISecurePreferences securePreferences = getSecurePreferencesForServer(serverUri, false).orNull();
        if (securePreferences == null) {
            return Optional.absent();
        }

        try {
            return Optional.fromNullable(securePreferences.get(Constants.PREF_REPOSITORY_PASSWORD, null));
        } catch (StorageException e) {
            Logs.log(LogMessages.ERROR_FAILED_TO_LOAD_SECURE_PREFERENCE, e);
            return Optional.absent();
        }
    }

    public boolean hasPassword(String serverUri) {
        ISecurePreferences securePreferences = getSecurePreferencesForServer(serverUri, false).orNull();
        if (securePreferences == null) {
            return false;
        }

        try {
            return securePreferences.isEncrypted(Constants.PREF_REPOSITORY_PASSWORD);
        } catch (StorageException e) {
            Logs.log(LogMessages.ERROR_FAILED_TO_LOAD_SECURE_PREFERENCE);
            return false;
        }
    }

    private Optional<ISecurePreferences> getSecurePreferencesForServer(String uri, boolean create) {
        if (Strings.isNullOrEmpty(uri)) {
            return Optional.absent();
        }

        if (securePreferencesRoot == null) {
            Logs.log(LogMessages.ERROR_FAILED_TO_LOAD_SECURE_PREFERENCE);
            return Optional.absent();
        }

        String nodeName = EncodingUtils.encodeSlashes(uri);
        if (create || securePreferencesRoot.nodeExists(nodeName)) {
            return Optional.of(securePreferencesRoot.node(nodeName));
        }
        return Optional.absent();
    }

    public static String[] splitRemoteRepositoryString(String remoteUrls) {
        Iterable<String> split = Splitter.on(URL_SEPARATOR).omitEmptyStrings().split(remoteUrls);
        return Iterables.toArray(split, String.class);
    }

    public static String joinRemoteRepositoriesToString(String[] remotes) {
        return Joiner.on(ModelsRcpPreferences.URL_SEPARATOR).join(remotes);
    }

    public static String joinRemoteRepositoriesToString(Iterable<String> remotes) {
        return joinRemoteRepositoriesToString(Iterables.toArray(remotes, String.class));
    }
}
