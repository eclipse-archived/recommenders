/**
 * Copyright (c) 2010, 2012 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Olav Lenz - initial API and implementation.
 */
package org.eclipse.recommenders.internal.apidocs.rcp.l10n;

import org.eclipse.osgi.util.NLS;

public final class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.recommenders.internal.apidocs.rcp.l10n.messages"; //$NON-NLS-1$

    public static String PROVIDER_INTRO_IMPLEMENTOR_SELFCALL_STATISTIC;
    public static String PROVIDER_INTRO_JAVADOC_NOT_FOUND;
    public static String PROVIDER_INTRO_NO_STATIC_HOOKS_FOUND;
    public static String PROVIDER_INTRO_OVERRIDE_PATTERNS;
    public static String PROVIDER_INTRO_OVERRIDE_STATISTICS;
    public static String PROVIDER_INTRO_SUBCLASS_SELFCALL_STATISTICS;

    public static String TABLE_HEADER_OVERRIDE_PATTERN;

    public static String TABLE_CELL_FREQUENCY_ALWAYS;
    public static String TABLE_CELL_FREQUENCY_USUALLY;
    public static String TABLE_CELL_FREQUENCY_SOMETIMES;
    public static String TABLE_CELL_FREQUENCY_OCCASIONALLY;
    public static String TABLE_CELL_FREQUENCY_RARELY;
    public static String TABLE_CELL_RELATION_CALL;
    public static String TABLE_CELL_RELATION_OVERRIDE;
    public static String TABLE_CELL_SUFFIX_FREQUENCIES;
    public static String TABLE_CELL_SUFFIX_PERCENTAGE;

    public static String JOB_UPDATING_APIDOCS;

    public static String LOG_ERROR_FAILED_TO_DETERMINE_STATIC_MEMBERS;
    public static String LOG_ERROR_DURING_JAVADOC_SELECTION;
    public static String LOG_ERROR_FAILED_TO_INSTANTIATE_PROVIDER;
    public static String LOG_ERROR_CLEAR_PREFERENCES;
    public static String LOG_ERROR_READ_PREFERENCES;
    public static String LOG_ERROR_SAVE_PREFERENCES;

    public static String LOG_WARNING_NO_SUCH_ENTRY;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
