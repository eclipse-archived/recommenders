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
import static com.google.common.util.concurrent.MoreExecutors.listeningDecorator;
import static java.lang.Thread.MIN_PRIORITY;
import static org.eclipse.recommenders.extdoc.rcp.providers.ExtdocProvider.Status.NOT_AVAILABLE;
import static org.eclipse.recommenders.utils.Checks.cast;
import static org.eclipse.recommenders.utils.Executors.coreThreadsTimoutExecutor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eclipse.recommenders.extdoc.rcp.providers.ExtdocProvider;
import org.eclipse.recommenders.extdoc.rcp.providers.ExtdocProvider.Status;
import org.eclipse.recommenders.internal.extdoc.rcp.scheduling.Events.NewSelectionEvent;
import org.eclipse.recommenders.internal.extdoc.rcp.scheduling.Events.ProviderActivationEvent;
import org.eclipse.recommenders.internal.extdoc.rcp.scheduling.Events.ProviderDeactivationEvent;
import org.eclipse.recommenders.internal.extdoc.rcp.scheduling.Events.ProviderDelayedEvent;
import org.eclipse.recommenders.internal.extdoc.rcp.scheduling.Events.ProviderFailedEvent;
import org.eclipse.recommenders.internal.extdoc.rcp.scheduling.Events.ProviderFinishedEvent;
import org.eclipse.recommenders.internal.extdoc.rcp.scheduling.Events.ProviderFinishedLateEvent;
import org.eclipse.recommenders.internal.extdoc.rcp.scheduling.Events.ProviderNotAvailableEvent;
import org.eclipse.recommenders.internal.extdoc.rcp.scheduling.Events.ProviderStartedEvent;
import org.eclipse.recommenders.internal.extdoc.rcp.scheduling.Events.RenderNowEvent;
import org.eclipse.recommenders.internal.extdoc.rcp.ui.ProviderContentPart;
import org.eclipse.recommenders.rcp.events.JavaSelectionEvent;
import org.eclipse.recommenders.utils.annotations.Testing;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.google.common.base.Optional;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

public class ProviderExecutionScheduler {

    @Testing("visibility allows to set test values")
    protected static int RENDER_TIMEOUT_IN_MS = 2000;
    private static final int NUMBER_OF_THREADS = 7;

    private static int poolId;

    private static ListeningExecutorService createListeningThreadPool(final int numberOfThreads) {
        final String threadPoolId = "Recommenders-extdoc-pool-" + poolId++ + "thread-";
        final ThreadPoolExecutor pool = coreThreadsTimoutExecutor(NUMBER_OF_THREADS, MIN_PRIORITY, threadPoolId);
        final ListeningExecutorService listeningPool = listeningDecorator(pool);
        return listeningPool;
    }

    private final ListeningExecutorService pool;
    private final Map<ExtdocProvider, Future<?>> futures;

    private final List<ExtdocProvider> providers;
    private final SubscriptionManager subscriptionManager;
    private final ProviderContentPart contentPart;
    private EventBus extdocBus;

    private Boolean isAlreadyRendered = false;
    private JavaSelectionEvent currentSelection;
    private final CountDownLatch latch;

    public ProviderExecutionScheduler(final List<ExtdocProvider> providers,
            final SubscriptionManager subscriptionManager, final ProviderContentPart contentPart,
            final EventBus extdocBus) {
        this.providers = providers;
        this.extdocBus = extdocBus;
        this.subscriptionManager = subscriptionManager;
        this.contentPart = contentPart;

        pool = createListeningThreadPool(NUMBER_OF_THREADS);
        futures = newHashMap();
        extdocBus.register(this);

        // ensure latch is always initialized to handle race condition in case
        // of an early dispose
        latch = new CountDownLatch(providers.size());
    }

    public void scheduleOnSelection(final JavaSelectionEvent selection) {
        this.currentSelection = selection;
        createNewRenderingPanelInUiThread();
        postInUiThread(new NewSelectionEvent(selection));

        for (final ExtdocProvider provider : providers) {
            if (!provider.isEnabled()) {
                latch.countDown();
                continue;
            }

            final Composite composite = contentPart.getRenderingArea(provider);
            final Optional<Method> optMethod = subscriptionManager.findSubscribedMethod(provider, selection);

            if (optMethod.isPresent()) {
                final OnSelectionCallable callable =
                        new OnSelectionCallable(provider, optMethod.get(), selection, composite, latch);
                try {

                    final ListenableFuture<?> future = pool.submit(callable);
                    futures.put(provider, future);
                } catch (final RejectedExecutionException e) {
                    // happens if scheduler is already disposed
                    latch.countDown();
                }
            } else {
                postInUiThread(new ProviderNotAvailableEvent(provider));
                latch.countDown();
            }
        }

        blockUntilAllFinishedOrRenderTimeout();
        postProviderDelayedEventsForLateProviders();
        triggerRenderNow();
    }

