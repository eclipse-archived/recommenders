/**
 * Copyright (c) 2014 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Sewe - initial API and implementation.
 */
package org.eclipse.recommenders.internal.jdt.l10n;

import static org.eclipse.core.runtime.IStatus.*;

import org.eclipse.recommenders.utils.Logs;
import org.eclipse.recommenders.utils.Logs.DefaultLogMessage;
import org.eclipse.recommenders.utils.Logs.ILogMessage;
import org.osgi.framework.Bundle;

public final class LogMessages extends DefaultLogMessage {

    private static final Bundle BUNDLE = Logs.getBundle(LogMessages.class);

    private static int code = 1;

    public static final ILogMessage ERROR_CANNOT_DETERMINE_LOCATION = new LogMessages(ERROR,
            Messages.LOG_ERROR_CANNOT_DETERMINE_LOCATION);
    public static final ILogMessage ERROR_CANNOT_FETCH_ALL_PACKAGE_FRAGMENT_ROOTS = new LogMessages(ERROR,
            Messages.LOG_ERROR_CANNOT_FETCH_ALL_PACKAGE_FRAGMENT_ROOTS);
    public static final ILogMessage ERROR_CANNOT_FETCH_CLASS_FILES = new LogMessages(ERROR,
            Messages.LOG_ERROR_CANNOT_FETCH_CLASS_FILES);
    public static final ILogMessage ERROR_CANNOT_FETCH_COMPILATION_UNITS = new LogMessages(ERROR,
            Messages.LOG_ERROR_CANNOT_FETCH_COMPILATION_UNITS);
    public static final ILogMessage ERROR_CANNOT_FETCH_JAVA_PROJECTS = new LogMessages(ERROR,
            Messages.LOG_ERROR_CANNOT_FETCH_JAVA_PROJECTS);
    public static final ILogMessage ERROR_CANNOT_FETCH_PACKAGE_FRAGMENT = new LogMessages(ERROR,
            Messages.LOG_ERROR_CANNOT_FETCH_PACKAGE_FRAGMENT);
    public static final ILogMessage ERROR_CANNOT_FETCH_PACKAGE_FRAGMENT_ROOT_KIND = new LogMessages(ERROR,
            Messages.LOG_ERROR_CANNOT_FETCH_PACKAGE_FRAGMENT_ROOT_KIND);
    public static final ILogMessage ERROR_CANNOT_FETCH_PACKAGE_FRAGMENT_ROOTS = new LogMessages(ERROR,
            Messages.LOG_ERROR_CANNOT_FETCH_PACKAGE_FRAGMENT_ROOTS);
    public static final ILogMessage ERROR_CANNOT_FETCH_SOURCE_ATTACHMENT_PATH = new LogMessages(ERROR,
            Messages.LOG_ERROR_CANNOT_FETCH_SOURCE_ATTACHMENT_PATH);
    public static final ILogMessage ERROR_CANNOT_FETCH_TYPES = new LogMessages(ERROR,
            Messages.LOG_ERROR_CANNOT_FETCH_TYPES);
    public static final ILogMessage ERROR_CANNOT_FIND_TYPE_IN_PROJECT = new LogMessages(ERROR,
            Messages.LOG_ERROR_CANNOT_FIND_TYPE_IN_PROJECT);
    public static final ILogMessage ERROR_FAILED_TO_CREATE_METHODNAME = new LogMessages(ERROR,
            Messages.LOG_ERROR_FAILED_TO_CREATE_METHODNAME);
    public static final ILogMessage ERROR_FAILED_TO_CREATE_TYPENAME = new LogMessages(ERROR,
            Messages.LOG_ERROR_FAILED_TO_CREATE_TYPENAME);
    public static final ILogMessage ERROR_SNIPPET_REPLACE_LEADING_WHITESPACE_FAILED = new LogMessages(ERROR,
            Messages.LOG_ERROR_SNIPPET_REPLACE_LEADING_WHITESPACE_FAILED);

    public static final ILogMessage WARN_FAILED_TO_GET_TEXT_SELECTION = new LogMessages(WARNING,
            Messages.LOG_WARN_FAILED_TO_GET_TEXT_SELECTION);

    private LogMessages(int severity, String message) {
        super(severity, code++, message);
    }

    @Override
    public Bundle bundle() {
        return BUNDLE;
    }
}
