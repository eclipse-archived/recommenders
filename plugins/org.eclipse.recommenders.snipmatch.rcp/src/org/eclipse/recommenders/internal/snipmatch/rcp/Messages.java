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
package org.eclipse.recommenders.internal.snipmatch.rcp;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.recommenders.internal.snipmatch.rcp.messages"; //$NON-NLS-1$
    public static String COMPLETION_ENGINE_NO_SNIPPETS_FOUND;

    public static String DIALOG_MESSAGE_ENTER_NEW_EXTRA_SEARCH_TERM;
    public static String DIALOG_MESSAGE_ENTER_NEW_TAG;
    public static String DIALOG_MESSAGE_INVALID_SNIPPET_NAME;
    public static String DIALOG_MESSAGE_NO_REPOSITORY_AVAILABLE;
    public static String DIALOG_MESSAGE_SAVE_SNIPPET_WITH_MODIFIED_CODE;

    public static String DIALOG_OPTION_CANCEL;
    public static String DIALOG_OPTION_SAVE;
    public static String DIALOG_OPTION_SAVE_AS_NEW;

    public static String DIALOG_TITLE_ENTER_NEW_EXTRA_SEARCH_TERM;
    public static String DIALOG_TITLE_ENTER_NEW_TAG;
    public static String DIALOG_TITLE_ERROR_WHILE_STORING_SNIPPET;
    public static String DIALOG_TITLE_INAVLID_SNIPPET_NAME;
    public static String DIALOG_TITLE_SAVE_SNIPPET;

    public static String DIALOG_VALIDATOR_EXTRA_SEARCH_TERM_ALREADY_ADDED;
    public static String DIALOG_VALIDATOR_TAG_ALREADY_ADDED;

    public static String EDITOR_BUTTON_ADD_EXTRASEARCH_TERM;
    public static String EDITOR_BUTTON_ADD_TAGS;
    public static String EDITOR_BUTTON_REMOVE_EXTRA_SEARCH_TERM;
    public static String EDITOR_BUTTON_REMOVE_TAGS;

    public static String EDITOR_LABEL_SNIPPET_DESCRIPTION;
    public static String EDITOR_LABEL_SNIPPET_NAME;
    public static String EDITOR_LABEL_SNIPPET_UUID;
    public static String EDITOR_LABEL_SNIPPETS_EXTRA_SEARCH_TERMS;
    public static String EDITOR_LABEL_SNIPPETS_TAG;
    public static String EDITOR_LABEL_TITLE_METADATA;

    public static String EDITOR_PAGE_NAME_METADATA;
    public static String EDITOR_PAGE_NAME_SOURCE;

    public static String ERROR_CREATING_SNIPPET_PROPOSAL_FAILED;
    public static String ERROR_FAILURE_TO_CLONE_REPOSITORY;
    public static String ERROR_SNIPPET_NAME_CAN_NOT_BE_EMPTY;
    public static String ERROR_WHILE_OPENING_EDITOR;
    public static String ERROR_NO_EDITABLE_REPO_FOUND;
    public static String ERROR_NO_EDITABLE_REPO_FOUND_HINT;

    public static String SEARCH_PLACEHOLDER_SEARCH_TEXT;
    public static String SEARCH_DISPLAY_STRING;

    public static String SNIPPETS_VIEW_BUTTON_ADD;
    public static String SNIPPETS_VIEW_BUTTON_EDIT;
    public static String SNIPPETS_VIEW_BUTTON_RECONNECT;
    public static String SNIPPETS_VIEW_BUTTON_REMOVE;

    public static String JOB_OPENING_SNIPPET_REPOSITORY;
    public static String JOB_RECONNECTING_SNIPPET_REPOSITORY;
    public static String JOB_REFRESHING_SNIPPETS_VIEW;

    public static String PREFPAGE_LABEL_SNIPPETS_REPO_FETCH_URL;
    public static String PREFPAGE_LABEL_SNIPPETS_PUSH_SETTINGS_DESCRIPTION;
    public static String PREFPAGE_LABEL_SNIPPETS_REPO_PUSH_URL;
    public static String PREFPAGE_LABEL_SNIPPETS_REPO_PUSH_BRANCH;
    public static String PREFPAGE_DESCRIPTION;
    public static String PREFPAGE_ERROR_INVALID_BRANCH_PREFIX_FORMAT;

    public static String WARNING_CANNOT_APPLY_SNIPPET;
    public static String WARNING_REPOSITION_CURSOR;
    public static String WARNING_FAILURE_TO_UPDATE_REPOSITORY;

    public static String GROUP_FETCH_SETTINGS;
    public static String GROUP_PUSH_SETTINGS;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
