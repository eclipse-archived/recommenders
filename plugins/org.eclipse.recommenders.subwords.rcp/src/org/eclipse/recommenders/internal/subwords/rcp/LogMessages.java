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
package org.eclipse.recommenders.internal.subwords.rcp;

import static org.eclipse.core.runtime.IStatus.ERROR;

import org.eclipse.recommenders.utils.Logs;
import org.eclipse.recommenders.utils.Logs.DefaultLogMessage;
import org.osgi.framework.Bundle;

public class LogMessages extends DefaultLogMessage {

    private static int code = 1;

    @SuppressWarnings("restriction")
    public static final LogMessages EXCEPTION_DURING_CODE_COMPLETION = new LogMessages(ERROR,
            org.eclipse.recommenders.internal.completion.rcp.Messages.LOG_ERROR_EXCEPTION_DURING_CODE_COMPLETION);

    static Bundle bundle = Logs.getBundle(LogMessages.class);

    private LogMessages(int severity, String message) {
        super(severity, code++, message);
    }

    @Override
    public Bundle bundle() {
        return bundle;
    }
}
