/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Olav Lenz - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp;

import org.eclipse.osgi.util.NLS;

public final class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.recommenders.internal.rcp.messages"; //$NON-NLS-1$

    public static String PREFPAGE_DESCRIPTION_EMPTY;

    public static String JOB_INITIALIZE_PROJECTS;

    public static String LOG_ERROR_ACTIVE_PAGE_FINDER_TOO_EARLY;
    public static String LOG_ERROR_EXCEPTION_IN_SERVICE_HOOK;
    public static String LOG_ERROR_PREFERENCES_NOT_SAVED;
    public static String LOG_ERROR;
    public static String LOG_WARNING;
    public static String LOG_INFO;
    public static String LOG_OK;
    public static String LOG_CANCEL;

    public static String DIALOG_TITLE_BUNDLE_RESOLUTION_FAILURE;
    public static String DIALOG_MESSAGE_BUNDLE_RESOLUTION_FAILURE;
    public static String DIALOG_TOGGLE_IGNORE_BUNDLE_RESOLUTION_FAILURES;
    public static String DIALOG_MESSAGE_BUNDLE_RESOLUTION_FAQ;
    public static String DIALOG_MESSAGE_BUNDLE_RESOLUTION_FILE_A_BUG;
    public static String DIALOG_BUTTON_RESTART;
    public static String DIALOG_LABEL_BUNDLE_LIST;
    public static String DIALOG_RESTART_NOT_POSSIBLE;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
