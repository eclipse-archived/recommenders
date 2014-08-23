/**
 * Copyright (c) 2014 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.utils;

import static org.eclipse.core.runtime.IStatus.ERROR;
import static org.eclipse.recommenders.utils.Checks.*;
import static org.eclipse.recommenders.utils.Throws.throwUnreachable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;

public class Logs {

    public interface ILogMessage {

        /**
         * The severity of a log message. One of {@link IStatus#ERROR}, {@link IStatus#WARNING}, {@link IStatus#INFO},
         * {@link IStatus#OK}, {@link IStatus#CANCEL}
         */
        int severity();

        /**
         * The error or log code of this log message.
         */
        int code();

        /**
         * A message string that may contain {@link MessageFormat} compatible placeholders (e.g., {0}).
         */
        String message();

        /**
         * The bundle the log message is defined by.
         */
        Bundle bundle();
    }

    public abstract static class DefaultLogMessage implements ILogMessage {

        private int severity;
        private int code;
        private String message;

        public DefaultLogMessage(int severity, int code, String message) {
            ensureIsGreaterOrEqualTo(code, 1, "The error code cannot be '0'");
            ensureIsNotNull(message);
            this.severity = severity;
            this.code = code;
            this.message = message;
        }

        @Override
        public int severity() {
            return severity;
        }

        @Override
        public int code() {
            return code;
        }

        @Override
        public String message() {
            return message;
        }

        @Override
        public abstract Bundle bundle();

    }

    public static IStatus toStatus(ILogMessage msg, Throwable t, Object... args) {
        ensureIsNotNull(msg);
        String message = null;
        try {
            message = MessageFormat.format(msg.message(), args);
        } catch (Exception e) {
            // in case of an error, do a bullet proof error logging and continue working as if almost nothing happened:
            message = msg.message();
            Bundle bundle = FrameworkUtil.getBundle(Logs.class);
            if (bundle != null) {
                ILog log = Platform.getLog(bundle);
                if (log != null) {
                    String format = MessageFormat.format("Failed to format '{0}': {1}", msg.message(), e.getMessage());
                    Status error = new Status(ERROR, bundle.getSymbolicName(), format, e);
                    log.log(error);
                }
            }
        }
        return new Status(msg.severity(), msg.bundle().getSymbolicName(), msg.code(), message, t);
    }

    public static void log(ILogMessage msg) {
        log(msg, (Object[]) null);
    }

    public static void log(ILogMessage msg, Throwable t) {
        log(msg, t, (Object[]) null);
    }

    public static void log(ILogMessage msg, Object... args) {
        log(msg, LogTraceException.newTrace(), args);
    }

    public static void log(ILogMessage msg, Throwable t, Object... args) {
        try {
            IStatus status = toStatus(msg, t, args);
            Bundle bundle = msg.bundle();
            ILog log = Platform.getLog(bundle);
            log.log(status);
        } catch (Exception e) {
            // swallow this one...
            // we are likely in a test case that does not run inside an OSGI/Eclipse runtime.
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
            return "CANCEL";
        case IStatus.ERROR:
            return "ERROR";
        case IStatus.WARNING:
            return "WARNING";
        case IStatus.INFO:
            return "INFO";
        case IStatus.OK:
            return "OK";
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

    private static final class FakeBundle implements Bundle {

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

    private static final class LogTraceException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        static LogTraceException newTrace() {
            LogTraceException res = new LogTraceException();
            res.fillInStackTrace();
            return res;
        }
    }
}
