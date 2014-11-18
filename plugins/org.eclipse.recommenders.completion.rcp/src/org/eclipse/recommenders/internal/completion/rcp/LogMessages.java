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
package org.eclipse.recommenders.internal.completion.rcp;

import static org.eclipse.core.runtime.IStatus.*;

import org.eclipse.recommenders.utils.Logs;
import org.eclipse.recommenders.utils.Logs.DefaultLogMessage;
import org.osgi.framework.Bundle;

public final class LogMessages extends DefaultLogMessage {

    private static int code = 1;

    private static final Bundle BUNDLE = Logs.getBundle(LogMessages.class);

    public static final LogMessages LOG_ERROR_SESSION_PROCESSOR_FAILED = new LogMessages(ERROR,
            Messages.LOG_ERROR_SESSION_PROCESSOR_FAILED);
    public static final LogMessages LOG_ERROR_EXCEPTION_DURING_CODE_COMPLETION = new LogMessages(ERROR,
            Messages.LOG_ERROR_EXCEPTION_DURING_CODE_COMPLETION);
    public static final LogMessages LOG_ERROR_COMPILATION_FAILURE_PREVENTS_PROPOSAL_MATCHING = new LogMessages(ERROR,
            Messages.LOG_ERROR_COMPILATION_FAILURE_PREVENTS_PROPOSAL_MATCHING);
    public static final LogMessages LOG_WARNING_LINKAGE_ERROR = new LogMessages(WARNING,
            Messages.LOG_WARNING_LINKAGE_ERROR);

    private LogMessages(int severity, String message) {
        super(severity, code++, message);
    }

    @Override
    public Bundle bundle() {
        return BUNDLE;
    }
}
