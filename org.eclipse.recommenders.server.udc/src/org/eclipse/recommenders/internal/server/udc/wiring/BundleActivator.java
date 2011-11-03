/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 *    Andreas Frankenberger - initial API and implementation.
 */
package org.eclipse.recommenders.internal.server.udc.wiring;

import org.eclipse.recommenders.commons.injection.InjectionService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;

public class BundleActivator implements org.osgi.framework.BundleActivator {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private ServiceTracker tracker;
	private static BundleContext context;
	private WebserviceActivator serviceActivator;

	static BundleContext getContext() {
		return context;
	}

	@Override
	public void start(final BundleContext bundleContext) throws Exception {
		BundleActivator.context = bundleContext;
		tracker = new ServiceTracker(context, HttpService.class.getName(), null) {

			private HttpService associatedWithHttpService;

			@Override
			public Object addingService(final ServiceReference serviceRef) {
				associatedWithHttpService = (HttpService) super.addingService(serviceRef);

				final Injector child = InjectionService.getInstance().getInjector()
						.createChildInjector(new GuiceModule(associatedWithHttpService));
				serviceActivator = child.getInstance(WebserviceActivator.class);
				try {
					serviceActivator.start();
				} catch (final Exception e) {
					throw new RuntimeException(e);
				}
				log.info("Started Code Recommenders Call Models Service");
				return associatedWithHttpService;

			}

			@Override
			public void removedService(final ServiceReference ref, final Object service) {
				if (associatedWithHttpService == service) {
					stopService();
					associatedWithHttpService = null;
				}
				// XXX is super required?
				super.removedService(ref, service);
				log.info("Stopped Code Recommenders Call Models Service");
			}
		};
		tracker.open();
	}

	@Override
	public void stop(final BundleContext bundleContext) throws Exception {
		BundleActivator.context = null;
		tracker.close();
		stopService();
	}

	private void stopService() {
		if (serviceActivator != null) {
			serviceActivator.stop();
		}
	}

}
