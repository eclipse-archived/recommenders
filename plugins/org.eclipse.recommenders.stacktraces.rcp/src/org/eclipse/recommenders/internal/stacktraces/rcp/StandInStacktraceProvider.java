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
package org.eclipse.recommenders.internal.stacktraces.rcp;

import static org.eclipse.recommenders.utils.Logs.log;

import java.lang.reflect.Method;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.recommenders.utils.Reflections;

import com.google.common.collect.Sets;

public class StandInStacktraceProvider {

    public static class StandInException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public StandInException(String message) {
            super(message);
        }
    }

    private static final String STAND_IN_MESSAGE = "Stand-In Stacktrace supplied by Eclipse Stacktraces & Error Reporting Tool";

    private static final Set<String> BLACKLISTED_CLASSNAMES = Sets.newHashSet();

    static {
        BLACKLISTED_CLASSNAMES.add("java.security.AccessController");
        BLACKLISTED_CLASSNAMES.add("org.eclipse.core.internal.runtime.Log");
        BLACKLISTED_CLASSNAMES.add("org.eclipse.core.internal.runtime.RuntimeLog");
        BLACKLISTED_CLASSNAMES.add("org.eclipse.core.internal.runtime.PlatformLogWriter");
        BLACKLISTED_CLASSNAMES.add("org.eclipse.osgi.internal.log.ExtendedLogReaderServiceFactory");
        BLACKLISTED_CLASSNAMES.add("org.eclipse.osgi.internal.log.ExtendedLogReaderServiceFactory$3");
        BLACKLISTED_CLASSNAMES.add("org.eclipse.osgi.internal.log.ExtendedLogServiceFactory");
        BLACKLISTED_CLASSNAMES.add("org.eclipse.osgi.internal.log.ExtendedLogServiceImpl");
        BLACKLISTED_CLASSNAMES.add("org.eclipse.osgi.internal.log.LoggerImpl");
        BLACKLISTED_CLASSNAMES.add("org.eclipse.recommenders.internal.stacktraces.rcp.DebugStacktraceProvider");
        BLACKLISTED_CLASSNAMES.add("org.eclipse.recommenders.internal.stacktraces.rcp.LogListener");
    }

    private static Method SET_EXCEPTION = Reflections.getDeclaredMethod(Status.class, "setException", Throwable.class)
            .orNull();

    protected void insertStandInStacktraceIfEmpty(final IStatus status) {
        if (requiresStandInStacktrace(status)) {
            Throwable syntetic = new StandInException(STAND_IN_MESSAGE);
            syntetic.fillInStackTrace();
            StackTraceElement[] stacktrace = syntetic.getStackTrace();
            StackTraceElement[] clearedStacktrace = clearBlacklistedTopStackframes(stacktrace, BLACKLISTED_CLASSNAMES);
            syntetic.setStackTrace(clearedStacktrace);
            try {
                SET_EXCEPTION.invoke(status, syntetic);
            } catch (Exception e) {
                log(LogMessages.LOG_WARNING_REFLECTION_FAILED, e, SET_EXCEPTION);
            }
        }
    }

    private boolean requiresStandInStacktrace(final IStatus status) {
        if (status.getException() != null) {
            return false;
        }
        if (!(status instanceof Status)) {
            return false;
            // TODO log unknown status and send classname
        }
        return true;
    }

    protected StackTraceElement[] clearBlacklistedTopStackframes(StackTraceElement[] stackframes,
            Set<String> blacklistedClassNames) {
        if (ArrayUtils.isEmpty(stackframes)) {
            return stackframes;
        }
        int index = findCutOffIndex(stackframes, blacklistedClassNames);
        return ArrayUtils.subarray(stackframes, index, stackframes.length);
    }

    private int findCutOffIndex(StackTraceElement[] stackframes, Set<String> blacklistedClassNames) {
        for (int i = 0; i < stackframes.length; i++) {
            StackTraceElement current = stackframes[i];
            if (!blacklistedClassNames.contains(current.getClassName())) {
                return i;
            }
        }
        return 0;
    }
}
