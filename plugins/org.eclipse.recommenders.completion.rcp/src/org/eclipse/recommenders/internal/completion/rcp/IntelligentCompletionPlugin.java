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
package org.eclipse.recommenders.internal.completion.rcp;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.recommenders.utils.rcp.LoggingUtils;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class IntelligentCompletionPlugin extends AbstractUIPlugin {

    public static final String PLUGIN_ID = "org.eclipse.recommenders.codecompletion"; //$NON-NLS-1$

    private static IntelligentCompletionPlugin plugin;

    public static IntelligentCompletionPlugin getDefault() {
        return plugin;
    }

    public static void log(final CoreException e) {
        LoggingUtils.log(e, getDefault());
    }

    public static void log(final IStatus status) {
        LoggingUtils.log(status, getDefault());
    }

    public static void logError(final Throwable e, final String format, final Object... args) {
        LoggingUtils.logError(e, getDefault(), format, args);
    }

    public static void logWarning(final Throwable e, final String format, final Object... args) {
        LoggingUtils.logError(e, getDefault(), format, args);
    }

    public static void logWarning(final String format, final Object... args) {
        LoggingUtils.logWarning(null, getDefault(), format, args);
    }

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
}
