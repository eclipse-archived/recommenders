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
package org.eclipse.recommenders.internal.utils.rcp.l10n;

import static org.eclipse.core.runtime.IStatus.ERROR;

import org.eclipse.recommenders.utils.Logs;
import org.eclipse.recommenders.utils.Logs.DefaultLogMessage;
import org.eclipse.recommenders.utils.Logs.ILogMessage;
import org.osgi.framework.Bundle;

public final class LogMessages extends DefaultLogMessage {

    private static final Bundle BUNDLE = Logs.getBundle(LogMessages.class);

    private static int code = 1;

    public static final ILogMessage ERROR_FAILED_TO_EXECUTE_COMMAND = new LogMessages(ERROR,
            Messages.LOG_ERROR_FAILED_TO_EXECUTE_COMMAND);
    public static final ILogMessage ERROR_MALFORMED_URI = new LogMessages(ERROR,
            Messages.LOG_ERROR_MALFORMED_URI);
    public static final ILogMessage ERROR_FAILED_TO_READ_EXTENSION_ATTRIBUTE = new LogMessages(ERROR,
            Messages.LOG_ERROR_FAILED_TO_READ_EXTENSION_ATTRIBUTE);

    private LogMessages(int severity, String message) {
        super(severity, code++, message);
    }

    @Override
    public Bundle bundle() {
        return BUNDLE;
    }
}
