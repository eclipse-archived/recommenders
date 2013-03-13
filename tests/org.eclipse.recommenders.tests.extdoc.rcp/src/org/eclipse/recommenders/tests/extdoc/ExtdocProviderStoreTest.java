/**
 * Copyright (c) 2010, 2012 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Patrick Gottschaemmer, Olav Lenz - initial tests
 */
package org.eclipse.recommenders.tests.extdoc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.eclipse.recommenders.extdoc.rcp.providers.ExtdocProvider;
import org.eclipse.recommenders.internal.extdoc.rcp.ui.ExtdocPreferences;
import org.eclipse.recommenders.internal.extdoc.rcp.ui.ExtdocView;
import org.eclipse.recommenders.internal.extdoc.rcp.ui.SubscriptionManager;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;

public class ExtdocProviderStoreTest {

    private ExtdocView view;
    private ExtdocProvider provider0;
    private ExtdocProvider provider1;
    private ExtdocProvider provider2;
    private ExtdocProvider additionalProvider;
    private List<ExtdocProvider> providers;
    private ExtdocPreferences preferences;

    @Before
    public void setup() {
        preferences = new ExtdocPreferences();

        boolean nodeCleared = preferences.clearProviderRankingPreferences();
        if (!nodeCleared) {
            fail("Test fails because of clearing of ProviderRanking store failed.");
        }

        provider0 = createProviderMock("provider0");
        provider1 = createProviderMock("provider1");
        provider2 = createProviderMock("provider2");
        additionalProvider = createProviderMock("additionalProvider");

        providers = Lists.newArrayList(provider0, provider1, provider2);
        view = createView(providers);
    }

    private ExtdocProvider createProviderMock(String name) {
        ExtdocProvider mock = mock(ExtdocProvider.class);
        when(mock.getId()).thenReturn(name);
        return mock;
    }

    private ExtdocView createView(List<ExtdocProvider> providers) {
        EventBus bus = mock(EventBus.class);

        SubscriptionManager subManger = mock(SubscriptionManager.class);
        return new ExtdocView(bus, subManger, providers, preferences);
    }

    @Test
    public void removeProvider() {
        view.storeProviderRanking();

        providers.remove(provider1);
        view = createView(providers);

        List<ExtdocProvider> expectedProviders = Lists.newArrayList(provider0, provider2);
        assertEquals(view.getProviderRanking(), expectedProviders);
    }

    @Test
    public void addProvider() {
        view.storeProviderRanking();

        providers.add(1, additionalProvider);
        view = createView(providers);

        List<ExtdocProvider> expectedProviders = Lists.newArrayList(provider0, provider1, provider2, additionalProvider);
        assertEquals(view.getProviderRanking(), expectedProviders);
    }

    @Test
    public void modifyProviderOrder() {
        view.moveAfter(0, 1);
        view.storeProviderRanking();

        providers.remove(provider2);
        providers.add(additionalProvider);
        view = createView(providers);

        List<ExtdocProvider> expectedProviders = Lists.newArrayList(provider1, provider0, additionalProvider);
        assertEquals(view.getProviderRanking(), expectedProviders);
    }
}
