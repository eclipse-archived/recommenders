/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 */
package org.eclipse.recommenders.internal.rcp.logging;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

public class LoggingActivator extends Plugin {

    public static final String BUNDLE_ID = "org.eclipse.recommenders.rcp.logging";

    @Override
    public void start(final BundleContext bundleContext) throws Exception {
        final IStatus res = new LogbackConfigurationInitializer().call();
        getLog().log(res);
    }

    @Override
    public void stop(final BundleContext bundleContext) throws Exception {
    }

}
