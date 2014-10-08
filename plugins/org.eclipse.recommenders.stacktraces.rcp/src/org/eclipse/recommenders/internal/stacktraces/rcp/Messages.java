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
    public static String SETTINGSPAGE_TITEL;
    public static String SETTINGSPAGE_DESC;
    public static String SETTINGSPAGE_GROUPLABEL_PERSONAL;
    public static String LINK_LEARN_MORE;
    public static String LINK_PROVIDE_FEEDBACK;
    public static String PREVIEWPAGE_DESC;
    public static String PREVIEWPAGE_LABEL_COMMENT;
    public static String PREVIEWPAGE_LABEL_MESSAGE;
    public static String PREVIEWPAGE_TITLE;
    public static String THANKYOUDIALOG_FURTHER_INFORMATION;
    public static String THANKYOUDIALOG_INVALID_SERVER_RESPONSE;
    public static String THANKYOUDIALOG_MARKED_DUPLICATE;
    public static String THANKYOUDIALOG_MARKED_FIXED;
    public static String THANKYOUDIALOG_MARKED_MOVED;
    public static String THANKYOUDIALOG_MARKED_NORMAL;
    public static String THANKYOUDIALOG_MARKED_UNKNOWN;
    public static String THANKYOUDIALOG_MATCHED_NEED_FURTHER_INFORMATION;
    public static String THANKYOUDIALOG_MATCHED_PLEASE_ADD_TO_CC;
    public static String THANKYOUDIALOG_NO_FURTHER_INFORMATIONS;
    public static String THANKYOUDIALOG_NOT_ABLE_TO_REPRODUCE_PLEASE_VISIT;
    public static String THANKYOUDIALOG_PLEASE_NOTE_ADDITIONAL_PERMISSIONS;
    public static String THANKYOUDIALOG_RECEIVED_AND_TRACKED;
    public static String THANKYOUDIALOG_RECEIVED_UNKNOWN_SERVER_RESPONSE;
    public static String THANKYOUDIALOG_THANK_YOU;
    public static String THANKYOUDIALOG_THANK_YOU_FOR_HELP;
    public static String THANKYOUDIALOG_TRACKED_PLEASE_ADD_TO_CC;
    public static String THANKYOUDIALOG_ADDITIONAL_INFORMATIONS;
    public static String TOOLTIP_ANONYMIZE_STACKTRACES;
    public static String TOOLTIP_CLEAR_MESSAGES;
    public static String TOOLTIP_SKIP_SIMILAR;
    public static String UPLOADJOB_BAD_RESPONSE;
    public static String UPLOADJOB_FAILED_WITH_EXCEPTION;
    public static String UPLOADJOB_NAME;
    public static String UPLOADJOB_TASKNAME;
    public static String UPLOADJOB_THANK_YOU;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
