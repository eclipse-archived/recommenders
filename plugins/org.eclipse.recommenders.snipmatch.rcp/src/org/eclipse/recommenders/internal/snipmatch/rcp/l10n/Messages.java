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
package org.eclipse.recommenders.internal.snipmatch.rcp.l10n;

import org.eclipse.osgi.util.NLS;

public final class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.recommenders.internal.snipmatch.rcp.l10n.messages"; //$NON-NLS-1$

    public static String COMPLETION_ENGINE_NO_SNIPPETS_FOUND;
    public static String COMPLETION_ENGINE_REPOSITORY_MATCHES;

    public static String CONFIRM_DIALOG_DELETE_REPOSITORY_MESSAGE;
    public static String CONFIRM_DIALOG_DELETE_REPOSITORY_TITLE;
    public static String CONFIRM_DIALOG_DELETE_REPOSITORY_TOGGLE_MESSAGE;
    public static String CONFIRM_DIALOG_DELETE_SNIPPET_MESSAGE;
    public static String CONFIRM_DIALOG_DELETE_SNIPPET_TITLE;

    public static String PROPOSAL_INFO_IDENTIFYING_PROJECT_DEPENDENCIES;

    public static String PROPOSAL_LABEL_IDENTIFYING_PROJECT_DEPENDENCIES;

    public static String DIALOG_MESSAGE_ENTER_NEW_EXTRA_SEARCH_TERM;
    public static String DIALOG_MESSAGE_ENTER_NEW_FILENAME_RESTRICTION;
    public static String DIALOG_MESSAGE_ENTER_NEW_TAG;
    public static String DIALOG_MESSAGE_ERROR_SNIPPET_SOURCE_INVALID;
    public static String DIALOG_MESSAGE_INVALID_SNIPPET_NAME;
    public static String DIALOG_MESSAGE_NO_MESSAGE_EXCEPTION;
    public static String DIALOG_MESSAGE_INVALID_SNIPPET_LOCATION;
    public static String DIALOG_MESSAGE_SAVE_SNIPPET_WITH_MODIFIED_CODE;
    public static String DIALOG_MESSAGE_BRANCH_CHECKOUT_FAILURE;
    public static String DIALOG_MESSAGE_BRANCH_CHECKOUT_FAILURE_LINK;
    public static String DIALOG_MESSAGE_NO_FORMAT_BRANCH_FAILURE;
    public static String DIALOG_MESSAGE_SELECT_DEPENDENCY;
    public static String DIALOG_MESSAGE_NO_GIT_CHANGES_IN_SELECTION;
    public static String DIALOG_MESSAGE_GIT_PUSH_SUCCESSFUL;

    public static String DIALOG_OPTION_CANCEL;
    public static String DIALOG_OPTION_SAVE;
    public static String DIALOG_OPTION_SAVE_AS_NEW;

    public static String DIALOG_TITLE_ENTER_NEW_EXTRA_SEARCH_TERM;
    public static String DIALOG_TITLE_ENTER_NEW_FILENAME_RESTRICTION;
    public static String DIALOG_TITLE_ENTER_NEW_TAG;
    public static String DIALOG_TITLE_ERROR_SNIPPET_SOURCE_INVALID;
    public static String DIALOG_TITLE_INAVLID_SNIPPET_NAME;
    public static String DIALOG_TITLE_INVALID_SNIPPET_LOCATION;
    public static String DIALOG_TITLE_SAVE_SNIPPET;
    public static String DIALOG_TITLE_BRANCH_CHECKOUT_FAILURE;
    public static String DIALOG_TITLE_SELECT_DEPENDENCY;
    public static String DIALOG_TITLE_SELECTION_NOT_SHAREABLE;
    public static String DIALOG_TITLE_GIT_PUSH_SUCCESSFUL;

    public static String DIALOG_VALIDATOR_EXTRA_SEARCH_TERM_ALREADY_ADDED;
    public static String DIALOG_VALIDATOR_FILENAME_RESTRICTION_ALREADY_ADDED;
    public static String DIALOG_VALIDATOR_FILENAME_RESTRICTION_CONTAINS_ILLEGAL_CHARACTER;
    public static String DIALOG_VALIDATOR_TAG_ALREADY_ADDED;

    public static String EDITOR_BUTTON_ADD;
    public static String EDITOR_BUTTON_REMOVE;

    public static String EDITOR_EXTENSIONS_HEADER_EXT_LINK;

    public static String EDITOR_LABEL_SNIPPET_EXTRA_SEARCH_TERMS;
    public static String EDITOR_LABEL_SNIPPET_DEPENDENCIES;
    public static String EDITOR_LABEL_SNIPPET_DESCRIPTION;
    public static String EDITOR_LABEL_SNIPPET_FILENAME_RESTRICTIONS;
    public static String EDITOR_LABEL_SNIPPET_LOCATION;
    public static String EDITOR_LABEL_SNIPPET_NAME;
    public static String EDITOR_LABEL_SNIPPET_TAG;
    public static String EDITOR_LABEL_SNIPPET_UUID;

    public static String EDITOR_TEXT_MESSAGE_SNIPPET_DESCRIPTION;
    public static String EDITOR_TEXT_MESSAGE_SNIPPET_NAME;

    public static String SNIPMATCH_LOCATION_FILE;
    public static String SNIPMATCH_LOCATION_JAVA_FILE;
    public static String SNIPMATCH_LOCATION_JAVA;
    public static String SNIPMATCH_LOCATION_JAVA_STATEMENTS;
    public static String SNIPMATCH_LOCATION_JAVA_MEMBERS;
    public static String SNIPMATCH_LOCATION_JAVADOC;

    public static String EDITOR_TITLE_METADATA;
    public static String EDITOR_TITLE_RAW_SOURCE;
    public static String EDITOR_TOOLBAR_ITEM_HELP;

    public static String EDITOR_DESCRIPTION_DEPENDENCIES;
    public static String EDITOR_DESCRIPTION_EXTRA_SEARCH_TERMS;
    public static String EDITOR_DESCRIPTION_FILENAME_RESTRICTIONS;
    public static String EDITOR_DESCRIPTION_LOCATION;
    public static String EDITOR_DESCRIPTION_TAGS;

    public static String LOG_ERROR_CREATING_SNIPPET_PROPOSAL_FAILED;
    public static String LOG_ERROR_SNIPPET_COULD_NOT_BE_EVALUATED;
    public static String LOG_ERROR_CANNOT_COMPUTE_LOCATION;
    public static String LOG_ERROR_FAILED_TO_BIND_FILE;
    public static String LOG_ERROR_FAILED_TO_EXECUTE_COMMAND;
    public static String LOG_ERROR_FAILED_TO_DELETE_GIT_SNIPPET_REPOSITORY_ON_DISK;
    public static String LOG_ERROR_FAILED_TO_JOIN_OPEN_JOB;
    public static String LOG_ERROR_FAILED_TO_LOAD_EDITOR_PAGES;
    public static String LOG_ERROR_FAILED_TO_OPEN_EDITOR;
    public static String LOG_ERROR_FAILED_TO_OPEN_GIT_SNIPPET_REPOSITORY;
    public static String LOG_ERROR_FAILED_TO_OPEN_WIZARD_WITH_ZERO_HEIGHT_LIST;
    public static String LOG_ERROR_FAILED_TO_READ_EXTENSION_POINT;
    public static String LOG_ERROR_FAILED_TO_RELOAD_REPOSITORIES;
    public static String LOG_ERROR_FAILED_TO_STORE_SNIPPET;

    public static String ERROR_COMMIT_FAILED;
    public static String ERROR_FAILURE_TO_CLONE_REPOSITORY;
    public static String ERROR_NO_FORMAT_BRANCH;
    public static String ERROR_SNIPPET_NAME_CANNOT_BE_EMPTY;
    public static String ERROR_SNIPPET_LOCATION_CANNOT_BE_EMPTY;
    public static String ERROR_NO_EDITABLE_REPO_FOUND;
    public static String ERROR_NO_EDITABLE_REPO_FOUND_HINT;
    public static String ERROR_REPOSITORY_NOT_OPEN_YET;
    public static String ERROR_EXCEPTION_WHILE_PUSHING_SNIPPETS_TO_REMOTE_GIT_REPO;
    public static String ERROR_FAILURE_TO_PUSH_SNIPPETS_TO_REMOTE_GIT_REPO;
    public static String ERROR_UNABLE_TO_DETERMINE_SOURCE_VIEWER;

    public static String SEARCH_PLACEHOLDER_SEARCH_TEXT;
    public static String SEARCH_DISPLAY_STRING;

    public static String SELECT_REPOSITORY_DIALOG_MESSAGE;
    public static String SELECT_REPOSITORY_DIALOG_TITLE;
    public static String SNIPPETS_VIEW_MENUITEM_ADD_REPOSITORY;
    public static String SNIPPETS_VIEW_MENUITEM_ADD_SNIPPET;
    public static String SNIPPETS_VIEW_MENUITEM_ADD_SNIPPET_TO_REPOSITORY;

    public static String SNIPPETS_VIEW_MENUITEM_DISABLE_REPOSITORY;
    public static String SNIPPETS_VIEW_MENUITEM_EDIT_REPOSITORY;
    public static String SNIPPETS_VIEW_MENUITEM_EDIT_SNIPPET;

    public static String SNIPPETS_VIEW_MENUITEM_ENABLE_REPOSITORY;
    public static String SNIPPETS_VIEW_MENUITEM_REFRESH;
    public static String SNIPPETS_VIEW_MENUITEM_REMOVE_REPOSITORY;
    public static String SNIPPETS_VIEW_MENUITEM_REMOVE_SNIPPET;
    public static String SNIPPETS_VIEW_MENUITEM_SHARE_SNIPPET;

    public static String JOB_NAME_IDENTIFYING_PROJECT_DEPENDENCIES;
    public static String JOB_NAME_OPENING_SNIPPET_REPOSITORY;
    public static String JOB_NAME_RECONNECTING_SNIPPET_REPOSITORY;
    public static String JOB_NAME_SEARCHING_SNIPPET_REPOSITORIES;
    public static String JOB_NAME_REFRESHING_SNIPPETS_VIEW;
    public static String JOB_NAME_PUSHING_SNIPPETS_TO_REMOTE_GIT_REPO;

    public static String JOB_GROUP_UPDATING_SNIPPETS_VIEW;
    public static String JOB_RESETTING_GIT_REPOSITORY;

    public static String PREFPAGE_BUTTON_EDIT;
    public static String PREFPAGE_BUTTON_NEW;
    public static String PREFPAGE_BUTTON_REMOVE;
    public static String PREFPAGE_DESCRIPTION;
    public static String PREFPAGE_LABEL_REMOTE_SNIPPETS_REPOSITORY;

    public static String WARNING_CANNOT_APPLY_SNIPPET;
    public static String WARNING_REPOSITION_CURSOR;
    public static String WARNING_FAILURE_TO_UPDATE_REPOSITORY;
    public static String WARNING_FAILURE_TO_CHECKOUT_CURRENT_BRANCH;

    public static String WIZARD_GIT_REPOSITORY_ADD_DESCRIPTION;
    public static String WIZARD_GIT_REPOSITORY_EDIT_DESCRIPTION;
    public static String WIZARD_GIT_REPOSITORY_ERROR_ABSOLUTE_URL_REQUIRED;
    public static String WIZARD_GIT_REPOSITORY_ERROR_EMPTY_BRANCH_PREFIX;
    public static String WIZARD_GIT_REPOSITORY_ERROR_EMPTY_FETCH_URL;
    public static String WIZARD_GIT_REPOSITORY_ERROR_EMPTY_NAME;
    public static String WIZARD_GIT_REPOSITORY_ERROR_EMPTY_PUSH_URL;
    public static String WIZARD_GIT_REPOSITORY_ERROR_INVALID_URL;
    public static String WIZARD_GIT_REPOSITORY_ERROR_INVALID_BRANCH_PREFIX_FORMAT;
    public static String WIZARD_GIT_REPOSITORY_ERROR_URL_PROTOCOL_UNSUPPORTED;
    public static String WIZARD_GIT_REPOSITORY_GROUP_FETCH_SETTINGS;
    public static String WIZARD_GIT_REPOSITORY_GROUP_PUSH_SETTINGS;
    public static String WIZARD_GIT_REPOSITORY_LABEL_FETCH_URL;
    public static String WIZARD_GIT_REPOSITORY_LABEL_NAME;
    public static String WIZARD_GIT_REPOSITORY_LABEL_PUSH_URL;
    public static String WIZARD_GIT_REPOSITORY_LABEL_PUSH_BRANCH_PREFIX;
    public static String WIZARD_GIT_REPOSITORY_PAGE_NAME;
    public static String WIZARD_GIT_REPOSITORY_PUSH_SETTINGS_DESCRIPTION;
    public static String WIZARD_GIT_REPOSITORY_TITLE;
    public static String WIZARD_GIT_REPOSITORY_WINDOW_TITLE;

    public static String LOG_ERROR_CREATING_INDEX_HEAD_DIFF;
    public static String LOG_ERROR_DEFAULT_REPO_CONFIGURATION_WITHOUT_ID;
    public static String LOG_ERROR_LOADING_DEFAULT_REPO_CONFIGURATION;
    public static String LOG_ERROR_LOADING_REPO_CONFIGURATION;
    public static String LOG_ERROR_LOADING_DEFAULT_GIT_REPO_CONFIGURATION;
    public static String LOG_ERROR_SERVICE_NOT_RUNNING;
    public static String LOG_ERROR_STORING_DEFAULT_REPO_CONFIGURATION;
    public static String LOG_ERROR_STORING_DISABLED_REPOSITORY_CONFIGURATIONS;
    public static String LOG_WARNING_DEFAULT_GIT_REPO_URL_DUPLICATE;

    public static String WIZARD_TYPE_SELECTION_DESCRIPTION;
    public static String WIZARD_TYPE_SELECTION_LABEL_WIZARDS;
    public static String WIZARD_TYPE_SELECTION_NAME;
    public static String WIZARD_TYPE_SELECTION_TITLE;
    public static String WIZARD_TYPE_SELECTION_WINDOW_TITLE;

    public static String TABLE_CELL_SUFFIX_SNIPPETS;
    public static String TABLE_COLUMN_TITLE_SNIPPETS;
    public static String TABLE_REPOSITORY_DISABLED;

    public static String TOOLBAR_TOOLTIP_COLLAPSE_ALL;
    public static String TOOLBAR_TOOLTIP_EXPAND_ALL;

    public static String MONITOR_CALCULATING_DIFF;
    public static String MONITOR_SEARCH_SNIPPETS;
    public static String MONITOR_REFRESHING_TABLE;

    public static String LIST_SEPARATOR;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
