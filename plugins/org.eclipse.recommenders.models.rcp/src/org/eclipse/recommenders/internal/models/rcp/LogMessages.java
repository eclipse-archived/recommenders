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
package org.eclipse.recommenders.internal.models.rcp;

import static org.eclipse.core.runtime.IStatus.ERROR;

import org.eclipse.recommenders.utils.Logs;
import org.eclipse.recommenders.utils.Logs.DefaultLogMessage;
import org.osgi.framework.Bundle;

public class LogMessages extends DefaultLogMessage {

    public static final LogMessages SAVE_PREFERENCES_FAILED = new LogMessages(ERROR,
            Messages.LOG_ERROR_SAVE_PREFERENCES);
    public static final LogMessages ADVISOR_INSTANTIATION_FAILED = new LogMessages(ERROR,
            Messages.LOG_ERROR_ADVISOR_INSTANTIATION);

    static Bundle bundle = Logs.getBundle(LogMessages.class);

    private static int code = 1;

    private LogMessages(int severity, String message) {
        super(severity, code++, message);
    }

    @Override
    public Bundle bundle() {
        return bundle;
    }
}
