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
package org.eclipse.recommenders.internal.apidocs.rcp;

import static org.eclipse.core.runtime.IStatus.*;

import org.eclipse.recommenders.utils.Logs;
import org.eclipse.recommenders.utils.Logs.DefaultLogMessage;
import org.eclipse.recommenders.utils.Logs.ILogMessage;
import org.osgi.framework.Bundle;

public class LogMessages extends DefaultLogMessage {

    private static int code = 1;

    public static final LogMessages FAILED_TO_DETERMINE_STATIC_MEMEBERS = new LogMessages(ERROR,
            "Failed to determine static members for ''{0}''");
    public static final LogMessages ERROR_DURING_JAVADOC_SELECTION = new LogMessages(ERROR,
            "Error during javadoc selection ''{0}''");
    public static final ILogMessage FAILED_TO_INSTANTIATE_PROVIDER = new LogMessages(ERROR,
            "failed to instantiate provider ''{0}'': {1}");

    public static final ILogMessage FAILED_TO_CLEAR_PREFERENCES = new LogMessages(ERROR,
            Messages.LOG_ERROR_CLEAR_PREFERENCES);
    public static final ILogMessage FAILED_TO_READ_PREFERENCES = new LogMessages(ERROR,
            Messages.LOG_ERROR_READ_PREFERENCES);
    public static final ILogMessage FAILED_TO_SAVE_PREFERENCES = new LogMessages(ERROR,
            Messages.LOG_ERROR_SAVE_PREFERENCES);

    public static final ILogMessage NO_SUCH_ENTRY = new LogMessages(WARNING, Messages.LOG_WARNING_NO_SUCH_ENTRY);

    static Bundle bundle = Logs.getBundle(LogMessages.class);

    private LogMessages(int severity, String message) {
        // we are a bit lazy and don't use fixed error codes. But this this is likely not too much of a problem.
        super(severity, code++, message);
    }

    @Override
    public Bundle bundle() {
        return bundle;
    }
}
