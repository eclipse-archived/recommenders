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

import static org.eclipse.recommenders.internal.models.rcp.Constants.P_REPOSITORY_ENABLE_AUTO_DOWNLOAD;

import java.util.Set;

import javax.inject.Inject;

import org.eclipse.e4.core.di.extensions.Preference;

import com.google.common.collect.Sets;

public class ModelsRcpPreferences {

    @Inject
    @Preference(P_REPOSITORY_ENABLE_AUTO_DOWNLOAD)
    public boolean autoDownloadEnabled;

    @Inject
    @Preference(Constants.P_REPOSITORY_URL)
    public String remote;

    @Inject
    public void setRemote(@Preference(Constants.P_REPOSITORY_URL) String remote) throws Exception {
        for (Runnable r : callbacks) {
            r.run();
        }
    }

    Set<Runnable> callbacks = Sets.newHashSet();

    public void addRemoteUrlChangedCallback(Runnable runnable) {
        callbacks.add(runnable);
    }

    public void removeRemoteUrlChangedCallback(Runnable runnable) {
        callbacks.remove(runnable);
    }

}
