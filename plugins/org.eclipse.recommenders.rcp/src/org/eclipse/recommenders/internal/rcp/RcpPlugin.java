/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp;

import java.util.concurrent.TimeUnit;

import org.eclipse.recommenders.internal.rcp.news.CheckForProjectNewsJob;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class RcpPlugin extends AbstractUIPlugin {

    private static RcpPlugin plugin;

    public static RcpPlugin getDefault() {
        return plugin;
    }

    @Override
    public void start(final BundleContext context) throws Exception {
        plugin = this;
        super.start(context);
        new CheckForProjectNewsJob().schedule(TimeUnit.HOURS.toMillis(2));
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
        super.stop(context);
        plugin = null;
    }
}
