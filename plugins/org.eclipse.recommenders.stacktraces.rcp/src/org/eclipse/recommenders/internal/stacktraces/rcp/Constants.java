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

import java.util.Set;

import org.eclipse.recommenders.internal.stacktraces.rcp.model.ModelPackage;

import com.google.common.collect.ImmutableSet;

public final class Constants {

    private Constants() {
        throw new IllegalStateException("Not meant to be instantiated"); //$NON-NLS-1$
    }

    static final String PLUGIN_ID = "org.eclipse.recommenders.stacktraces.rcp";

    static final String PREF_PAGE_ID = "org.eclipse.recommenders.stacktraces.rcp.preferencePages.errorReporting";

    /**
     * Specifying '-Dorg.eclipse.recommenders.stacktraces.rcp.skipReports=true' as vmarg in eclipse launch
     * configurations lets the log listener skip automated error reporting.
     */
    static final String SYSPROP_SKIP_REPORTS = PLUGIN_ID + ".skipReports";
    static final String SYSPROP_ECLIPSE_BUILD_ID = "eclipse.buildId";

    static final String PROP_NAME;
    static final String PROP_EMAIL;
    static final String PROP_ANONYMIZE_STACKTRACES;
    static final String PROP_ANONYMIZE_MESSAGES;
    static final String PROP_CONFIGURED;
    static final String PROP_SEND_ACTION;
    static final String PROP_REMEMBER_SEND_ACTION;
    static final String PROP_REMEMBER_SETTING_PERIOD_START;
    static final String PROP_SKIP_SIMILAR_ERRORS;
    static final String PROP_WHITELISTED_PLUGINS;
    static final String PROP_WHITELISTED_PACKAGES;
    static final String PROP_SERVER;

    static {
        ModelPackage pkg = ModelPackage.eINSTANCE;
        PROP_ANONYMIZE_STACKTRACES = pkg.getSettings_AnonymizeStrackTraceElements().getName();
        PROP_ANONYMIZE_MESSAGES = pkg.getSettings_AnonymizeMessages().getName();
        PROP_CONFIGURED = pkg.getSettings_Configured().getName();
        PROP_EMAIL = pkg.getSettings_Email().getName();
        PROP_NAME = pkg.getSettings_Name().getName();
        PROP_REMEMBER_SEND_ACTION = pkg.getSettings_RememberSendAction().getName();
        PROP_REMEMBER_SETTING_PERIOD_START = pkg.getSettings_RememberSendActionPeriodStart().getName();
        PROP_SEND_ACTION = pkg.getSettings_Action().getName();
        PROP_SERVER = pkg.getSettings_ServerUrl().getName();
        PROP_SKIP_SIMILAR_ERRORS = pkg.getSettings_SkipSimilarErrors().getName();
        PROP_WHITELISTED_PACKAGES = pkg.getSettings_WhitelistedPackages().getName();
        PROP_WHITELISTED_PLUGINS = pkg.getSettings_WhitelistedPluginIds().getName();
    }

    static final String HELP_URL = "https://dev.eclipse.org/recommenders/community/confess/";
    static final String FEEDBACK_FORM_URL = "https://docs.google.com/a/codetrails.com/forms/d/1wd9AzydLv_TMa7ZBXHO7zQIhZjZCJRNMed-6J4fVNsc/viewform";
    static final String SERVER_URL = getServerUrl();

    private static String getServerUrl() {
        return System.getProperty(PLUGIN_ID + "." + PROP_SERVER,
                "https://dev.eclipse.org/recommenders/community/confess/0.5/reports/");
    }

    // Cache
    public static final int PREVIOUS_ERROR_CACHE_MAXIMUM_SIZE = 30;
    public static final int PREVIOUS_ERROR_CACHE_EXPIRE_AFTER_ACCESS_MINUTES = 10;

    // Whitelist for sending
    public static final String WHITELISTED_PLUGINS = "org.eclipse.;org.apache.log4j;com.codetrails;";
    public static final String WHITELISTED_PACKAGES = "org.eclipse.;;;org.apache.;java.;javax.;javafx.;sun.;com.sun.;com.codetrails.;org.osgi.;com.google.;ch.qos.;org.slf4j.;";

    // Classes removed from top of stand-in-stacktrace
    public static final Set<String> STAND_IN_STACKTRACE_BLACKLIST = ImmutableSet.of("java.security.AccessController",
            "org.eclipse.core.internal.runtime.Log", "org.eclipse.core.internal.runtime.RuntimeLog",
            "org.eclipse.core.internal.runtime.PlatformLogWriter",
            "org.eclipse.osgi.internal.log.ExtendedLogReaderServiceFactory",
            "org.eclipse.osgi.internal.log.ExtendedLogReaderServiceFactory$3",
            "org.eclipse.osgi.internal.log.ExtendedLogServiceFactory",
            "org.eclipse.osgi.internal.log.ExtendedLogServiceImpl", "org.eclipse.osgi.internal.log.LoggerImpl",
            "org.eclipse.recommenders.internal.stacktraces.rcp.StandInStacktraceProvider",
            "org.eclipse.recommenders.internal.stacktraces.rcp.LogListener");

    // values for anonymization
    public static final String HIDDEN = "HIDDEN";
    public static final String SOURCE_BEGIN_MESSAGE = "----------------------------------- SOURCE BEGIN -------------------------------------";
    public static final String SOURCE_FILE_REMOVED = "source file contents removed";

    public static final String VERSION = "0.6";

}
