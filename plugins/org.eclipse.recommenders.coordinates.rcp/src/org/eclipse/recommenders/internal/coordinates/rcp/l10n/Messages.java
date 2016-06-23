/**
 * Copyright (c) 2010, 2012 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Olav Lenz - externalize Strings.
 */
package org.eclipse.recommenders.internal.coordinates.rcp.l10n;

import org.eclipse.osgi.util.NLS;

public final class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.recommenders.internal.coordinates.rcp.l10n.messages"; //$NON-NLS-1$

    public static String BUTTON_LABEL_DOWN;
    public static String BUTTON_LABEL_UP;

    public static String PREFPAGE_TITLE_ADVISORS;

    public static String PREFPAGE_DESCRIPTION_ADVISORS;

    public static String FIELD_LABEL_ADVISORS;

    public static String MENUITEM_CLEAR_CACHE;
    public static String MENUITEM_SHOW;
    public static String MENUITEM_SHOW_ALL;
    public static String MENUITEM_SHOW_CONFLICTING_COORDINATES_ONLY;
    public static String MENUITEM_SHOW_MANUALLY_ASSIGNED_COORDINATES_ONLY;
    public static String MENUITEM_SHOW_MISSING_COORDINATES_ONLY;

    public static String TOOLBAR_TOOLTIP_REFRESH;

    public static String DIALOG_TITLE_INVALID_COORDINATE_FORMAT;

    public static String DIALOG_MESSAGE_INVALID_COORDINATE_FORMAT;

    public static String COLUMN_LABEL_COORDINATE;
    public static String COLUMN_LABEL_LOCATION;

    public static String TABLE_CELL_TOOLTIP_HINTS;
    public static String TABLE_CELL_TOOLTIP_KEY_VALUE;
    public static String TABLE_CELL_TOOLTIP_LOCATION;
    public static String TABLE_CELL_TOOLTIP_TYPE;
    public static String TABLE_CELL_TOOLTIP_UNKNOWN_COORDINATE;

    public static String JOB_REFRESHING_PROJECT_COORDINATES_VIEW;
    public static String JOB_RESOLVING_DEPENDENCIES;

    public static String TASK_ASSIGNING_PROJECT_COORDINATES;
    public static String TASK_ASSIGNING_PROJECT_COORDINATE_TO;

    public static String LOG_ERROR_ADVISOR_INSTANTIATION_FAILED;
    public static String LOG_ERROR_BIND_FILE_NAME;
    public static String LOG_ERROR_FAILED_TO_CREATE_ADVISOR;
    public static String LOG_ERROR_FAILED_TO_DETECT_PROJECT_JRE;
    public static String LOG_ERROR_FAILED_TO_READ_CACHED_COORDINATES;
    public static String LOG_ERROR_FAILED_TO_READ_MANUAL_MAPPINGS;
    public static String LOG_ERROR_FAILED_TO_REGISTER_PROJECT_DEPENDENCIES;
    public static String LOG_ERROR_FAILED_TO_WRITE_CACHED_COORDINATES;
    public static String LOG_ERROR_FAILED_TO_WRITE_MANUAL_MAPPINGS;
    public static String LOG_ERROR_IN_ADVISOR_SERVICE_SUGGEST;

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
