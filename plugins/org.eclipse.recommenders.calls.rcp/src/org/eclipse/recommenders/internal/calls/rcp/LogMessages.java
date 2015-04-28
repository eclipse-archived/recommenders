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
package org.eclipse.recommenders.internal.calls.rcp;

import static org.eclipse.core.runtime.IStatus.*;

import org.eclipse.recommenders.utils.Logs;
import org.eclipse.recommenders.utils.Logs.DefaultLogMessage;
import org.eclipse.recommenders.utils.Logs.ILogMessage;
import org.osgi.framework.Bundle;

public final class LogMessages extends DefaultLogMessage {

    private static int code = 1;

    private static final Bundle BUNDLE = Logs.getBundle(LogMessages.class);

    public static final LogMessages ERROR_PROPOSAL_MATCHING_FAILED = new LogMessages(ERROR,
            Messages.LOG_ERROR_PROPOSAL_MATCHING_FAILED);
    public static final ILogMessage ERROR_RECEIVER_TYPE_LOOKUP_FAILED = new LogMessages(ERROR,
            Messages.LOG_ERROR_RECEIVER_TYPE_LOOKUP_FAILED);

    public static final LogMessages WARNING_MISSING_LOOKUP_ENVIRONMENT = new LogMessages(WARNING,
            Messages.LOG_WARNING_MISSING_LOOKUP_ENVIRONMENT);

    private LogMessages(int severity, String message) {
        super(severity, code++, message);
    }

    @Override
    public Bundle bundle() {
        return BUNDLE;
    }
}
