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
package org.eclipse.recommenders.extdoc.rcp.ui;

import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.recommenders.extdoc.rcp.Provider;
import org.eclipse.recommenders.extdoc.rcp.ExtdocModule.Extdoc;
import org.eclipse.recommenders.extdoc.rcp.scheduling.ProviderExecutionScheduler;
import org.eclipse.recommenders.extdoc.rcp.scheduling.SubscriptionManager;
import org.eclipse.recommenders.rcp.events.JavaSelectionEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

public class ExtdocView extends ViewPart {

    private final EventBus workspaceBus;
    private final EventBus extdocBus;
    private final SubscriptionManager subscriptionManager;
    private final ExtdocIconLoader iconLoader;
    private final List<Provider> providers;

    private ProviderOverviewPart overviewPart;
    private ProviderContentPart contentPart;

    private ProviderExecutionScheduler scheduler;
    private SashForm sashForm;

    @Inject
    public ExtdocView(EventBus workspaceBus, @Extdoc EventBus extdocBus, SubscriptionManager subscriptionManager,
            ExtdocIconLoader iconLoader, List<Provider> providers) {
        this.workspaceBus = workspaceBus;
        this.extdocBus = extdocBus;
        this.subscriptionManager = subscriptionManager;
        this.iconLoader = iconLoader;
        this.providers = providers;
    }

    @Override
    public void createPartControl(Composite parent) {
        createSashForm(parent);

        overviewPart = new ProviderOverviewPart(extdocBus, providers, iconLoader);
        overviewPart.createControl(sashForm);
        contentPart = new ProviderContentPart(providers);
        contentPart.createControl(sashForm);

        subscribeToEventBusses();
    }

    private void createSashForm(Composite parent) {
        sashForm = new SashForm(parent, SWT.SMOOTH);
        sashForm.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
    }

    @Override
    public void init(IViewSite site, IMemento memento) throws PartInitException {
        super.init(site, memento);

        // TODO load settings
        // sashForm.setWeights(preferences.getSashWeights());
    }

//    @Override
//    public void saveState(IMemento memento) {
//        preferences.setSashWeights(sashForm.getWeights());
//    }

    @Override
    public void dispose() {
        unsubscribeFromEventBusses();
        disposeScheduler();
        super.dispose();
    }

    @Override
    public void setFocus() {
        // TODO: check if this is needed
    }

    @Subscribe
    public void handle(JavaSelectionEvent selection) {
        disposeScheduler();
        scheduler = new ProviderExecutionScheduler(providers, subscriptionManager, contentPart, extdocBus);
        scheduler.scheduleOnSelection(selection);
    }

    private void disposeScheduler() {
        if (scheduler != null) {
            scheduler.dispose();
        }
    }

    private void subscribeToEventBusses() {
        extdocBus.register(this);
        extdocBus.register(contentPart);
        extdocBus.register(overviewPart);
        workspaceBus.register(this);
    }

    private void unsubscribeFromEventBusses() {
        extdocBus.unregister(this);
        extdocBus.unregister(contentPart);
        extdocBus.unregister(overviewPart);
        workspaceBus.unregister(this);
    }
}