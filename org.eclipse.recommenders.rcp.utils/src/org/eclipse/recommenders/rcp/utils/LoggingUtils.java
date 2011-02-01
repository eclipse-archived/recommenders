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
package org.eclipse.recommenders.rcp.utils;

import static java.lang.String.format;
import static org.eclipse.recommenders.commons.utils.Checks.ensureIsNotNull;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.Bundle;

public class LoggingUtils {
    private static IStatus newStatus(final int kind, final Throwable exception, final String pluginId,
            final String messageFormat, final Object... messageArgs) {
        final String message = messageFormat == null ? "" : format(messageFormat, messageArgs);
        final IStatus res = new Status(kind, pluginId, message, exception);
        return res;
    }

    public static void logError(final Throwable exception, final Plugin plugin, final String format,
            final Object... args) {
        final IStatus error = newStatus(IStatus.ERROR, exception, getSymbolicName(plugin), format, args);
        log(error, plugin);
    }

    public static void logWarning(final Throwable exception, final Plugin plugin, final String format,
            final Object... args) {
        final IStatus status = newStatus(IStatus.WARNING, exception, getSymbolicName(plugin), format, args);
        log(status, plugin);
    }

    private static String getSymbolicName(final Plugin plugin) {
        ensureIsNotNull(plugin, "logging requires a plug-in to be specified");
        final Bundle bundle = plugin.getBundle();
        return bundle.getSymbolicName();
    }

    public static void log(final IStatus status, final Plugin plugin) {
        final ILog log = plugin.getLog();
        log.log(status);
    }

    public static void log(final CoreException exception, final Plugin plugin) {
        final IStatus status = exception.getStatus();
        log(status, plugin);
    }
}
