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
package org.eclipse.recommenders.internal.stacktraces.rcp;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.recommenders.internal.stacktraces.rcp.messages"; //$NON-NLS-1$
    public static String CONFIGURATIONDIALOG_ACTION_TOOLTIP;
    public static String CONFIGURATIONDIALOG_ANONYMIZATION;
    public static String CONFIGURATIONDIALOG_DISABLE;
    public static String CONFIGURATIONDIALOG_ENABLE;
    public static String CONFIGURATIONDIALOG_INFO;
    public static String CONFIGURATIONDIALOG_PLEASE_TAKE_MOMENT_TO_CONFIGURE;
    public static String CONFIGURATIONDIALOG_PREFERENCE_PAGE_LINK;
    public static String CONFIGURATIONDIALOG_REPORTING_STARTED_FIRST_TIME;
    public static String ERRORREPORTWIZARD_WE_NOTICED_ERROR;
    public static String FIELD_LABEL_SERVER;
    public static String FIELD_LABEL_ACTION;
    public static String FIELD_LABEL_ACTION_REPORT_ASK;
    public static String FIELD_LABEL_ACTION_REPORT_NEVER;
    public static String FIELD_LABEL_ACTION_REPORT_ALWAYS;
    public static String FIELD_LABEL_ACTION_REPORT_PAUSE_DAY;
    public static String FIELD_LABEL_ACTION_REPORT_PAUSE_RESTART;
    public static String FIELD_LABEL_SKIP_SIMILAR_ERRORS;
    public static String FIELD_LABEL_ANONYMIZE_STACKTRACES;
    public static String FIELD_LABEL_ANONYMIZE_MESSAGES;
    public static String FIELD_LABEL_NAME;
    public static String FIELD_DESC_NAME;
    public static String FIELD_MESSAGE_NAME;
    public static String FIELD_LABEL_EMAIL;
    public static String FIELD_DESC_EMAIL;
    public static String FIELD_MESSAGE_EMAIL;
    public static String SETTINGSPAGE_TITLE;
    public static String SETTINGSPAGE_DESC;
    public static String SETTINGSPAGE_GROUPLABEL_PERSONAL;
    public static String LINK_LEARN_MORE;
    public static String LINK_PROVIDE_FEEDBACK;
    public static String PREFERENCEPAGE_ASK_LABEL;
    public static String PREVIEWPAGE_DESC;
    public static String PREVIEWPAGE_LABEL_COMMENT;
    public static String PREVIEWPAGE_LABEL_MESSAGE;
    public static String PREVIEWPAGE_TITLE;
    public static String THANKYOUDIALOG_COMMITTER_MESSAGE;
    public static String THANKYOUDIALOG_INVALID_SERVER_RESPONSE;
    public static String THANKYOUDIALOG_MARKED_DUPLICATE;
    public static String THANKYOUDIALOG_MARKED_FIXED;
    public static String THANKYOUDIALOG_MARKED_MOVED;
    public static String THANKYOUDIALOG_MARKED_NORMAL;
    public static String THANKYOUDIALOG_MARKED_UNKNOWN;
    public static String THANKYOUDIALOG_MATCHED_EXISTING_BUG;
    public static String THANKYOUDIALOG_COMMITTER_MESSAGE_EMPTY;
    public static String THANKYOUDIALOG_MARKED_WORKSFORME;
    public static String THANKYOUDIALOG_RECEIVED_AND_TRACKED;
    public static String THANKYOUDIALOG_RECEIVED_UNKNOWN_SERVER_RESPONSE;
    public static String THANKYOUDIALOG_THANK_YOU;
    public static String THANKYOUDIALOG_THANK_YOU_FOR_HELP;
    public static String THANKYOUDIALOG_NEW;
    public static String THANKYOUDIALOG_ADDITIONAL_INFORMATION;
    public static String TOOLTIP_MAKE_STACKTRACE_ANONYMOUS;
    public static String TOOLTIP_MAKE_MESSAGES_ANONYMOUS;
    public static String TOOLTIP_SKIP_SIMILAR;
    public static String UPLOADJOB_ALREADY_FIXED_UPDATE;
    public static String UPLOADJOB_BAD_RESPONSE;
    public static String UPLOADJOB_FAILED_WITH_EXCEPTION;
    public static String UPLOADJOB_NAME;
    public static String UPLOADJOB_NEED_FURTHER_INFORMATION;
    public static String UPLOADJOB_TASKNAME;
    public static String UPLOADJOB_THANK_YOU;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
