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
package org.eclipse.recommenders.utils.rcp.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.recommenders.utils.rcp.LoggingUtils;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class RecommendersUtilsPlugin extends AbstractUIPlugin {

    private static RecommendersUtilsPlugin plugin;

    @Override
    public void start(final BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    public static RecommendersUtilsPlugin getDefault() {
        return plugin;
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

    public static void log(final CoreException e) {
        LoggingUtils.log(e, getDefault());
    }

    public static void log(final IStatus s) {
        LoggingUtils.log(s, getDefault());
    }
}
