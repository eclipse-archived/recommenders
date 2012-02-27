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
package org.eclipse.recommenders.utils.rcp;

import static java.lang.String.format;
import static org.eclipse.recommenders.utils.Checks.ensureIsNotNull;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.Bundle;

public class LoggingUtils {
    public static IStatus newStatus(final int kind, final Throwable exception, final String pluginId,
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
        try {
            // this fails sometimes with an NPE in
            // org.eclipse.core.internal.runtime.Log.isLoggable(Log.java:101)
            log.log(status);
        } catch (final Throwable e) {
            System.out.println(status);
        }
    }

    public static void log(final CoreException exception, final Plugin plugin) {
        final IStatus status = exception.getStatus();
        log(status, plugin);
    }

    public static IStatus newInfo(final Throwable exception, final String pluginId, final String messageFormat,
            final Object... methodArgs) {
        return newStatus(IStatus.INFO, exception, pluginId, messageFormat, methodArgs);
    }

    public static IStatus newInfo(final String pluginId, final String messageFormat, final Object... methodArgs) {
        return newStatus(IStatus.INFO, null, pluginId, messageFormat, methodArgs);
    }

    public static IStatus newError(final Throwable exception, final String pluginId, final String messageFormat,
            final Object... methodArgs) {
        return newStatus(IStatus.ERROR, exception, pluginId, messageFormat, methodArgs);
    }

    public static IStatus newWarning(final Throwable exception, final String pluginId, final String messageFormat,
            final Object... methodArgs) {
        return newStatus(IStatus.WARNING, exception, pluginId, messageFormat, methodArgs);
    }
}
