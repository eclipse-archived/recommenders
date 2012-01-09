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

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.eclipse.recommenders.extdoc.rcp.providers.ExtdocProvider;
import org.eclipse.recommenders.extdoc.rcp.providers.ExtdocProviderDescription;
import org.eclipse.recommenders.internal.extdoc.rcp.scheduling.Events.ProviderActivationEvent;
import org.eclipse.recommenders.internal.extdoc.rcp.scheduling.Events.ProviderDeactivationEvent;
import org.eclipse.recommenders.internal.extdoc.rcp.scheduling.Events.ProviderOrderChangedEvent;
import org.junit.Before;
import org.junit.Test;

public class ProviderConfigurationPersistenceServiceTest {

    private List<ExtdocProvider> providers;
    private PreferencesFacade preferences;

    public ProviderConfigurationPersistenceService sut;

    @Before
    public void setup() {
        providers = newArrayList();
        mockPreferences();

        sut = new ProviderConfigurationPersistenceService(preferences);
    }

    @Test
    public void providersAreSortedOnInit() {
        providers.add(mockProvider("b", true));
        providers.add(mockProvider("c", true));
        providers.add(mockProvider("a", true));

        sut.initializeProviderConfiguration(providers);

        assertOrder("a", "b", "c");
    }

    @Test
    public void missingProvidersAreSimpleLeftOutOnInit() {
        providers.add(mockProvider("b", true));
        providers.add(mockProvider("a", true));

        sut.initializeProviderConfiguration(providers);

        assertOrder("a", "b");
    }

    @Test
    public void newProvidersAreAppendedToTheEnd() {
        providers.add(mockProvider("b", true));
        providers.add(mockProvider("a", true));
        providers.add(mockProvider("d", true));

        sut.initializeProviderConfiguration(providers);

        assertOrder("a", "b", "d");
    }

    @Test
    public void enablementIsLoadedOnInit() {
        ExtdocProvider enabledProvider = mockProvider("a", true);
        providers.add(enabledProvider);
        ExtdocProvider disabledProvider = mockProvider("b", false);
        providers.add(disabledProvider);

        sut.initializeProviderConfiguration(providers);

        verify(enabledProvider).setEnabled(true);
        verify(disabledProvider).setEnabled(false);
    }

    @Test
    public void orderChangeIsPersisted() {
        sut.initializeProviderConfiguration(providers);
        sut.onEvent(mock(ProviderOrderChangedEvent.class));
        verify(preferences).storeOrderedProviders(providers);
    }

    @Test
    public void providerActivationIsPersisted() {
        sut.initializeProviderConfiguration(providers);
        sut.onEvent(mock(ProviderActivationEvent.class));
        verify(preferences).storeProviderEnablement(providers);
    }

    @Test
    public void providerDeactivationIsPersisted() {
        sut.initializeProviderConfiguration(providers);
        sut.onEvent(mock(ProviderDeactivationEvent.class));
        verify(preferences).storeProviderEnablement(providers);
    }

    private void mockPreferences() {
        preferences = mock(PreferencesFacade.class);
        when(preferences.loadOrderedProviderNames()).thenReturn(new String[] { "a", "b", "c" });
    }

    private ExtdocProvider mockProvider(String name, boolean isEnabled) {
        ExtdocProvider provider = mock(ExtdocProvider.class);

        when(provider.isEnabled()).thenReturn(isEnabled);

        ExtdocProviderDescription desc = new ExtdocProviderDescription(name, null);
        when(provider.getDescription()).thenReturn(desc);

        when(preferences.isProviderEnabled(provider)).thenReturn(isEnabled);

        return provider;
    }

    private void assertOrder(String... orderedNames) {
        int idx = 0;
        for (ExtdocProvider provider : providers) {
            String actual = provider.getDescription().getName();
            String expected = orderedNames[idx++];
            assertEquals(expected, actual);
        }
    }
}