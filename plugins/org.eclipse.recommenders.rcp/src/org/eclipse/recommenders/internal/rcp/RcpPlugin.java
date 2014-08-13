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

import static org.eclipse.recommenders.utils.Throws.throwUnreachable;

import java.text.MessageFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.recommenders.utils.Checks;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public class RcpPlugin extends AbstractUIPlugin {

    private static RcpPlugin plugin;

    public static RcpPlugin getDefault() {
        return plugin;
    }

    public static void log(final CoreException e) {
        _log(e, getDefault());
    }

    public static void logError(final Exception e, final String format, final Object... args) {
        _logError(e, getDefault(), format, args);
    }

    public static void logWarning(final Exception e, final String format, final Object... args) {
        _logError(e, getDefault(), format, args);
    }

    public static void logWarning(final String format, final Object... args) {
        logWarning(null, getDefault(), format, args);
    }

    public static void log(final IStatus res) {
        log(res, getDefault());
    }

    public static IStatus _newStatus(final int kind, final Throwable exception, final String pluginId,
            final String messageFormat, final Object... messageArgs) {
        final String message = messageFormat == null ? "" : MessageFormat.format(messageFormat, messageArgs); //$NON-NLS-1$
        return new Status(kind, pluginId, message, exception);
    }

    private static void _logError(final Throwable exception, final Plugin plugin, final String format,
            final Object... args) {
        final IStatus error = _newStatus(IStatus.ERROR, exception, getSymbolicName(plugin), format, args);
        log(error, plugin);
    }

    private static String getSymbolicName(final Plugin plugin) {
        Checks.ensureIsNotNull(plugin, "Logging requires a plug-in to be specified"); //$NON-NLS-1$
        final Bundle bundle = plugin.getBundle();
        return bundle.getSymbolicName();
    }

    private static void log(final IStatus status, final Plugin plugin) {
        final ILog log = plugin.getLog();
        try {
            // this fails sometimes with an NPE in
            // org.eclipse.core.internal.runtime.Log.isLoggable(Log.java:101)
            log.log(status);
        } catch (final Exception e) {
            System.out.println(status);
        }
    }

    private static void _log(final CoreException exception, final Plugin plugin) {
        final IStatus status = exception.getStatus();
        log(status, plugin);
    }

    private static String _toString(final IStatus status) {
        final StringBuilder sb = new StringBuilder();
        _appendSeverityAndMessage(status, sb);
        _appendException(status, sb);
        if (status.isMultiStatus()) {
            _appendChildren(status, sb);
        }
        return sb.toString();
    }

    private static void _appendSeverityAndMessage(final IStatus status, final StringBuilder sb) {
        sb.append(_toSeverity(status)).append(':').append(' ').append(status.getMessage());
    }

    private static String _toSeverity(final IStatus status) {
        switch (status.getSeverity()) {
        case IStatus.CANCEL:
            return Messages.LOG_CANCEL;
        case IStatus.ERROR:
            return Messages.LOG_ERROR;
        case IStatus.WARNING:
            return Messages.LOG_WARNING;
        case IStatus.INFO:
            return Messages.LOG_INFO;
        case IStatus.OK:
            return Messages.LOG_OK;
        default:
            throw throwUnreachable();
        }
    }

    private static void _appendException(final IStatus status, final StringBuilder sb) {
        if (status.getException() != null) {
            sb.append(' ').append(status.getException());
        }
    }

    private static void _appendChildren(final IStatus status, final StringBuilder sb) {
        for (final IStatus child : status.getChildren()) {
            sb.append('\n').append(_toString(child));
        }
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
