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
package org.eclipse.recommenders.extdoc.rcp.scheduling;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.util.concurrent.MoreExecutors.listeningDecorator;
import static java.util.concurrent.Executors.newFixedThreadPool;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.recommenders.extdoc.rcp.Provider;
import org.eclipse.recommenders.extdoc.rcp.scheduling.Events.NewSelectionEvent;
import org.eclipse.recommenders.extdoc.rcp.scheduling.Events.ProviderActivationEvent;
import org.eclipse.recommenders.extdoc.rcp.scheduling.Events.ProviderDeactivationEvent;
import org.eclipse.recommenders.extdoc.rcp.scheduling.Events.ProviderDelayedEvent;
import org.eclipse.recommenders.extdoc.rcp.scheduling.Events.ProviderFailedEvent;
import org.eclipse.recommenders.extdoc.rcp.scheduling.Events.ProviderFinishedEvent;
import org.eclipse.recommenders.extdoc.rcp.scheduling.Events.ProviderFinishedLateEvent;
import org.eclipse.recommenders.extdoc.rcp.scheduling.Events.ProviderNotAvailableEvent;
import org.eclipse.recommenders.extdoc.rcp.scheduling.Events.ProviderStartedEvent;
import org.eclipse.recommenders.extdoc.rcp.scheduling.Events.RenderNowEvent;
import org.eclipse.recommenders.extdoc.rcp.ui.ProviderContentPart;
import org.eclipse.recommenders.rcp.events.JavaSelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.google.common.base.Optional;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

public class ProviderExecutionScheduler {

    private static final int NUMBER_OF_THREADS = 7;
    private static final int SECONDS_FOR_RENDER_TIMEOUT = 2;

    private final ListeningExecutorService pool;
    private final Map<Provider, Future<?>> futures;

    private final List<Provider> providers;
    private final SubscriptionManager subscriptionManager;
    private final ProviderContentPart contentPart;
    private EventBus extdocBus;

    private Boolean isAlreadyRendered = false;
    private JavaSelectionEvent currentSelection;

    public ProviderExecutionScheduler(final List<Provider> providers, final SubscriptionManager subscriptionManager,
            final ProviderContentPart coontentPart, final EventBus extdocBus) {
        this.providers = providers;
        this.extdocBus = extdocBus;
        this.subscriptionManager = subscriptionManager;
        this.contentPart = coontentPart;

        pool = createListeningThreadPool(NUMBER_OF_THREADS);
        futures = newHashMap();
    }

    private static ListeningExecutorService createListeningThreadPool(final int numberOfThreads) {
        final ExecutorService pool = newFixedThreadPool(numberOfThreads);
        final ListeningExecutorService listeningPool = listeningDecorator(pool);
        return listeningPool;
    }

    public void scheduleOnSelection(final JavaSelectionEvent selection) {
        this.currentSelection = selection;
        createNewRenderingPanelInUiThread();
        postInUiThread(new NewSelectionEvent(selection));
        // TODO incrementable latch?
        final CountDownLatch latch = new CountDownLatch(providers.size());

        for (final Provider provider : providers) {
            if (!provider.isEnabled()) {
                latch.countDown();
                continue;
            }

            final Composite composite = contentPart.getRenderingArea(provider);
            final Optional<Method> optMethod = subscriptionManager.findFirstSubscribedMethod(provider, selection);

            if (optMethod.isPresent()) {
                final OnSelectionCallable callable = new OnSelectionCallable(provider, optMethod.get(), selection,
                        composite, latch);
                final ListenableFuture<?> future = pool.submit(callable);
                futures.put(provider, future);

            } else {
                postInUiThread(new ProviderNotAvailableEvent(provider));
                latch.countDown();
            }
        }

        blockUntilAllFinishedOrRenderTimeout(latch);
        postProviderDelayedEventsForLateProviders();
        triggerRenderNow();
    }

    private void createNewRenderingPanelInUiThread() {
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                contentPart.createNewRenderingPanel();
            }
        });
    }

    private void blockUntilAllFinishedOrRenderTimeout(final CountDownLatch l) {
        try {
            l.await(SECONDS_FOR_RENDER_TIMEOUT, TimeUnit.SECONDS);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void postProviderDelayedEventsForLateProviders() {
        for (final Provider provider : providers) {
            final Future<?> future = futures.get(provider);
            if (future != null && !future.isDone()) {
                postInUiThread(new ProviderDelayedEvent(provider));
            }
        }
    }

    private void triggerRenderNow() {
        postInUiThread(new RenderNowEvent());
        isAlreadyRendered = true;
    }

    @Subscribe
    public void handle(final ProviderActivationEvent e) {
        if (isRunning(e.provider)) {
            return;
        }

        final Composite composite = contentPart.getRenderingArea(e.provider);
        final Optional<Method> optMethod = subscriptionManager.findFirstSubscribedMethod(e.provider, currentSelection);

        if (optMethod.isPresent()) {
            final OnActivationCallable callable = new OnActivationCallable(e.provider, optMethod.get(),
                    currentSelection, composite);
            final ListenableFuture<?> future = pool.submit(callable);
            futures.put(e.provider, future);

        } else {
            postInUiThread(new ProviderNotAvailableEvent(e.provider));
        }
    }

    private boolean isRunning(final Provider p) {
        return futures.containsKey(p);
    }

    @Subscribe
    public void handle(final ProviderDeactivationEvent e) {
        final Future<?> future = futures.get(e.provider);
        if (future != null) {
            future.cancel(true);
            // // TODO Review: is needed?
            // postInUiThread(new ProviderExecutionCancelledEvent(e.provider));
        }
    }

    public void dispose() {
        pool.shutdownNow();
        extdocBus = new EventBus();
    }

    private void postInUiThread(final Object event) {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                extdocBus.post(event);
            }
        });
    }

    private class OnActivationCallable implements Callable<Void> {

        private final Provider provider;
        private final Method method;
        private final JavaSelectionEvent selection;
        private final Composite composite;

        public OnActivationCallable(final Provider provider, final Method method, final JavaSelectionEvent selection,
                final Composite composite) {
            this.provider = provider;
            this.method = method;
            this.selection = selection;
            this.composite = composite;
        }

        @Override
        public Void call() throws Exception {
            postInUiThread(new ProviderStartedEvent(provider));

            try {
                invokeProvider();
                if (isTooLate()) {
                    postInUiThread(new ProviderFinishedLateEvent(provider));
                } else {
                    postInUiThread(new ProviderFinishedEvent(provider));
                }
            } catch (final InterruptedException e) {
                // this happens on cancel request. don't propagate
            } catch (final Exception e) {
                postInUiThread(new ProviderFailedEvent(provider, e));
            }

            futures.remove(provider);
            return null;
        }

        protected boolean isTooLate() {
            return false;
        }

        private void invokeProvider() throws Exception {
            if (!method.isAccessible()) {
                method.setAccessible(true);
            }
            method.invoke(provider, selection.getElement(), selection, composite);
        }
    }

    private class OnSelectionCallable extends OnActivationCallable {
        private final CountDownLatch latch;

        public OnSelectionCallable(final Provider provider, final Method method, final JavaSelectionEvent selection,
                final Composite composite, final CountDownLatch latch) {
            super(provider, method, selection, composite);
            this.latch = latch;
        }

        @Override
        public Void call() throws Exception {
            super.call();
            latch.countDown();
            return null;
        }

        @Override
        protected boolean isTooLate() {
            return isAlreadyRendered;
        }
    }
}