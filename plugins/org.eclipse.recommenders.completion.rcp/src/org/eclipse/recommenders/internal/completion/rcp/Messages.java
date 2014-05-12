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
package org.eclipse.recommenders.internal.completion.rcp;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.recommenders.internal.completion.rcp.messages"; //$NON-NLS-1$

    public static String PROPOSAL_LABEL_DISCOVER_EXTENSIONS;
    public static String PROPOSAL_LABEL_ENABLE_COMPLETION;
    public static String PROPOSAL_LABEL_NO_PROPOSALS;

    public static String PROPOSAL_TOOLTIP_DISCOVER_EXTENSIONS;
    public static String PROPOSAL_TOOLTIP_ENABLE_COMPLETION;

    public static String PROPOSAL_CATEGORY_CODE_RECOMMENDERS;

    public static String BROWSER_LABEL_PROJECT_WEBSITE;

    public static String BROWSER_TOOLTIP_PROJECT_WEBSITE;

    public static String PREFPAGE_TITLE_COMPLETIONS;

    public static String PREFPAGE_DESCRIPTION_COMPLETIONS;

    public static String PREFPAGE_FOOTER_COMPLETIONS;

    public static String BUTTON_LABEL_CONFIGURE;

    public static String FIELD_LABEL_SESSION_PROCESSORS;

    public static String FIELD_LABEL_ENABLE_COMPLETION;

    public static String FIELD_TOOLTIP_ENABLE_COMPLETION;

    public static String DIALOG_TITLE_FAILED_TO_GUESS_PARAMETERS;
    public static String DIALOG_TITLE_FAILURE;

    public static String JOB_DISABLING_CONTENT_ASSIST_CATEGORY;

    public static String LOG_ERROR_EXCEPTION_DURING_CODE_COMPLETION;
    public static String LOG_ERROR_SESSION_PROCESSOR_FAILED;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
