/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.rcp;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.recommenders.commons.injection.InjectionService;
import org.eclipse.recommenders.internal.rcp.RecommendersDashboard;
import org.eclipse.recommenders.rcp.utils.LoggingUtils;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class RecommendersPlugin extends AbstractUIPlugin {
    private static RecommendersPlugin plugin;

    public static RecommendersPlugin getDefault() {
        return plugin;
    }

    public static void log(final CoreException e) {
        LoggingUtils.log(e, getDefault());
    }

    public static void logError(final Exception e, final String format, final Object... args) {
        LoggingUtils.logError(e, getDefault(), format, args);
    }

    public static void logWarning(final Exception e, final String format, final Object... args) {
        LoggingUtils.logError(e, getDefault(), format, args);
    }

    public static void logWarning(final String format, final Object... args) {
        LoggingUtils.logWarning(null, getDefault(), format, args);
    }

    // private Injector injector;

    @Override
    public void start(final BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        // initInjector();
    }

    private void initInjector() {
        InjectionService.getInstance().getInjector().getInstance(RecommendersDashboard.class);
    }

    //
    // public Injector getInjector() {
    // return injector;
    // }

    // public <T> T requestInstance(final Class<T> type) {
    // final T res = injector.getInstance(type);
    // return res;
    // }

    // public void requestInjectMembers(final Object obj) {
    // injector.injectMembers(obj);
    // }

    @Override
    public void stop(final BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }
}
