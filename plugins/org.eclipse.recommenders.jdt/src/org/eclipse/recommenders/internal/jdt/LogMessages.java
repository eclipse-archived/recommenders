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
package org.eclipse.recommenders.internal.jdt;

import static org.eclipse.core.runtime.IStatus.ERROR;

import org.eclipse.recommenders.utils.Logs;
import org.eclipse.recommenders.utils.Logs.DefaultLogMessage;
import org.eclipse.recommenders.utils.Logs.ILogMessage;
import org.osgi.framework.Bundle;

public final class LogMessages extends DefaultLogMessage {

    private static int code = 1;

    private static final Bundle BUNDLE = Logs.getBundle(LogMessages.class);

    public static final LogMessages ERROR_SNIPPET_REPLACE_LEADING_WHITESPACE_FAILED = new LogMessages(ERROR,
            Messages.LOG_ERROR_SNIPPET_REPLACE_LEADING_WHITESPACE_FAILED);

    public static final LogMessages COULD_NOT_FIND_JAVA_PROJECTS = new LogMessages(ERROR, "Could not fetch java projects from workspace");
    public static final ILogMessage COULD_NOT_FIND_PACKAGE_FRAGMENT_ROOTS = new LogMessages(ERROR,
            "Could not fetch package fragment roots of project '{1}'");
    public static final ILogMessage COULD_NOT_FIND_PACKAGE_FRAGMENTS = new LogMessages(ERROR,
            "Could not fetch package fragment of root '{1}'");
    public static final ILogMessage COULD_NOT_FIND_COMPILATION_UNITS = new LogMessages(ERROR,
            "Could not fetch compilation units of package '{1}'");
    public static final ILogMessage COULD_NOT_FIND_TYPE = new LogMessages(ERROR, "Could not find type '{1}' in project '{2}'");

    private LogMessages(int severity, String message) {
        super(severity, code++, message);
    }

    @Override
    public Bundle bundle() {
        return BUNDLE;
    }
}
