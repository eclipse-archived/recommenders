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

import static java.text.MessageFormat.format;
import static org.eclipse.recommenders.utils.Checks.ensureIsNotNull;
import static org.eclipse.recommenders.utils.Throws.throwUnreachable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.recommenders.internal.rcp.Messages;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;

public class Logs {

    public static IStatus newStatus(final int kind, final Throwable exception, final String pluginId,
            final String messageFormat, final Object... messageArgs) {
        final String message = messageFormat == null ? "" : format(messageFormat, messageArgs); //$NON-NLS-1$
        return new Status(kind, pluginId, message, exception);
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
        ensureIsNotNull(plugin, "Logging requires a plug-in to be specified"); //$NON-NLS-1$
        final Bundle bundle = plugin.getBundle();
        return bundle.getSymbolicName();
    }

    public static void log(final IStatus status, final Plugin plugin) {
        final ILog log = plugin.getLog();
        try {
            // this fails sometimes with an NPE in
            // org.eclipse.core.internal.runtime.Log.isLoggable(Log.java:101)
            log.log(status);
        } catch (final Exception e) {
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

    public static String toString(final IStatus status) {
        final StringBuilder sb = new StringBuilder();
        appendSeverityAndMessage(status, sb);
        appendException(status, sb);
        if (status.isMultiStatus()) {
            appendChildren(status, sb);
        }
        return sb.toString();
    }

    private static void appendSeverityAndMessage(final IStatus status, final StringBuilder sb) {
        sb.append(toSeverity(status)).append(':').append(' ').append(status.getMessage());
    }

    private static String toSeverity(final IStatus status) {
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

    private static void appendException(final IStatus status, final StringBuilder sb) {
        if (status.getException() != null) {
            sb.append(' ').append(status.getException());
        }
    }

    private static void appendChildren(final IStatus status, final StringBuilder sb) {
        for (final IStatus child : status.getChildren()) {
            sb.append('\n').append(toString(child));
        }
    }

    public static Bundle getBundle(Class<?> bundleClazz) {
        Bundle res = FrameworkUtil.getBundle(bundleClazz);
        if (res != null) {
            return res;
        }
        String fakeSymbolicName = bundleClazz.getPackage().getName();
        return new FakeBundle(fakeSymbolicName);
    }

    public static ILog getLog(Bundle bundle) {
        if (bundle == null) {
            return new SystemOutLog();
        }
        return Platform.getLog(bundle);
    }

    private static final class SystemOutLog implements ILog {
        @Override
        public void removeLogListener(ILogListener listener) {
        }

        @Override
        public void log(IStatus status) {
            System.out.println(status);
        }

        @Override
        public Bundle getBundle() {
            return null;
        }

        @Override
        public void addLogListener(ILogListener listener) {
        }
    }

    static final class FakeBundle implements Bundle {

        private String symbolicName;

        public FakeBundle(String fakeBundleId) {
            this.symbolicName = fakeBundleId;

        }

        @Override
        public int compareTo(Bundle arg0) {
            return 0;
        }

        @Override
        public void update(InputStream input) throws BundleException {
        }

        @Override
        public void update() throws BundleException {
        }

        @Override
        public void uninstall() throws BundleException {
        }

        @Override
        public void stop(int options) throws BundleException {
        }

        @Override
        public void stop() throws BundleException {
        }

        @Override
        public void start(int options) throws BundleException {
        }

        @Override
        public void start() throws BundleException {
        }

        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            return null;
        }

        @Override
        public boolean hasPermission(Object permission) {
            return false;
        }

        @Override
        public Version getVersion() {
            return null;
        }

        @Override
        public String getSymbolicName() {
            return symbolicName;
        }

        @Override
        public int getState() {
            return 0;
        }

        @Override
        public Map<X509Certificate, List<X509Certificate>> getSignerCertificates(int signersType) {
            return null;
        }

        @Override
        public ServiceReference<?>[] getServicesInUse() {
            return null;
        }

        @Override
        public Enumeration<URL> getResources(String name) throws IOException {
            return null;
        }

        @Override
        public URL getResource(String name) {
            return null;
        }

        @Override
        public ServiceReference<?>[] getRegisteredServices() {
            return null;
        }

        @Override
        public String getLocation() {
            return null;
        }

        @Override
        public long getLastModified() {
            return 0;
        }

        @Override
        public Dictionary<String, String> getHeaders(String locale) {
            return null;
        }

        @Override
        public Dictionary<String, String> getHeaders() {
            return null;
        }

        @Override
        public Enumeration<String> getEntryPaths(String path) {
            return null;
        }

        @Override
        public URL getEntry(String path) {
            return null;
        }

        @Override
        public File getDataFile(String filename) {
            return null;
        }

        @Override
        public long getBundleId() {
            return 0;
        }

        @Override
        public BundleContext getBundleContext() {
            return null;
        }

        @Override
        public Enumeration<URL> findEntries(String path, String filePattern, boolean recurse) {
            return null;
        }

        @Override
        public <A> A adapt(Class<A> type) {
            return null;
        }
    }
}
