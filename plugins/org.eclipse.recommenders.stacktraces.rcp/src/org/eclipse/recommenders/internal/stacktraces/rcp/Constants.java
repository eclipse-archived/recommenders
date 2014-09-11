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

public class Constants {
    static final String PLUGIN_ID = "org.eclipse.recommenders.stacktraces.rcp";
    static final String PROP_NAME = "name";
    static final String PROP_EMAIL = "email";
    static final String PROP_ANONYMIZE_STACKTRACES = "anonymize-stacktraces";
    static final String PROP_ANONYMIZE_MESSAGES = "anonymize-messages";
    static final String PROP_SEND_ACTION = "send-action";
    static final String PROP_SKIP_SIMILAR_ERRORS = "skip-similar-errors";
    static final String PROP_WHITELISTED_PLUGINS = "whitelisted-plugins";
    static final String PROP_WHITELISTED_PACKAGES = "whitelisted-packages";
    static final String PROP_SERVER = "server-url";

    static final String HELP_URL = "https://docs.google.com/document/d/14vRLXcgSwy0rEbpJArsR_FftOJW1SjWUAmZuzc2O8YI/pub";
    static final String FEEDBACK_FORM_URL = "https://docs.google.com/a/codetrails.com/forms/d/1wd9AzydLv_TMa7ZBXHO7zQIhZjZCJRNMed-6J4fVNsc/viewform";
    static final String SERVER_URL = getServerUrl();

    private static String getServerUrl() {
        return System.getProperty(PLUGIN_ID + "." + PROP_SERVER,
                "http://recommenders.eclipse.org/stats/stacktraces/0.5.0/new/");
    }
}
