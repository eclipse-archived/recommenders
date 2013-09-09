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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.recommenders.rcp.utils.Logs;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class RcpPlugin extends AbstractUIPlugin {

    static final String DIALOG_TITLE = "Help us improve Code Recommenders";

    private static RcpPlugin plugin;
    public static String P_UUID = "recommenders.uuid"; //$NON-NLS-1$

    public static RcpPlugin getDefault() {
        return plugin;
    }

    public static void log(final CoreException e) {
        Logs.log(e, getDefault());
    }

    public static void logError(final Exception e, final String format, final Object... args) {
        Logs.logError(e, getDefault(), format, args);
    }

    public static void logWarning(final Exception e, final String format, final Object... args) {
        Logs.logError(e, getDefault(), format, args);
    }

    public static void logWarning(final String format, final Object... args) {
        Logs.logWarning(null, getDefault(), format, args);
    }

    public static void log(final IStatus res) {
        Logs.log(res, getDefault());
    }

    @Override
    public void start(final BundleContext context) throws Exception {
        plugin = this;
        super.start(context);
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
        super.stop(context);
        plugin = null;
    }
}
