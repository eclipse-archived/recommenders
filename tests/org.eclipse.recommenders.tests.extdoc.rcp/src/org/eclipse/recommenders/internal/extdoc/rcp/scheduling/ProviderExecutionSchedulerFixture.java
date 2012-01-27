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
package org.eclipse.recommenders.internal.extdoc.rcp.scheduling;

import static com.google.common.collect.Maps.newHashMap;
import static java.util.Arrays.asList;
import static org.eclipse.recommenders.internal.extdoc.rcp.scheduling.helper.JavaSelectionTestUtils.TYPE_IN_TYPE_DECLARATION;
import static org.eclipse.recommenders.rcp.events.JavaSelectionEvent.JavaSelectionLocation.METHOD_BODY;
import static org.eclipse.recommenders.utils.Checks.ensureIsNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.recommenders.extdoc.rcp.providers.ExtdocProvider;
import org.eclipse.recommenders.extdoc.rcp.providers.JavaSelectionSubscriber;
import org.eclipse.recommenders.internal.extdoc.rcp.ui.ProviderContentPart;
import org.eclipse.recommenders.rcp.events.JavaSelectionEvent;
import org.eclipse.swt.widgets.Composite;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;

public class ProviderExecutionSchedulerFixture {

    protected static final int RENDER_TIMEOUT = 125;
    protected static final int TIME_OF_SLOW_PROVIDERS = 250;

    public static final JavaSelectionEvent SELECTION = TYPE_IN_TYPE_DECLARATION;

    public final ExtdocProviderHelper happyPathProvider;
    public final ExtdocProviderHelper failingProvider;
    public final ExtdocProviderHelper unavailableProvider;
    public final ExtdocProviderHelper slowProvider;
    public final ExtdocProviderHelper slowFailingProvider;
    public final ExtdocProviderHelper slowUnavailableProvider;
    public final ExtdocProviderHelper uninterestedProvider;

    public final EventBus bus;
    public final EventBus mockBus;
    public final ProviderContentPart contentPart;

    public final Map<ExtdocProviderHelper, Composite> composites = newHashMap();

    public RuntimeException lastThrownException;

    public ProviderExecutionSchedulerFixture() {

        bus = new EventBus();
        mockBus = mock(EventBus.class);
        contentPart = mock(ProviderContentPart.class);

        happyPathProvider = createProvider(false, false, true);
        failingProvider = createProvider(false, true, true);
        unavailableProvider = createProvider(false, false, false);
        slowProvider = createProvider(true, false, true);
        slowFailingProvider = createProvider(true, true, true);
        slowUnavailableProvider = createProvider(true, false, false);
        uninterestedProvider = new ExtdocProviderHelper() {
            @Override
            @JavaSelectionSubscriber(METHOD_BODY)
            public Status methodToCall(final IJavaElement e, final JavaSelectionEvent s, final Composite p)
                    throws InterruptedException {
                countExecution();
                return Status.OK;
            }
        };
        registerCompositeForProvider(uninterestedProvider);

    }

    private ExtdocProviderHelper createProvider(final Boolean shouldSleep, final boolean shouldCrash,
            final boolean isAvailable) {

        final ExtdocProviderHelper provider = new ExtdocProviderHelper() {
            @Override
            @JavaSelectionSubscriber
            public Status methodToCall(final IJavaElement e, final JavaSelectionEvent s, final Composite c)
                    throws InterruptedException {
                selection = s;
                composite = c;

                countExecution();

                if (shouldSleep) {
                    Thread.sleep(TIME_OF_SLOW_PROVIDERS);
                }

                if (shouldCrash) {
                    lastThrownException = new RuntimeException("some exception that occurs on execution");
                    throw lastThrownException;
                }

                if (isAvailable) {
                    return Status.OK;
                } else {
                    return Status.NOT_AVAILABLE;
                }
            }
        };
        registerCompositeForProvider(provider);

        return provider;
    }

    private void registerCompositeForProvider(final ExtdocProviderHelper provider) {
        final Composite c = mock(Composite.class);
        when(contentPart.getRenderingArea(provider)).thenReturn(c);
        composites.put(provider, c);
    }

    public ProviderExecutionScheduler createScheduler(final ExtdocProvider... providerArray) {
        final List<ExtdocProvider> providers = asList(providerArray);
        final ProviderExecutionScheduler scheduler = new ProviderExecutionScheduler(providers, new SubscriptionManager(
                providers), contentPart, bus) {
            {
                RENDER_TIMEOUT_IN_MS = RENDER_TIMEOUT;
            }
        };
        return scheduler;
    }

    public ProviderExecutionScheduler createSchedulerWithMockedBus() {
        final List<ExtdocProvider> providers = Lists.newArrayList();
        final SubscriptionManager subscriptionManager = mock(SubscriptionManager.class);
        final ProviderContentPart contentPart = mock(ProviderContentPart.class);
        return new ProviderExecutionScheduler(providers, subscriptionManager, contentPart, mockBus);
    }

    public static abstract class ExtdocProviderHelper extends ExtdocProvider {
        int counter = 0;
        JavaSelectionEvent selection;
        Composite composite;

        public void countExecution() {
            counter++;
        }

        public void assertExecution() {
            assertTrue(counter > 0);
        }

        public void assertExecution(final int times) {
            assertTrue(counter == times);
        }

        public void assertParameters(final JavaSelectionEvent selection, final Composite composite) {
            ensureIsNotNull(this.selection, "selection must be remembered in helper implementation");
            assertEquals(selection, this.selection);

            ensureIsNotNull(this.composite, "composite must be remembered in helper implementation");
            assertEquals(composite, this.composite);
        }

        public abstract Status methodToCall(IJavaElement e, JavaSelectionEvent s, Composite p)
                throws InterruptedException;
    }

    public Composite getComposite(final ExtdocProviderHelper provider) {
        final Composite c = composites.get(provider);
        ensureIsNotNull(c);
        return c;
    }
}