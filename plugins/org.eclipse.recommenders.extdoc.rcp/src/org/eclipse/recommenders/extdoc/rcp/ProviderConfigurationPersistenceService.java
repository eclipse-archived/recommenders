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
package org.eclipse.recommenders.extdoc.rcp;

import java.util.List;

import org.eclipse.recommenders.extdoc.rcp.scheduling.Events.ProviderActivationEvent;
import org.eclipse.recommenders.extdoc.rcp.scheduling.Events.ProviderDeactivationEvent;
import org.eclipse.recommenders.extdoc.rcp.scheduling.Events.ProviderOrderChangedEvent;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

public class ProviderConfigurationPersistenceService {

    private final PreferencesFacade preferences;
    private List<Provider> providers;

    @Inject
    public ProviderConfigurationPersistenceService(PreferencesFacade preferences) {
        this.preferences = preferences;
    }

    public void initializeProviderConfiguration(List<Provider> providers) {
        this.providers = providers;
        loadNamesAndReorderList();
        loadEnablement();
    }

    private void loadNamesAndReorderList() {
        String[] orderedProviderNames = preferences.loadOrderedProviderNames();
        for (int i = orderedProviderNames.length - 1; i >= 0; i--) {
            moveToTopIfNameExists(orderedProviderNames[i]);
        }
    }

    private void moveToTopIfNameExists(String nameToMove) {
        for (Provider p : providers) {
            if (nameToMove.equals(p.getDescription().getName())) {
                providers.remove(p);
                providers.add(0, p);
                return;
            }
        }
    }

    private void loadEnablement() {
        for (Provider p : providers) {
            boolean isEnabledInPrefs = preferences.isProviderEnabled(p);
            p.setEnabled(isEnabledInPrefs);
        }
    }

    @Subscribe
    public void onEvent(ProviderOrderChangedEvent e) {
        preferences.storeOrderedProviders(providers);
    }

    @Subscribe
    public void onEvent(ProviderActivationEvent e) {
        preferences.storeProviderEnablement(providers);
    }

    @Subscribe
    public void onEvent(ProviderDeactivationEvent e) {
        preferences.storeProviderEnablement(providers);
    }
}