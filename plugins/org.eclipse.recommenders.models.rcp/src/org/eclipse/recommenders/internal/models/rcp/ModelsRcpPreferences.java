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

import javax.inject.Inject;

import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.recommenders.models.rcp.ModelEvents.ModelRepositoryUrlChangedEvent;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
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

    @Inject
    public ModelsRcpPreferences(EventBus bus) {
        this.bus = bus;
    }

    @Inject
    public void setRemote(@Preference(Constants.PREF_REPOSITORY_URL_LIST) String newRemote) throws Exception {
        String[] old = remotes;
        remotes = splitRemoteRepositoryString(newRemote);
        if (!isEquals(remotes, old)) {
            bus.post(new ModelRepositoryUrlChangedEvent());
        }
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
