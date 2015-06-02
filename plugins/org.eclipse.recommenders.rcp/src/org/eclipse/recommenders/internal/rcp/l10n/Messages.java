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
package org.eclipse.recommenders.internal.rcp.l10n;

import org.eclipse.osgi.util.NLS;

public final class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.recommenders.internal.rcp.l10n.messages"; //$NON-NLS-1$

    public static String PREFPAGE_DESCRIPTION_EMPTY;
    public static String PREFPAGE_LINKS_DESCRIPTION;

    public static String LOG_ERROR_ACTIVE_PAGE_FINDER_TOO_EARLY;
    public static String LOG_ERROR_EXCEPTION_OCCURRED_IN_SERVICE_HOOK;
    public static String LOG_ERROR_PREFERENCES_NOT_SAVED;
    public static String LOG_ERROR_FAILED_TO_RESOLVE_SELECTION;
    public static String LOG_ERROR_EXCEPTION_WHILE_CHECKING_OFFSETS;
    public static String LOG_ERROR_ARRAY_TYPE_IN_JAVA_ELEMENT_RESOLVER;
    public static String LOG_ERROR_FAILED_TO_RESOLVE_METHOD;
    public static String LOG_ERROR_FAILED_TO_GENERATE_UUID;
    public static String LOG_ERROR_FAILED_TO_RESOLVE_TYPE_PARAMETER;
    public static String LOG_ERROR_FAILED_TO_EXECUTE_COMMAND;
    public static String LOG_ERROR_FAILED_TO_READ_EXTENSION_ATTRIBUTE;
    public static String LOG_ERROR_AN_ERROR_OCCURRED;
    public static String LOG_WARNING_ERROR_WHILE_PARSING_NEWS_FEED;
    public static String LOG_WARNING_ERROR_WHILE_PARSING_NEWS_FEED_ITEM;

    public static String DIALOG_TITLE_BUNDLE_RESOLUTION_FAILURE;
    public static String DIALOG_MESSAGE_BUNDLE_RESOLUTION_FAILURE;
    public static String DIALOG_TOGGLE_IGNORE_BUNDLE_RESOLUTION_FAILURES;
    public static String DIALOG_MESSAGE_BUNDLE_RESOLUTION_FAQ;
    public static String DIALOG_MESSAGE_BUNDLE_RESOLUTION_FILE_A_BUG;
    public static String DIALOG_BUTTON_RESTART;
    public static String DIALOG_LABEL_BUNDLE_LIST;
    public static String DIALOG_RESTART_NOT_POSSIBLE;

    public static String NEWS_NOTIFY_MESSAGE;
    public static String NEWS_LOADING_MESSAGE;
    public static String NEWS_TURN_OFF_MESSAGE;

    public static String LABEL_NOTIFICATION_NAME;

    public static String JOB_NAME_CLOSE;
    public static String JOB_NAME_INITIALIZING_PROJECTS;
    public static String JOB_NAME_FADE;
    public static String JOB_NAME_NETWORK_COMMUNCIATION_TEST;
    public static String JOB_NAME_SELECTION_LISTENER_REGISTRATION;

    public static String LOG_ERROR_ON_PROXY_AUTHENTICATION_TEST;
    public static String LOG_ERROR_ON_APACHE_HEAD_REQUEST;
    public static String LOG_ERROR_ON_P2_HEAD_REQUEST;

    public static String TASK_NETWORK_COMMUNICATION_TEST;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
