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
package org.eclipse.recommenders.internal.rcp.l10n;

import static org.eclipse.core.runtime.IStatus.*;

import org.eclipse.recommenders.utils.Logs;
import org.eclipse.recommenders.utils.Logs.DefaultLogMessage;
import org.eclipse.recommenders.utils.Logs.ILogMessage;
import org.osgi.framework.Bundle;

public final class LogMessages extends DefaultLogMessage {

    private static int code = 1;

    private static final Bundle BUNDLE = Logs.getBundle(LogMessages.class);

    public static final LogMessages ERROR_ACTIVE_PAGE_FINDER_TOO_EARLY = new LogMessages(ERROR,
            Messages.LOG_ERROR_ACTIVE_PAGE_FINDER_TOO_EARLY);
    public static final LogMessages ERROR_ARRAY_TYPE_IN_JAVA_ELEMENT_RESOLVER = new LogMessages(ERROR,
            Messages.LOG_ERROR_ARRAY_TYPE_IN_JAVA_ELEMENT_RESOLVER);
    public static final LogMessages ERROR_DIALOG_RESTART_NOT_POSSIBLE = new LogMessages(ERROR,
            Messages.LOG_ERROR_DIALOG_RESTART_NOT_POSSIBLE);
    public static final LogMessages ERROR_EXCEPTION_OCCURRED_IN_SERVICE_HOOK = new LogMessages(ERROR,
            Messages.LOG_ERROR_EXCEPTION_OCCURRED_IN_SERVICE_HOOK);
    public static final LogMessages ERROR_EXCEPTION_WHILE_CHECKING_OFFSETS = new LogMessages(ERROR,
            Messages.LOG_ERROR_EXCEPTION_WHILE_CHECKING_OFFSETS);
    public static final LogMessages ERROR_FAILED_TO_EXECUTE_COMMAND = new LogMessages(ERROR,
            Messages.LOG_ERROR_FAILED_TO_EXECUTE_COMMAND);
    public static final LogMessages ERROR_FAILED_TO_FIND_FIELDS_FOR_TYPE = new LogMessages(ERROR,
            Messages.LOG_ERROR_FAILED_TO_FIND_FIELDS_FOR_TYPE);
    public static final LogMessages ERROR_FAILED_TO_FIND_METHODS_FOR_TYPE = new LogMessages(ERROR,
            Messages.LOG_ERROR_FAILED_TO_FIND_METHODS_FOR_TYPE);
    public static final LogMessages ERROR_FAILED_TO_FIND_OVERRIDDEN_METHOD_OF_METHOD = new LogMessages(ERROR,
            Messages.LOG_ERROR_FAILED_TO_FIND_OVERRIDDEN_METHOD_OF_METHOD);
    public static final LogMessages ERROR_FAILED_TO_FIND_SUPERTYPE_NAME_OF_TYPE = new LogMessages(ERROR,
            Messages.LOG_ERROR_FAILED_TO_FIND_SUPERTYPE_NAME_OF_TYPE);
    public static final LogMessages ERROR_FAILED_TO_FIND_SUPERTYPE_OF_TYPE = new LogMessages(ERROR,
            Messages.LOG_ERROR_FAILED_TO_FIND_SUPERTYPE_OF_TYPE);
    public static final LogMessages ERROR_FAILED_TO_FIND_TYPE_FROM_SIGNATURE = new LogMessages(ERROR,
            Messages.LOG_ERROR_FAILED_TO_FIND_TYPE_FROM_SIGNATURE);
    public static final LogMessages ERROR_FAILED_TO_FIND_TYPE_OF_FIELD = new LogMessages(ERROR,
            Messages.LOG_ERROR_FAILED_TO_FIND_TYPE_OF_FIELD);
    public static final LogMessages ERROR_FAILED_TO_FIND_TYPE_HIERARCHY_OF_TYPE = new LogMessages(ERROR,
            Messages.LOG_ERROR_FAILED_TO_FIND_TYPE_HIERARCHY_OF_TYPE);
    public static final LogMessages ERROR_FAILED_TO_GENERATE_UUID = new LogMessages(ERROR,
            Messages.LOG_ERROR_FAILED_TO_GENERATE_UUID);
    public static final LogMessages ERROR_FAILED_TO_READ_EXTENSION_ATTRIBUTE = new LogMessages(ERROR,
            Messages.LOG_ERROR_FAILED_TO_READ_EXTENSION_ATTRIBUTE);
    public static final LogMessages ERROR_FAILED_TO_RESOLVE_METHOD = new LogMessages(ERROR,
            Messages.LOG_ERROR_FAILED_TO_RESOLVE_METHOD);
    public static final LogMessages ERROR_FAILED_TO_RESOLVE_SELECTION = new LogMessages(ERROR,
            Messages.LOG_ERROR_FAILED_TO_RESOLVE_SELECTION);
    public static final LogMessages ERROR_FAILED_TO_RESOLVE_TYPE_PARAMETER = new LogMessages(ERROR,
            Messages.LOG_ERROR_FAILED_TO_RESOLVE_TYPE_PARAMETER);
    public static final LogMessages ERROR_FAILED_TO_RESOLVE_UNQUALIFIED_JDT_TYPE = new LogMessages(ERROR,
            Messages.LOG_ERROR_FAILED_TO_RESOLVE_UNQUALIFIED_JDT_TYPE);
    public static final LogMessages ERROR_FAILED_TO_RESOLVE_UNQUALIFIED_TYPE_NAME = new LogMessages(ERROR,
            Messages.LOG_ERROR_FAILED_TO_RESOLVE_UNQUALIFIED_TYPE_NAME);
    public static final LogMessages ERROR_PREFERENCES_NOT_SAVED = new LogMessages(ERROR,
            Messages.LOG_ERROR_PREFERENCES_NOT_SAVED);

