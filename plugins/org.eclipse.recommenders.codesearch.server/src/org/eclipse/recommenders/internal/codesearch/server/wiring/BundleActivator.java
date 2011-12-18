/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 */
package org.eclipse.recommenders.internal.codesearch.server.wiring;

import org.osgi.framework.BundleContext;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTracker;

public class BundleActivator implements org.osgi.framework.BundleActivator {

    private ServiceTracker tracker;
    WebserviceActivator serviceActivator;

    @Override
    public void start(final BundleContext context) throws Exception {
        tracker = new CodesearchServiceStartStopHelper(context, HttpService.class.getName(), null);
        tracker.open();
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
        tracker.close();
    }

}
