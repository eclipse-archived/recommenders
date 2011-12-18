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

import static org.eclipse.recommenders.utils.Checks.ensureIsNotNull;

import java.util.Properties;

import org.eclipse.equinox.http.jetty.JettyConfigurator;
import org.eclipse.equinox.http.jetty.JettyConstants;
import org.eclipse.recommenders.codesearch.server.resources.CodeSearchResource;
import org.eclipse.recommenders.internal.rcp.logging.LoggingActivator;
import org.eclipse.recommenders.server.ServerConfiguration;
import org.eclipse.recommenders.server.udc.resources.UploadResource;
import org.eclipse.ui.IStartup;
import org.osgi.framework.FrameworkUtil;

public class Startup implements IStartup {

    @Override
    public void earlyStartup() {
        try {
            startLoggingPlugin();
            startHttpService();
            startServer();
        } catch (final Exception e) {
            System.out.println(e);
        }
    }

    private void startServer() {
        ensureIsNotNull(FrameworkUtil.getBundle(CodeSearchResource.class));
        ensureIsNotNull(FrameworkUtil.getBundle(UploadResource.class));
    }

    private void startHttpService() throws Exception {

        final Properties settings = new Properties();
        settings.put(JettyConstants.HTTP_PORT, ServerConfiguration.DEFAULT_HTTP_PORT);
        JettyConfigurator.startServer(Activator.JETTY_ID, settings);
    }

    private void startLoggingPlugin() {
        ensureIsNotNull(FrameworkUtil.getBundle(LoggingActivator.class));
    }

}