    public static final ILogMessage WARNING_EXCEPTION_PARSING_NEWS_FEED = new LogMessages(WARNING,
            Messages.LOG_WARNING_ERROR_WHILE_PARSING_NEWS_FEED);
    public static final ILogMessage WARNING_EXCEPTION_PARSING_NEWS_FEED_ITEM = new LogMessages(WARNING,
            Messages.LOG_WARNING_ERROR_WHILE_PARSING_NEWS_FEED_ITEM);
    public static final LogMessages WARNING_FAILED_TO_FIND_FIELDS_FOR_TYPE = new LogMessages(WARNING,
            Messages.LOG_ERROR_FAILED_TO_FIND_FIELDS_FOR_TYPE);
    public static final LogMessages WARNING_FAILED_TO_FIND_METHODS_FOR_TYPE = new LogMessages(WARNING,
            Messages.LOG_ERROR_FAILED_TO_FIND_METHODS_FOR_TYPE);
    public static final LogMessages WARNING_FAILED_TO_FIND_TYPE_HIERARCHY_OF_TYPE = new LogMessages(WARNING,
            Messages.LOG_ERROR_FAILED_TO_FIND_TYPE_HIERARCHY_OF_TYPE);
    public static final LogMessages WARNING_FAILED_TO_FIND_OVERRIDDEN_METHOD_OF_METHOD = new LogMessages(WARNING,
            Messages.LOG_ERROR_FAILED_TO_FIND_OVERRIDDEN_METHOD_OF_METHOD);
    public static final LogMessages WARNING_FAILED_TO_FIND_SUPERTYPE_NAME_OF_TYPE = new LogMessages(WARNING,
            Messages.LOG_ERROR_FAILED_TO_FIND_SUPERTYPE_NAME_OF_TYPE);
    public static final LogMessages WARNING_FAILED_TO_FIND_SUPERTYPE_OF_TYPE = new LogMessages(WARNING,
            Messages.LOG_ERROR_FAILED_TO_FIND_SUPERTYPE_OF_TYPE);
    public static final LogMessages WARNING_FAILED_TO_FIND_TYPE_FROM_SIGNATURE = new LogMessages(WARNING,
            Messages.LOG_ERROR_FAILED_TO_FIND_TYPE_FROM_SIGNATURE);
    public static final LogMessages WARNING_FAILED_TO_FIND_TYPE_OF_FIELD = new LogMessages(WARNING,
            Messages.LOG_ERROR_FAILED_TO_FIND_TYPE_OF_FIELD);
    public static final LogMessages WARNING_FAILED_TO_RESOLVE_TYPE_PARAMETER = new LogMessages(WARNING,
            Messages.LOG_ERROR_FAILED_TO_RESOLVE_TYPE_PARAMETER);
    public static final LogMessages WARNING_FAILED_TO_RESOLVE_UNQUALIFIED_JDT_TYPE = new LogMessages(WARNING,
            Messages.LOG_ERROR_FAILED_TO_RESOLVE_UNQUALIFIED_JDT_TYPE);
    public static final LogMessages WARNING_FAILED_TO_RESOLVE_UNQUALIFIED_TYPE_NAME = new LogMessages(WARNING,
            Messages.LOG_ERROR_FAILED_TO_RESOLVE_UNQUALIFIED_TYPE_NAME);

    private LogMessages(int severity, String message) {
        super(severity, code++, message);
    }

    @Override
    public Bundle bundle() {
        return BUNDLE;
    }
}
