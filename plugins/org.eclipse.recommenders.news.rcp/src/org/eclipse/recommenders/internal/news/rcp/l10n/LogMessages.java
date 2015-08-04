/**
 * Copyright (c) 2015 Codetrails GmbH. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Johannes Dorn - initial API and implementation.
 */
package org.eclipse.recommenders.internal.news.rcp.l10n;

import static org.eclipse.core.runtime.IStatus.*;

import org.eclipse.recommenders.internal.news.rcp.Logs;
import org.eclipse.recommenders.internal.news.rcp.Logs.DefaultLogMessage;
import org.osgi.framework.Bundle;

public final class LogMessages extends DefaultLogMessage {

    private static int code = 1;

    private static final Bundle BUNDLE = Logs.getBundle(LogMessages.class);

    public static final LogMessages ERROR_READING_PROPERTIES = new LogMessages(ERROR,
            Messages.LOG_ERROR_READING_PROPERTIES);
    public static final LogMessages ERROR_WRITING_PROPERTIES = new LogMessages(ERROR,
            Messages.LOG_ERROR_WRITING_PROPERTIES);

    public static final LogMessages ERROR_CONNECTING_URL = new LogMessages(ERROR, Messages.LOG_ERROR_CONNECTING_URL);

    public static final LogMessages ERROR_FETCHING_MESSAGES = new LogMessages(ERROR,
            Messages.LOG_ERROR_FETCHING_MESSAGES);

    public static final LogMessages WARNING_DUPLICATE_FEED = new LogMessages(WARNING,
            Messages.LOG_WARNING_DUPLICATE_FEED);

    public static final LogMessages ERROR_FEED_MALFORMED_URL = new LogMessages(ERROR,
            Messages.FEED_DESCRIPTOR_MALFORMED_URL);

    public static final LogMessages ERROR_CONNECTING_URL_WITH_STATUS_CODE = new LogMessages(ERROR,
            Messages.LOG_ERROR_CONNECTING_URL_WITH_STATUS_CODE);

    private LogMessages(int severity, String message) {
        super(severity, code++, message);
    }

    @Override
    public Bundle bundle() {
        return BUNDLE;
    }
}
