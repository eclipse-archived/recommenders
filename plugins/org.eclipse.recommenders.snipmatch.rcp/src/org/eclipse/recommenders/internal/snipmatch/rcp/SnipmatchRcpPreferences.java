/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.snipmatch.rcp;

import javax.inject.Inject;

import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.recommenders.injection.InjectionService;
import org.eclipse.recommenders.internal.snipmatch.rcp.EclipseGitSnippetRepository.SnippetRepositoryUrlChangedEvent;

import com.google.common.eventbus.EventBus;

@SuppressWarnings("restriction")
public class SnipmatchRcpPreferences {

    private String location;
    private EventBus bus = InjectionService.getInstance().requestInstance(EventBus.class);

    @Inject
    public void setLocation(@Preference(Constants.PREF_SNIPPETS_REPO) String newValue) {
        String old = location;
        location = newValue;
        if (old != null) {
            bus.post(new SnippetRepositoryUrlChangedEvent());
        }
    }

    public String getLocation() {
        return location;
    }
}
