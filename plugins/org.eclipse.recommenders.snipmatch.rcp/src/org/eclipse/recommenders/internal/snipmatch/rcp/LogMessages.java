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
package org.eclipse.recommenders.internal.snipmatch.rcp;

import static org.eclipse.core.runtime.IStatus.ERROR;

import org.eclipse.recommenders.utils.Logs;
import org.eclipse.recommenders.utils.Logs.DefaultLogMessage;
import org.osgi.framework.Bundle;

public class LogMessages extends DefaultLogMessage {

    private static int code = 1;

    public static final LogMessages ERROR_CREATING_SNIPPET_PROPOSAL_FAILED = new LogMessages(ERROR,
            Messages.ERROR_CREATING_SNIPPET_PROPOSAL_FAILED);

    public static final LogMessages SNIPPET_REPLACE_LEADING_WHITESPACE_FAILED = new LogMessages(ERROR,
            "An error occured while determining the leading whitespace characters.");

    static Bundle bundle = Logs.getBundle(LogMessages.class);

    private LogMessages(int severity, String message) {
        super(severity, code++, message);
    }

    @Override
    public Bundle bundle() {
        return bundle;
    }
}
