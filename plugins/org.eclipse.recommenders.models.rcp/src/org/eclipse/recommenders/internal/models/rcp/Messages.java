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
package org.eclipse.recommenders.internal.models.rcp;

import org.eclipse.osgi.util.NLS;

public final class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.recommenders.internal.models.rcp.messages"; //$NON-NLS-1$




    public static String BUTTON_LABEL_DOWN;
    public static String BUTTON_LABEL_UP;

    public static String PREFPAGE_TITLE_ADVISORS;
    public static String PREFPAGE_TITLE_MODEL_REPOSITORIES;

    public static String PREFPAGE_DESCRIPTION_ADVISORS;
    public static String PREFPAGE_DESCRIPTION_MODEL_REPOSITORIES;

    public static String FIELD_LABEL_ADVISORS;
    public static String FIELD_LABEL_ENABLE_AUTO_DOWNLOAD;
    public static String FIELD_LABEL_REPOSITORY_URI;
    public static String FIELD_LABEL_REPOSITORY_URIS;

    public static String MENUITEM_ADD_REPOSITORY;
    public static String MENUITEM_CLEAR_CACHE;
    public static String MENUITEM_DELETE_MODELS;
    public static String MENUITEM_DOWNLOAD_MODELS;
    public static String MENUITEM_REMOVE_REPOSITORY;
    public static String MENUITEM_SHOW;
    public static String MENUITEM_SHOW_ALL;
    public static String MENUITEM_SHOW_CONFLICTING_COORDINATES_ONLY;
    public static String MENUITEM_SHOW_MANUALLY_ASSIGNED_COORDINATES_ONLY;
    public static String MENUITEM_SHOW_MISSING_COORDINATES_ONLY;

    public static String TOOLBAR_TOOLTIP_ADD_REPOSITORY;
    public static String TOOLBAR_TOOLTIP_COLLAPSE_ALL;
    public static String TOOLBAR_TOOLTIP_EXPAND_ALL;
    public static String TOOLBAR_TOOLTIP_REFRESH;

    public static String DIALOG_RESOLVING_DEPENDENCIES;
    public static String DIALOG_TITLE_ADD_MODEL_REPOSITORY;
    public static String DIALOG_TITLE_INVALID_COORDINATE_FORMAT;
    public static String DIALOG_TITLE_INDEX_DOWNLOAD_FAILURE;
    public static String DIALOG_TITLE_SELECT_PROJECT_COORDINATE;
    public static String DIALOG_TOGGLE_IGNORE_DOWNLOAD_FAILURES;

    public static String DIALOG_MESSAGE_URI_ALREADY_ADDED;
    public static String DIALOG_MESSAGE_INVALID_COORDINATE_FORMAT;
    public static String DIALOG_MESSAGE_INVALID_URI;
    public static String DIALOG_MESSAGE_INDEX_DOWNLOAD_FAILURE;
    public static String DIALOG_MESSAGE_NOT_ABSOLUTE_URI;
    public static String DIALOG_MESSAGE_UNSUPPORTED_PROTOCOL;
    public static String DIALOG_MESSAGE_FILE_A_BUG;

    public static String SEARCH_PLACEHOLDER_FILTER_TEXT;

    public static String COLUMN_LABEL_COORDINATE;
    public static String COLUMN_LABEL_DEPENDENCY;
    public static String COLUMN_LABEL_LOCATION;
    public static String COLUMN_LABEL_PROJECT_COORDINATE;
    public static String COLUMN_LABEL_REPOSITORY;

    public static String TABLE_CELL_SUFFIX_KNOWN_COORDINATES;
    public static String TABLE_CELL_TOOLTIP_AVAILABLE_LOCALLY;
    public static String TABLE_CELL_TOOLTIP_AVAILABLE_REMOTELY;
    public static String TABLE_CELL_TOOLTIP_HINTS;
    public static String TABLE_CELL_TOOLTIP_KEY_VALUE;
    public static String TABLE_CELL_TOOLTIP_LOCATION;
    public static String TABLE_CELL_TOOLTIP_TYPE;
    public static String TABLE_CELL_TOOLTIP_UNAVAILABLE;
    public static String TABLE_CELL_TOOLTIP_UNKNOWN_COORDINATE;

    public static String JOB_DELETING_MODEL_CACHE;
    public static String JOB_DOWNLOADING_MODELS;
    public static String JOB_DOWNLOAD_TRANSFERRED_SIZE;
    public static String JOB_DOWNLOAD_TRANSFERRED_TOTAL_SIZE;
    public static String JOB_REFRESHING_CACHED_COORDINATES;
    public static String JOB_REFRESHING_DEPENDENCY_OVERVIEW_VIEW;
    public static String JOB_REFRESHING_MODEL_REPOSITORIES_VIEW;
    public static String JOB_REFRESHING_PROJECT_COORDINATES_VIEW;
    public static String JOB_RESOLVING_DEPENDENCIES;
    public static String JOB_RESOLVING_MODEL;

    public static String TASK_ASSIGNING_PROJECT_COORDINATES;
    public static String TASK_ASSIGNING_PROJECT_COORDINATE_TO;
    public static String TASK_REFRESHING;
    public static String TASK_RESOLVING_MODEL;

    public static String LOG_ERROR_CLOSING_MODEL_INDEX_SERVICE;
    public static String LOG_ERROR_ADVISOR_INSTANTIATION;
    public static String LOG_ERROR_BIND_FILE_NAME;
    public static String LOG_ERROR_CREATE_EXECUTABLE_EXTENSION_FAILED;
    public static String LOG_ERROR_FAILED_TO_DELETE_MODEL_CACHE;
    public static String LOG_ERROR_MODEL_RESOLUTION_FAILURE;
    public static String LOG_ERROR_SAVE_PREFERENCES;
    public static String LOG_ERROR_SERVICE_NOT_RUNNING;

    public static String LOG_INFO_NO_MODEL_RESOLVED;

    public static String LIST_SEPARATOR;

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
