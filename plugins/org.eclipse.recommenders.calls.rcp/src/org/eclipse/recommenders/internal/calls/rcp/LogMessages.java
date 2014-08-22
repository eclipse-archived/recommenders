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
import org.osgi.framework.Bundle;

public class LogMessages extends DefaultLogMessage {

    private static int code = 1;

    public static final LogMessages LOG_WARNING_MISSING_LOOKUP_ENVIRONMENT = new LogMessages(WARNING,
            Messages.LOG_WARNING_MISSING_LOOKUP_ENVIRONMENT);
    public static final LogMessages LOG_ERROR_PROPOSAL_MATCHING_FAILED = new LogMessages(ERROR,
            Messages.LOG_ERROR_PROPOSAL_MATCHING_FAILED);

    static Bundle bundle = Logs.getBundle(LogMessages.class);

    private LogMessages(int severity, String message) {
        super(severity, code++, message);
    }

    @Override
    public Bundle bundle() {
        return bundle;
    }
}
