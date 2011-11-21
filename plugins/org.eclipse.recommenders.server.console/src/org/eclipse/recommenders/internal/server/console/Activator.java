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
package org.eclipse.recommenders.internal.server.console;

import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {
    public static String BUNDLE_ID = "org.eclipse.recommenders.server.console";

    @Override
    public void start(final BundleContext context) throws Exception {
        final String serviceName = CommandProvider.class.getName();
        final RecommendersCommandProvider commandProvider = new RecommendersCommandProvider();
        final Dictionary<String, String> properties = new Hashtable<String, String>();
        context.registerService(serviceName, commandProvider, properties);
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
    }

}