    private void createNewRenderingPanelInUiThread() {
        // if (contentPart.isDisposed()) {
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                contentPart.createNewRenderingPanel();
            }
        });
        // }
    }

    protected void blockUntilAllFinishedOrRenderTimeout() {
        try {
            latch.await(RENDER_TIMEOUT_IN_MS, TimeUnit.MILLISECONDS);
        } catch (final InterruptedException e) {
            // ignore
        }
    }

    private void postProviderDelayedEventsForLateProviders() {
        for (final ExtdocProvider provider : providers) {
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
    public void onEvent(final ProviderActivationEvent e) {
        if (isRunning(e.provider)) {
            return;
        }

        final Composite composite = contentPart.getRenderingArea(e.provider);
        final Optional<Method> optMethod = subscriptionManager.findSubscribedMethod(e.provider, currentSelection);

        if (optMethod.isPresent()) {
            final OnActivationCallable callable =
                    new OnActivationCallable(e.provider, optMethod.get(), currentSelection, composite);
            if (pool.isShutdown()) {
                return;
            }
            final ListenableFuture<?> future = pool.submit(callable);
            futures.put(e.provider, future);

        } else {
            postInUiThread(new ProviderNotAvailableEvent(e.provider));
        }
    }

    private boolean isRunning(final ExtdocProvider p) {
        return futures.containsKey(p);
    }

    @Subscribe
    public void onEvent(final ProviderDeactivationEvent e) {
        final Future<?> future = futures.get(e.provider);
        if (future != null) {
            future.cancel(true);
        }
    }

    public void dispose() {
        pool.shutdownNow();
        // pool = null;
        extdocBus.unregister(this);
        extdocBus = new EventBus();
        countLatchToZero();
    }

    private void countLatchToZero() {
        for (int i = 0; i < providers.size(); i++) {
            latch.countDown();
        }
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

        private final ExtdocProvider provider;
        private final Method method;
        private final JavaSelectionEvent selection;
        private final Composite composite;

        public OnActivationCallable(final ExtdocProvider provider, final Method method,
                final JavaSelectionEvent selection, final Composite composite) {
            this.provider = provider;
            this.method = method;
            this.selection = selection;
            this.composite = composite;
        }

        @Override
        public Void call() throws Exception {
            postInUiThread(new ProviderStartedEvent(provider));

            try {
                final Status returnStatus = invokeProvider();
                if (NOT_AVAILABLE.equals(returnStatus)) {
                    postInUiThread(new ProviderNotAvailableEvent(provider, isTooLate()));
                } else if (isTooLate()) {
                    postInUiThread(new ProviderFinishedLateEvent(provider));
                } else {
                    postInUiThread(new ProviderFinishedEvent(provider));
                }
            } catch (final InterruptedException e) {
                // this happens on cancel request. don't propagate
            } catch (final InvocationTargetException e) {
                // unwrap to increase testability
                postInUiThread(new ProviderFailedEvent(provider, e.getTargetException()));
            } catch (final Exception e) {
                postInUiThread(new ProviderFailedEvent(provider, e));
            }

            futures.remove(provider);
            return null;
        }

        protected boolean isTooLate() {
            return false;
        }

        private Status invokeProvider() throws Exception {
            if (!method.isAccessible()) {
                method.setAccessible(true);
            }
            final Object returnValue = method.invoke(provider, selection.getElement(), selection, composite);
            final Status status = cast(returnValue);
            return status;
        }
    }

    private class OnSelectionCallable extends OnActivationCallable {
        private final CountDownLatch latch;

        public OnSelectionCallable(final ExtdocProvider provider, final Method method,
                final JavaSelectionEvent selection, final Composite composite, final CountDownLatch latch) {
            super(provider, method, selection, composite);
            this.latch = latch;
        }

        @Override
        public Void call() throws Exception {
            try {
                super.call();
                return null;
            } finally {
                latch.countDown();
            }
        }

        @Override
        protected boolean isTooLate() {
            return isAlreadyRendered;
        }
    }
}