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

import static org.eclipse.core.runtime.IStatus.*;

import org.eclipse.recommenders.utils.Logs.DefaultLogMessage;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

public final class LogMessages extends DefaultLogMessage {

    private static int code = 1;

    private static final Bundle BUNDLE = FrameworkUtil.getBundle(LogMessages.class);
    private static final String VERSION = "Version: " + BUNDLE.getVersion().toString(); //$NON-NLS-1$

    public static final LogMessages NO_INTERNET = new LogMessages(WARNING,
            "Could not connect to server. Your IP is ''{0}''"); //$NON-NLS-1$
    public static final LogMessages FAILED_TO_PARSE_SEND_MODE = new LogMessages(WARNING,
            "Failed to parse send mode ''{0}''. Returning ''{1}'' instead."); //$NON-NLS-1$

    public static final LogMessages FAILED_TO_PARSE_REMEMBER_SEND_MODE = new LogMessages(WARNING,
            "Failed to parse remember send mode ''{0}''. Returning ''{1}'' instead."); //$NON-NLS-1$

    public static final LogMessages LOG_WARNING_REFLECTION_FAILED = new LogMessages(WARNING,
            "Could not access \u2018{0}\u2019 using reflection.  Functionality may be limited."); //$NON-NLS-1$
    public static final LogMessages PAUSE_PERIOD_ELAPSED = new LogMessages(INFO,
            "The paused interval for error reporting is elapsed, returning to 'ASK'-Mode"); //$NON-NLS-1$
    public static final LogMessages SAVE_PREFERENCES_FAILED = new LogMessages(ERROR, "Saving preferences failed"); //$NON-NLS-1$

    public static final LogMessages REPORTING_ERROR = new LogMessages(
            WARNING,
            "Unexpected Error occured while handling an error report. Please open a bug at https://bugs.eclipse.org/bugs/enter_bug.cgi?product=Recommenders.Incubator&component=Stacktraces");

    public static final LogMessages FIRST_CONFIGURATION_FAILED = new LogMessages(WARNING,
            "First configuration failed, please check the log"); //$NON-NLS-1$
    public static final LogMessages THANK_YOU_DIALOG_ERROR = new LogMessages(WARNING, "Error in thank you dialog"); //$NON-NLS-1$

    public static final LogMessages HISTORY_NOT_AVAILABLE = new LogMessages(WARNING, "History service is not available"); //$NON-NLS-1$
    public static final LogMessages HISTORY_NO_FINGERPRINT = new LogMessages(WARNING,
            "Cannot index error report without fingerprint"); //$NON-NLS-1$
    public static final LogMessages HISTORY_STOP_FAILED = new LogMessages(WARNING,
            "Stoping the history service failed."); //$NON-NLS-1$
    public static final LogMessages HISTORY_START_FAILED = new LogMessages(WARNING,
            "Starting the history service failed."); //$NON-NLS-1$

    public LogMessages(int severity, String message) {
        super(severity, code++, String.format("%s %s", message, VERSION));
    }

    @Override
    public Bundle bundle() {
        return BUNDLE;
    }
}
