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

import javax.inject.Inject;

import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.recommenders.injection.InjectionService;
import org.eclipse.recommenders.models.rcp.ModelEvents.ModelRepositoryUrlChangedEvent;

import com.google.common.eventbus.EventBus;

@SuppressWarnings("restriction")
public class ModelsRcpPreferences {

    @Inject
    @Preference(P_REPOSITORY_ENABLE_AUTO_DOWNLOAD)
    public boolean autoDownloadEnabled;

    public String remote;

    private EventBus bus = InjectionService.getInstance().requestInstance(EventBus.class);;

    @Inject
    void setRemote(@Preference(Constants.P_REPOSITORY_URL) String newRemote) throws Exception {
        String old = remote;
        remote = newRemote;
        if (old != null && !remote.equals(old)) {
            bus.post(new ModelRepositoryUrlChangedEvent());
        }
    }
}
