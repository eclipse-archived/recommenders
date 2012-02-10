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

import static org.eclipse.recommenders.internal.extdoc.rcp.scheduling.ProviderExecutionSchedulerFixture.RENDER_TIMEOUT;
import static org.eclipse.recommenders.internal.extdoc.rcp.scheduling.ProviderExecutionSchedulerFixture.SELECTION;
import static org.eclipse.recommenders.internal.extdoc.rcp.scheduling.ProviderExecutionSchedulerFixture.TIME_OF_SLOW_PROVIDERS;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

import org.eclipse.recommenders.extdoc.rcp.providers.ExtdocProvider;
import org.eclipse.recommenders.internal.extdoc.rcp.scheduling.Events.NewSelectionEvent;
import org.eclipse.recommenders.internal.extdoc.rcp.scheduling.Events.ProviderActivationEvent;
import org.eclipse.recommenders.internal.extdoc.rcp.scheduling.Events.ProviderDelayedEvent;
import org.eclipse.recommenders.internal.extdoc.rcp.scheduling.Events.ProviderFailedEvent;
import org.eclipse.recommenders.internal.extdoc.rcp.scheduling.Events.ProviderFinishedEvent;
import org.eclipse.recommenders.internal.extdoc.rcp.scheduling.Events.ProviderFinishedLateEvent;
import org.eclipse.recommenders.internal.extdoc.rcp.scheduling.Events.ProviderNotAvailableEvent;
import org.eclipse.recommenders.internal.extdoc.rcp.scheduling.Events.ProviderStartedEvent;
import org.eclipse.recommenders.internal.extdoc.rcp.scheduling.Events.RenderNowEvent;
import org.eclipse.recommenders.internal.extdoc.rcp.scheduling.ProviderExecutionSchedulerFixture.ExtdocProviderHelper;
import org.eclipse.recommenders.utils.Throws;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class ProviderExecutionSchedulerTest {

    ProviderExecutionSchedulerFixture fixture;
    BusSpyThatAlsoChecksForUiThread spy;
    ProviderExecutionScheduler sut;

    @Before
    public void setup() {
        fixture = new ProviderExecutionSchedulerFixture();
        spy = new BusSpyThatAlsoChecksForUiThread();
        fixture.bus.register(spy);
    }

    @Test
    @Ignore
    public void assertTestIsNotRunInUIThread() {
        final Thread actual = Thread.currentThread();
        final Thread unexpected = Display.getDefault().getThread();
        assertNotSame(unexpected, actual);
    }

    @Test
    public void ensureSchedulerRegisteresOnEventbus() {
        sut = fixture.createSchedulerWithMockedBus();
        verify(fixture.mockBus).register(sut);
    }

    @Test
    public void ensureSchedulerUnregisteresFromEventbusOnDispose() {
        sut = fixture.createSchedulerWithMockedBus();
        sut.dispose();
        verify(fixture.mockBus).unregister(sut);
    }

    @Test
    public void providersAreExecuted() throws InterruptedException {
        createSchedulerFireSelectionAndWait(fixture.happyPathProvider);
        assertExecution(fixture.happyPathProvider);
    }

    @Test
    public void providersAreExecutedOnActivation() {
        createSchedulerFireAsActivationAndWait(fixture.happyPathProvider);
        assertExecution(fixture.happyPathProvider);
    }

    @Test
    public void renderingPanelsAreCreatedOnNewSelection() throws InterruptedException {
        createSchedulerFireSelectionAndWait(fixture.happyPathProvider);
        verify(fixture.contentPart).createNewRenderingPanel();
    }

    @Test
    public void correctParametersAreProvidedToListeners() {
        createSchedulerFireSelectionAndWait(fixture.happyPathProvider, fixture.slowProvider);

        assertParameters(fixture.happyPathProvider);
        assertParameters(fixture.slowProvider);
    }

    @Test
    public void processingOfHappyPathProvider() {
        createSchedulerFireSelectionAndWait(fixture.happyPathProvider);

        spy.assertNextEvent(new NewSelectionEvent(SELECTION));
        spy.assertNextEvent(new ProviderStartedEvent(fixture.happyPathProvider));
        spy.assertNextEvent(new ProviderFinishedEvent(fixture.happyPathProvider));
        spy.assertNextEvent(new RenderNowEvent());
        spy.assertNoMoreEvents();
    }

    @Test
    public void processingOfSlowProvider() {
        createSchedulerFireSelectionAndWait(fixture.slowProvider);

        spy.assertNextEvent(new NewSelectionEvent(SELECTION));
        spy.assertNextEvent(new ProviderStartedEvent(fixture.slowProvider));
        spy.assertNextEvent(new ProviderDelayedEvent(fixture.slowProvider));
        spy.assertNextEvent(new RenderNowEvent());
        spy.assertNextEvent(new ProviderFinishedLateEvent(fixture.slowProvider));
        spy.assertNoMoreEvents();
    }

    @Test
    public void processingOfFailingProvider() {
        createSchedulerFireSelectionAndWait(fixture.failingProvider);

        spy.assertNextEvent(new NewSelectionEvent(SELECTION));
        spy.assertNextEvent(new ProviderStartedEvent(fixture.failingProvider));
        spy.assertNextEvent(new ProviderFailedEvent(fixture.failingProvider, fixture.lastThrownException));
        spy.assertNextEvent(new RenderNowEvent());
        spy.assertNoMoreEvents();
    }

    @Test
    public void processingOfSlowFailingProvider() {
        createSchedulerFireSelectionAndWait(fixture.slowFailingProvider);

        spy.assertNextEvent(new NewSelectionEvent(SELECTION));
        spy.assertNextEvent(new ProviderStartedEvent(fixture.slowFailingProvider));
        spy.assertNextEvent(new ProviderDelayedEvent(fixture.slowFailingProvider));
        spy.assertNextEvent(new RenderNowEvent());
        spy.assertNextEvent(new ProviderFailedEvent(fixture.slowFailingProvider, fixture.lastThrownException));
        spy.assertNoMoreEvents();
    }

    @Test
    public void processingOfUnavailableProvider() {
        createSchedulerFireSelectionAndWait(fixture.unavailableProvider);

        spy.assertNextEvent(new NewSelectionEvent(SELECTION));
        spy.assertNextEvent(new ProviderStartedEvent(fixture.unavailableProvider));
        spy.assertNextEvent(new ProviderNotAvailableEvent(fixture.unavailableProvider));
        spy.assertNextEvent(new RenderNowEvent());
        spy.assertNoMoreEvents();
    }

    @Test
    public void processingOfSlowUnavailableProvider() {
        createSchedulerFireSelectionAndWait(fixture.slowUnavailableProvider);

        spy.assertNextEvent(new NewSelectionEvent(SELECTION));
        spy.assertNextEvent(new ProviderStartedEvent(fixture.slowUnavailableProvider));
        spy.assertNextEvent(new ProviderDelayedEvent(fixture.slowUnavailableProvider));
        spy.assertNextEvent(new RenderNowEvent());
        spy.assertNextEvent(new ProviderNotAvailableEvent(fixture.slowUnavailableProvider, true));
        spy.assertNoMoreEvents();
    }

    @Test
    public void processingOfUninterestedProvider() {
        createSchedulerFireSelectionAndWait(fixture.uninterestedProvider);

        spy.assertNextEvent(new NewSelectionEvent(SELECTION));
        spy.assertNextEvent(new ProviderNotAvailableEvent(fixture.uninterestedProvider));
        spy.assertNextEvent(new RenderNowEvent());
        spy.assertNoMoreEvents();
    }

    @Test
    public void processingOfUninterestedProviderOnActivation() {
        createSchedulerFireAsActivationAndWait(fixture.uninterestedProvider);

        spy.assertNextEvent(new NewSelectionEvent(SELECTION));
        spy.assertNextEvent(new RenderNowEvent());
        spy.assertNextEvent(new ProviderNotAvailableEvent(fixture.uninterestedProvider));
        spy.assertNoMoreEvents();
    }

    @Test
    public void complexExampleContainsAllExpectedEvents() {
        createSchedulerFireSelectionAndWait(fixture.happyPathProvider, fixture.failingProvider, fixture.slowProvider,
                fixture.slowUnavailableProvider);

        spy.assertNextEvent(new NewSelectionEvent(SELECTION));

        spy.assertEvent(new ProviderStartedEvent(fixture.happyPathProvider));
        spy.assertEvent(new ProviderStartedEvent(fixture.failingProvider));
        spy.assertEvent(new ProviderStartedEvent(fixture.slowProvider));
        spy.assertEvent(new ProviderStartedEvent(fixture.slowUnavailableProvider));

        spy.assertEvent(new ProviderFinishedEvent(fixture.happyPathProvider));
        spy.assertEvent(new ProviderFailedEvent(fixture.failingProvider, fixture.lastThrownException));
        spy.assertEvent(new ProviderDelayedEvent(fixture.slowProvider));
        spy.assertEvent(new ProviderDelayedEvent(fixture.slowUnavailableProvider));

        spy.assertNextEvent(new RenderNowEvent());

        spy.assertEvent(new ProviderFinishedLateEvent(fixture.slowProvider));
        spy.assertEvent(new ProviderNotAvailableEvent(fixture.slowUnavailableProvider, true));

        spy.assertNoMoreEvents();
    }

    @Test
    public void renderTimeoutIsShortenedIfAllProvidersFinishEarly() {
        final long startTime = System.currentTimeMillis();
        createSchedulerAndFireSelection(fixture.happyPathProvider, fixture.failingProvider,
                fixture.uninterestedProvider, fixture.unavailableProvider);
        final long duration = System.currentTimeMillis() - startTime;
        assertTrue(duration < RENDER_TIMEOUT);
    }

    private void createSchedulerAndFireSelection(final ExtdocProviderHelper... providers) {
        sut = fixture.createScheduler(providers);
        sut.scheduleOnSelection(SELECTION);
    }

    private void createSchedulerFireSelectionAndWait(final ExtdocProviderHelper... providers) {
        createSchedulerAndFireSelection(providers);
        waitForFinish();
    }

    private void createSchedulerFireAsActivationAndWait(final ExtdocProviderHelper... providers) {
        for (final ExtdocProvider provider : providers) {
            provider.setEnabled(false);
        }
        createSchedulerFireSelectionAndWait(providers);
        for (final ExtdocProvider provider : providers) {
            provider.setEnabled(false);
            sut.onEvent(new ProviderActivationEvent(provider));
        }
        waitForFinish();
    }

    private void waitForFinish() {

        try {
            Thread.sleep(TIME_OF_SLOW_PROVIDERS + 100);
        } catch (final InterruptedException e) {
            Throws.throwUnhandledException(e);
        }

        // needed to run as junit (non plugin) test
        // while (Display.getDefault().readAndDispatch()) {
        // ;
        // }
    }

    private void assertExecution(final ExtdocProviderHelper provider) {
        provider.assertExecution(1);
    }

    private void assertParameters(final ExtdocProviderHelper provider) {
        final Composite composite = fixture.getComposite(provider);
        provider.assertParameters(SELECTION, composite);
    }
}