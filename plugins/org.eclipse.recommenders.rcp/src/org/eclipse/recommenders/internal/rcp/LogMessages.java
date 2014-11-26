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
package org.eclipse.recommenders.internal.rcp;

import static org.eclipse.core.runtime.IStatus.ERROR;

import org.eclipse.recommenders.utils.Logs;
import org.eclipse.recommenders.utils.Logs.DefaultLogMessage;
import org.osgi.framework.Bundle;

public final class LogMessages extends DefaultLogMessage {

    private static int code = 1;

    private static final Bundle BUNDLE = Logs.getBundle(LogMessages.class);

    public static final LogMessages EXCEPTION_OCCURRED_IN_SERVICE_HOOK = new LogMessages(ERROR,
            Messages.LOG_ERROR_EXCEPTION_OCCURRED_IN_SERVICE_HOOK);
    public static final LogMessages ACTIVE_PAGE_FINDER_TOO_EARLY = new LogMessages(ERROR,
            Messages.LOG_ERROR_ACTIVE_PAGE_FINDER_TOO_EARLY);
    public static final LogMessages PREFERENCES_NOT_SAVED = new LogMessages(ERROR,
            Messages.LOG_ERROR_PREFERENCES_NOT_SAVED);
    public static final LogMessages RESTART_ECLIPSE_NOT_POSSIBLE = new LogMessages(ERROR,
            Messages.DIALOG_RESTART_NOT_POSSIBLE);
    public static final LogMessages FAILED_TO_RESOLVE_SELECTION = new LogMessages(ERROR,
            Messages.LOG_ERROR_FAILED_TO_RESOLVE_SELECTION);
    public static final LogMessages EXCEPTION_WHILE_CHECKING_OFFSETS = new LogMessages(ERROR,
            Messages.LOG_ERROR_EXCEPTION_WHILE_CHECKING_OFFSETS);
    public static final LogMessages ARRAY_TYPE_IN_JAVA_ELEMENT_RESOLVER = new LogMessages(ERROR,
            Messages.LOG_ERROR_ARRAY_TYPE_IN_JAVA_ELEMENT_RESOLVER);
    public static final LogMessages FAILED_TO_RESOLVE_METHOD = new LogMessages(ERROR,
            Messages.LOG_ERROR_FAILED_TO_RESOLVE_METHOD);
    public static final LogMessages FAILED_TO_GENERATE_UUID = new LogMessages(ERROR,
            Messages.LOG_ERROR_FAILED_TO_GENERATE_UUID);
    public static final LogMessages FAILED_TO_RESOLVE_TYPE_PARAMETER = new LogMessages(ERROR,
            Messages.LOG_ERROR_FAILED_TO_RESOLVE_TYPE_PARAMETER);
    public static final LogMessages AN_ERROR_OCCURRED = new LogMessages(ERROR, Messages.LOG_ERROR_AN_ERROR_OCCURRED);

    private LogMessages(int severity, String message) {
        super(severity, code++, message);
    }

    @Override
    public Bundle bundle() {
        return BUNDLE;
    }
}
