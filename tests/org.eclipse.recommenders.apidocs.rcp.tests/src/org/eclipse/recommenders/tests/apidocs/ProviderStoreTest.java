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
package org.eclipse.recommenders.tests.apidocs;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.eclipse.recommenders.apidocs.rcp.ApidocProvider;
import org.eclipse.recommenders.internal.apidocs.rcp.ApidocsPreferences;
import org.eclipse.recommenders.internal.apidocs.rcp.ApidocsView;
import org.eclipse.recommenders.internal.apidocs.rcp.SubscriptionManager;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;

public class ProviderStoreTest {

    private ApidocsView view;
    private ApidocProvider provider0;
    private ApidocProvider provider1;
    private ApidocProvider provider2;
    private ApidocProvider additionalProvider;
    private List<ApidocProvider> providers;
    private ApidocsPreferences preferences;

    @Before
    public void setup() {
        preferences = new ApidocsPreferences();

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

    private ApidocProvider createProviderMock(String name) {
        ApidocProvider mock = mock(ApidocProvider.class);
        when(mock.getId()).thenReturn(name);
        return mock;
    }

    private ApidocsView createView(List<ApidocProvider> providers) {
        EventBus bus = mock(EventBus.class);

        SubscriptionManager subManger = mock(SubscriptionManager.class);
        return new ApidocsView(bus, subManger, providers, preferences);
    }

    @Test
    public void removeProvider() {
        view.storeProviderRanking();

        providers.remove(provider1);
        view = createView(providers);

        List<ApidocProvider> expectedProviders = Lists.newArrayList(provider0, provider2);
        assertEquals(view.getProviderRanking(), expectedProviders);
    }

    @Test
    public void addProvider() {
        view.storeProviderRanking();

        providers.add(1, additionalProvider);
        view = createView(providers);

        List<ApidocProvider> expectedProviders =
                Lists.newArrayList(provider0, provider1, provider2, additionalProvider);
        assertEquals(view.getProviderRanking(), expectedProviders);
    }

    @Test
    public void modifyProviderOrder() {
        view.moveAfter(0, 1);
        view.storeProviderRanking();

        providers.remove(provider2);
        providers.add(additionalProvider);
        view = createView(providers);

        List<ApidocProvider> expectedProviders = Lists.newArrayList(provider1, provider0, additionalProvider);
        assertEquals(view.getProviderRanking(), expectedProviders);
    }
}
