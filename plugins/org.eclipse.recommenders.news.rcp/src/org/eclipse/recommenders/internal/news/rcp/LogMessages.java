/**
 * Copyright (c) 2015 Codetrails GmbH. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Johannes Dorn - initial API and implementation.
 */
package org.eclipse.recommenders.internal.news.rcp;

import static org.eclipse.core.runtime.IStatus.ERROR;

import org.eclipse.recommenders.utils.Logs;
import org.eclipse.recommenders.utils.Logs.DefaultLogMessage;
import org.osgi.framework.Bundle;

public final class LogMessages extends DefaultLogMessage {

    private static int code = 1;

    private static final Bundle BUNDLE = Logs.getBundle(LogMessages.class);

    public static final LogMessages ERROR_READING_PROPERTIES = new LogMessages(ERROR,
            Messages.LOG_ERROR_READING_PROPERTIES);
    public static final LogMessages ERROR_WRITING_PROPERTIES = new LogMessages(ERROR,
            Messages.LOG_ERROR_WRITING_PROPERTIES);

    private LogMessages(int severity, String message) {
        super(severity, code++, message);
    }

    @Override
    public Bundle bundle() {
        return BUNDLE;
    }
}
