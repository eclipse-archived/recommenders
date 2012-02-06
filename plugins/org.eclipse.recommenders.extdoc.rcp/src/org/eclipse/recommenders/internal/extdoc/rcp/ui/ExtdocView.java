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
package org.eclipse.recommenders.internal.extdoc.rcp.ui;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.recommenders.extdoc.rcp.providers.ExtdocProvider;
import org.eclipse.recommenders.internal.extdoc.rcp.preferences.PreferencesFacade;
import org.eclipse.recommenders.internal.extdoc.rcp.preferences.ProviderConfigurationPersistenceService;
import org.eclipse.recommenders.internal.extdoc.rcp.scheduling.ProviderExecutionScheduler;
import org.eclipse.recommenders.internal.extdoc.rcp.scheduling.SubscriptionManager;
import org.eclipse.recommenders.internal.extdoc.rcp.wiring.ExtdocModule.Extdoc;
import org.eclipse.recommenders.rcp.events.JavaSelectionEvent;
import org.eclipse.recommenders.utils.rcp.PartListener2Adapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.part.ViewPart;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class ExtdocView extends ViewPart {

    private final EventBus workspaceBus;
    private final EventBus extdocBus;
    private final SubscriptionManager subscriptionManager;
    private final List<ExtdocProvider> providers;
    private final PreferencesFacade preferences;
    private final ProviderOverviewPart overviewPart;
    private final ProviderContentPart contentPart;

    private ProviderExecutionScheduler scheduler;
    private final Lock schedulerLock = new ReentrantLock();
    private SashForm sashForm;
    private final Provider<IWorkbenchPage> activePage;
    protected boolean visible = true;

    @Inject
    public ExtdocView(final EventBus workspaceBus, @Extdoc final EventBus extdocBus,
            final SubscriptionManager subscriptionManager, final ExtdocIconLoader iconLoader,
            final List<ExtdocProvider> providers, final PreferencesFacade preferences,
            final ProviderOverviewPart overviewPart, final ProviderContentPart contentPart,
            final ProviderConfigurationPersistenceService ps, final Provider<IWorkbenchPage> activePage) {
        this.workspaceBus = workspaceBus;
        this.extdocBus = extdocBus;
        this.subscriptionManager = subscriptionManager;
        this.providers = providers;
        this.preferences = preferences;
        this.overviewPart = overviewPart;
        this.contentPart = contentPart;
        this.activePage = activePage;
    }

    @Override
    public void createPartControl(final Composite parent) {
        createUiElements(parent);
        subscribeToEventBusses();
        addPartListener();
    }

    private void createUiElements(final Composite parent) {
        sashForm = new SashForm(parent, SWT.SMOOTH);
        sashForm.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());

        overviewPart.createControl(sashForm);
        contentPart.createControl(sashForm);

        sashForm.setWeights(preferences.loadSashWeights());
        sashForm.addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(final DisposeEvent e) {
                preferences.storeSashWeights(sashForm.getWeights());
            }
        });
    }

    private void subscribeToEventBusses() {
        extdocBus.register(contentPart);
        extdocBus.register(overviewPart);
        workspaceBus.register(this);
    }

    private void addPartListener() {
        activePage.get().addPartListener(new PartListener2Adapter() {

            @Override
            public void partHidden(final IWorkbenchPartReference partRef) {
                if (isExtdocView(partRef)) {
                    visible = false;
                }
            }

            @Override
            public void partVisible(final IWorkbenchPartReference partRef) {
                if (isExtdocView(partRef)) {
                    visible = true;
                }
            }

            private boolean isExtdocView(final IWorkbenchPartReference partRef) {
                return partRef.getPart(false) == ExtdocView.this;
            }

        });
    }

    @Override
    public void dispose() {
        unsubscribeFromEventBusses();
        disposeScheduler();
        super.dispose();
    }

    private void unsubscribeFromEventBusses() {
        extdocBus.unregister(contentPart);
        extdocBus.unregister(overviewPart);
        workspaceBus.unregister(this);
    }

    @Subscribe
    @AllowConcurrentEvents
    public void onJavaSelection(final JavaSelectionEvent selection) {
        if (visible) {
            disposeScheduler();
            scheduleNewSelection(selection);
        }
    }

    // it might be necessary to add a lock here... a synchronized method is not
    // possible as this method would block the "syncScheduling" method
    private void disposeScheduler() {
        if (scheduler != null) {
            scheduler.dispose();
            scheduler = null;
        }
    }

    private void scheduleNewSelection(final JavaSelectionEvent selection) {
        try {
            schedulerLock.lock();
            scheduler = new ProviderExecutionScheduler(providers, subscriptionManager, contentPart, extdocBus);
            scheduler.scheduleOnSelection(selection);
        } finally {
            schedulerLock.unlock();
        }
    }

    @Override
    public void setFocus() {
        contentPart.setFocus();
    }
}