/**
 * Copyright (c) 2010, 2014 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Sewe - initial API and implementation.
 */
package org.eclipse.recommenders.internal.completion.rcp.l10n;

import org.eclipse.osgi.util.NLS;

public final class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.recommenders.internal.completion.rcp.l10n.messages"; //$NON-NLS-1$

    public static String PROPOSAL_LABEL_DISCOVER_EXTENSIONS;
    public static String PROPOSAL_LABEL_ENABLED_COMPLETION;
    public static String PROPOSAL_LABEL_NO_PROPOSALS;

    public static String PROPOSAL_TOOLTIP_DISCOVER_EXTENSIONS;
    public static String PROPOSAL_TOOLTIP_ENABLED_COMPLETION;
    public static String PROPOSAL_CATEGORY_CODE_RECOMMENDERS;

    public static String PREFPAGE_TITLE_COMPLETIONS;

    public static String PREFPAGE_DESCRIPTION_COMPLETIONS;

    public static String PREFPAGE_FOOTER_COMPLETIONS;

    public static String BUTTON_LABEL_CONFIGURE;

    public static String FIELD_LABEL_SESSION_PROCESSORS;
    public static String FIELD_LABEL_ENABLE_COMPLETION;

    public static String FIELD_TOOLTIP_ENABLE_COMPLETION;

    public static String FIELD_LABEL_DECORATE_PROPOSAL_ICON;
    public static String FIELD_LABEL_DECORATE_PROPOSAL_TEXT;
    public static String FIELD_LABEL_MAX_NUMBER_OF_PROPOSALS;
    public static String FIELD_LABEL_MIN_PROPOSAL_PERCENTAGE;
    public static String FIELD_LABEL_UPDATE_PROPOSAL_RELEVANCE;

    public static String JOB_DISABLING_CONTENT_ASSIST_CATEGORY;
    public static String JOB_ENABLING_CONTENT_ASSIST_CATEGORY;

    public static String LOG_ERROR_COMPLETION_FAILURE_DURING_DEBUG_MODE;
    public static String LOG_ERROR_EXCEPTION_DURING_CODE_COMPLETION;
    public static String LOG_ERROR_EXCEPTION_DURING_CODE_COMPLETION_AT_OFFSET;
    public static String LOG_ERROR_EXCEPTION_WHILE_COMPUTING_LOOKUP_ENVIRONMENT;
    public static String LOG_ERROR_FAILED_TO_FLUSH_PREFERENCES;
    public static String LOG_ERROR_FAILED_TO_INSTANTIATE_COMPLETION_TIP;
    public static String LOG_ERROR_FAILED_TO_LOAD_COMPLETION_PROPOSAL_CLASS;
    public static String LOG_ERROR_FAILED_TO_PARSE_TYPE_NAME;
    public static String LOG_ERROR_FAILED_TO_SET_PROPOSAL_INFO;
    public static String LOG_ERROR_FAILED_TO_WRAP_JDT_PROPOSAL;
    public static String LOG_ERROR_SESSION_PROCESSOR_FAILED;
    public static String LOG_ERROR_SYNTATICALLY_INCORRECT_METHOD_NAME;
    public static String LOG_ERROR_UNEXPECTED_PROPOSAL_KIND;
    public static String LOG_ERROR_PROPOSAL_MATCHING_FAILED;

    public static String LOG_INFO_FALLBACK_METHOD_NAME_CREATION;

    public static String LOG_WARNING_LINKAGE_ERROR;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
