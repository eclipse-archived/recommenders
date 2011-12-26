/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Sebastian Proksch - initial API and implementation
 */
package org.eclipse.recommenders.internal.extdoc.rcp.preferences;

import java.util.List;

import org.eclipse.recommenders.extdoc.rcp.providers.ExtdocProvider;
import org.eclipse.recommenders.internal.extdoc.rcp.scheduling.Events.ProviderActivationEvent;
import org.eclipse.recommenders.internal.extdoc.rcp.scheduling.Events.ProviderDeactivationEvent;
import org.eclipse.recommenders.internal.extdoc.rcp.scheduling.Events.ProviderOrderChangedEvent;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

public class ProviderConfigurationPersistenceService {

    private final PreferencesFacade preferences;
    private List<ExtdocProvider> providers;

    @Inject
    public ProviderConfigurationPersistenceService(final PreferencesFacade preferences) {
        this.preferences = preferences;
    }

    public void initializeProviderConfiguration(final List<ExtdocProvider> providers) {
        this.providers = providers;
        loadNamesAndReorderList();
        loadEnablement();
    }

    private void loadNamesAndReorderList() {
        final String[] orderedProviderNames = preferences.loadOrderedProviderNames();
        for (int i = orderedProviderNames.length - 1; i >= 0; i--) {
            moveToTopIfNameExists(orderedProviderNames[i]);
        }
    }

    private void moveToTopIfNameExists(final String nameToMove) {
        for (final ExtdocProvider p : providers) {
            if (nameToMove.equals(p.getDescription().getName())) {
                providers.remove(p);
                providers.add(0, p);
                return;
            }
        }
    }

    private void loadEnablement() {
        for (final ExtdocProvider p : providers) {
            final boolean isEnabledInPrefs = preferences.isProviderEnabled(p);
            p.setEnabled(isEnabledInPrefs);
        }
    }

    @Subscribe
    public void onEvent(final ProviderOrderChangedEvent e) {
        preferences.storeOrderedProviders(providers);
    }

    @Subscribe
    public void onEvent(final ProviderActivationEvent e) {
        preferences.storeProviderEnablement(providers);
    }

    @Subscribe
    public void onEvent(final ProviderDeactivationEvent e) {
        preferences.storeProviderEnablement(providers);
    }
}