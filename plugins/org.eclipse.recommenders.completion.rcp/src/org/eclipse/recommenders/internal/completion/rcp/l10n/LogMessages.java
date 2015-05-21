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
package org.eclipse.recommenders.internal.completion.rcp.l10n;

import static org.eclipse.core.runtime.IStatus.*;

import org.eclipse.recommenders.utils.Logs;
import org.eclipse.recommenders.utils.Logs.DefaultLogMessage;
import org.eclipse.recommenders.utils.Logs.ILogMessage;
import org.osgi.framework.Bundle;

public final class LogMessages extends DefaultLogMessage {

    private static int code = 1;

    private static final Bundle BUNDLE = Logs.getBundle(LogMessages.class);

    public static final ILogMessage ERROR_COULD_NOT_CONVERT_METHOD_BINDING_TO_METHOD_NAME = new LogMessages(ERROR,
            Messages.LOG_ERROR_COULD_NOT_CONVERT_METHOD_BINDING_TO_METHOD_NAME);
    public static final ILogMessage ERROR_COULD_NOT_DETERMINE_DECLARING_TYPE = new LogMessages(ERROR,
            Messages.LOG_ERROR_COULD_NOT_DETERMINE_DECLARING_TYPE);
    public static final ILogMessage ERROR_FAILED_TO_LOOK_UP_COMPOUND_NAME = new LogMessages(ERROR,
            Messages.LOG_ERROR_FAILED_TO_LOOK_UP_COMPOUND_NAME);
    public static final LogMessages ERROR_SESSION_PROCESSOR_FAILED = new LogMessages(ERROR,
            Messages.LOG_ERROR_SESSION_PROCESSOR_FAILED);
    public static final LogMessages ERROR_EXCEPTION_DURING_CODE_COMPLETION = new LogMessages(ERROR,
            Messages.LOG_ERROR_EXCEPTION_DURING_CODE_COMPLETION);
    public static final LogMessages ERROR_EXCEPTION_DURING_CODE_COMPLETION_AT_OFFSET = new LogMessages(ERROR,
            Messages.LOG_ERROR_EXCEPTION_DURING_CODE_COMPLETION_AT_OFFSET);
    public static final LogMessages ERROR_EXCEPTION_WHILE_COMPUTING_LOOKUP_ENVIRONMENT = new LogMessages(ERROR,
            Messages.LOG_ERROR_EXCEPTION_WHILE_COMPUTING_LOOKUP_ENVIRONMENT);
    public static final LogMessages ERROR_COMPILATION_FAILURE_PREVENTS_PROPOSAL_MATCHING = new LogMessages(ERROR,
            Messages.LOG_ERROR_COMPILATION_FAILURE_PREVENTS_PROPOSAL_MATCHING);
    public static final ILogMessage ERROR_UNEXPECTED_PROPOSAL_KIND = new LogMessages(ERROR,
            Messages.LOG_ERROR_UNEXPECTED_PROPOSAL_KIND);

    public static final LogMessages WARNING_LINKAGE_ERROR = new LogMessages(WARNING, Messages.LOG_WARNING_LINKAGE_ERROR);


    private LogMessages(int severity, String message) {
        super(severity, code++, message);
    }

    @Override
    public Bundle bundle() {
        return BUNDLE;
    }
}
