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
package org.eclipse.recommenders.rcp.selfhosting;

import java.util.Properties;

import org.eclipse.equinox.http.jetty.JettyConfigurator;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

    public static final String JETTY_ID = "code-recommenders";
    public static final String BUNDLE_ID = "org.eclipse.recommenders.rcp.selfhosting";

    @Override
    public void start(final BundleContext context) throws Exception {
        context.registerService(CommandProvider.class.getName(), new ConsoleCommands(), new Properties());
    }

    @Override
    public void stop(final BundleContext bundleContext) throws Exception {
        JettyConfigurator.stopServer(JETTY_ID);
    }

}
