/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marcel Bruch - Initial API and implementation
 */
package org.eclipse.recommenders.internal.codesearch.server.wiring;

import org.eclipse.recommenders.injection.InjectionService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;

public final class CodesearchServiceStartStopHelper extends ServiceTracker {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private HttpService httpService;
    private WebserviceActivator serviceActivator;

    public CodesearchServiceStartStopHelper(final BundleContext context, final String clazz,
            final ServiceTrackerCustomizer customizer) {
        super(context, clazz, customizer);
    }

    @Override
    public Object addingService(final ServiceReference serviceRef) {
        httpService = (HttpService) super.addingService(serviceRef);
        final Injector parent = InjectionService.getInstance().getInjector();
        final Injector child = parent.createChildInjector(new GuiceModule(httpService));
        serviceActivator = child.getInstance(WebserviceActivator.class);
        try {
            serviceActivator.start();
            log.info("Started Code Recommenders code-search server.");
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
        return httpService;
    }

    @Override
    public void removedService(final ServiceReference ref, final Object service) {
        if (serviceActivator != null) {
            serviceActivator.stop();
            serviceActivator = null;
            httpService = null;
            log.info("Stopped Code Recommenders code-search server.");
        }
        super.removedService(ref, service);
    }
}