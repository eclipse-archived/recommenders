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
package org.eclipse.recommenders.internal.snipmatch.rcp;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.recommenders.rcp.utils.Logs;
import org.osgi.framework.Bundle;

public enum LogMessages {

    SNIPPET_REPLACE_LEADING_WHITESPACE_FAILED(IStatus.ERROR, 100,
            "An error occured while determining the leading whitespace characters.");

    static Bundle bundle = Logs.getBundle(LogMessages.class);
    static ILog log = Logs.getLog(bundle);

    private int severity;
    private int code;
    private String message;

    private LogMessages(int severity, int code, String message) {
        this.severity = severity;
        this.code = code;
        this.message = message;
    }

    public IStatus toStatus(Throwable t, Object... args) {
        return new Status(severity, bundle.getSymbolicName(), code, String.format(message, args), t);
    }

    public static void log(LogMessages msg) {
        LogMessages.log(msg, null, (Object[]) null);
    }

    public static void log(LogMessages msg, Object... args) {
        LogMessages.log(msg, null, args);
    }

    public static void log(LogMessages msg, Throwable t, Object... args) {
        log.log(msg.toStatus(t, args));
    }

}
