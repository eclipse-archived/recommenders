/**
 * Copyright (c) 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.server.codesearch;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.log4j.BasicConfigurator;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

    private static BundleContext context;

    static BundleContext getContext() {
        return context;
    }

    private File installLocation;

    @Override
    public void start(final BundleContext context) throws Exception {
        resolveInstallLocation(context);
        initLoggingSystem();

    }

    private void resolveInstallLocation(final BundleContext context) throws IOException {
        final Bundle bundle = context.getBundle();
        final URL indexDirUrl = bundle.getEntry("data");
        // final URLConverter converter = new URLConverterImpl();
        // final URL fileURL = converter.toFileURL(indexDirUrl);
        // installLocation = new File(fileURL.getPath()).getAbsoluteFile();
    }

    private void initLoggingSystem() {
        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();
    }

    @Override
    public void stop(final BundleContext context) throws Exception {

    }
}
