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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.recommenders.apidocs.rcp.ApidocProvider;
import org.eclipse.recommenders.internal.apidocs.rcp.ApidocsPreferences;
import org.eclipse.recommenders.internal.apidocs.rcp.ApidocsView;
import org.eclipse.recommenders.internal.apidocs.rcp.SubscriptionManager;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;

public class DnDTest {

    private ApidocsView view;
    private List<ApidocProvider> defaultOrder;
    private ApidocProvider provider0;
    private ApidocProvider provider1;
    private ApidocProvider provider2;
    private ApidocProvider provider3;
    private ApidocProvider provider4;

    @Before
    public void setup() {
        EventBus bus = mock(EventBus.class);
        SubscriptionManager subManger = mock(SubscriptionManager.class);

        provider0 = createProviderMock("provider0");
        provider1 = createProviderMock("provider1");
        provider2 = createProviderMock("provider2");
        provider3 = createProviderMock("provider3");
        provider4 = createProviderMock("provider4");

        List<ApidocProvider> providers = Lists.newArrayList(provider0, provider1, provider2, provider3, provider4);
        defaultOrder = new LinkedList<ApidocProvider>(providers);

        ApidocsPreferences preferences = mock(ApidocsPreferences.class);
        view = new ApidocsView(bus, subManger, providers, preferences);
    }

    private ApidocProvider createProviderMock(String name) {
        ApidocProvider mock = mock(ApidocProvider.class);
        when(mock.getId()).thenReturn(name);
        return mock;
    }

    @Test
    public void moveOnItselfBefore() {
        view.moveBefore(1, 1);

        assertEquals(defaultOrder, view.getProviderRanking());
    }

    @Test
    public void moveOnItselfAfter() {
        view.moveAfter(0, 0);

        assertEquals(defaultOrder, view.getProviderRanking());
    }

    @Test
    public void moveAfterNoChange() {
        view.moveAfter(3, 2);

        assertEquals(defaultOrder, view.getProviderRanking());
    }

    @Test
    public void moveBeforeNoChange() {
        view.moveBefore(2, 3);

        assertEquals(defaultOrder, view.getProviderRanking());
    }

    @Test
    public void moveBeforeTop() {
        view.moveBefore(4, 0);

        List<ApidocProvider> expectedOrder = Lists.newArrayList(provider4, provider0, provider1, provider2, provider3);

        assertEquals(expectedOrder, view.getProviderRanking());
    }

    @Test
    public void moveAfterTop() {
        view.moveAfter(2, 0);

        List<ApidocProvider> expectedOrder = Lists.newArrayList(provider0, provider2, provider1, provider3, provider4);

        assertEquals(expectedOrder, view.getProviderRanking());
    }

    @Test
    public void moveBeforeBottom() {
        view.moveBefore(2, 4);

        List<ApidocProvider> expectedOrder = Lists.newArrayList(provider0, provider1, provider3, provider2, provider4);

        assertEquals(expectedOrder, view.getProviderRanking());
    }

    @Test
    public void moveAfterBottom() {
        view.moveAfter(2, 4);

        List<ApidocProvider> expectedOrder = Lists.newArrayList(provider0, provider1, provider3, provider4, provider2);

        assertEquals(expectedOrder, view.getProviderRanking());
    }
}
