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
package org.eclipse.recommenders.internal.rcp.wala;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.recommenders.rcp.utils.LoggingUtils;
import org.osgi.framework.BundleContext;

public class RcpAnalysisPlugin extends Plugin {

    private static RcpAnalysisPlugin plugin;

    public static RcpAnalysisPlugin getDefault() {
        return plugin;
    }

    public static void log(CoreException e) {
        LoggingUtils.log(e, getDefault());
    }

    public static void logError(Exception e, String format, Object... args) {
        LoggingUtils.logError(e, getDefault(), format, args);
    }

    public static void logWarning(Exception e, String format, Object... args) {
        LoggingUtils.logError(e, getDefault(), format, args);
    }

    public static void logWarning(String format, Object... args) {
        LoggingUtils.logWarning(null, getDefault(), format, args);
    }

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

}
