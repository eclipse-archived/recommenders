/**
 * Copyright (c) 2014 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial implementation
 */
package org.eclipse.recommenders.internal.stacktraces.rcp;

import com.google.common.base.Optional;

public class ReportState {

    public static final String KEYWORD_NEEDINFO = "needinfo"; //$NON-NLS-1$
    public static final String[] EMPTY_STRINGS = new String[0];
    public static final String FIXED = "FIXED"; //$NON-NLS-1$
    public static final String ASSIGNED = "ASSIGNED"; //$NON-NLS-1$
    public static final String NOT_ECLIPSE = "NOT_ECLIPSE"; //$NON-NLS-1$
    public static final String INVALID = "INVALID"; //$NON-NLS-1$
    public static final String WONTFIX = "WONTFIX"; //$NON-NLS-1$
    public static final String WORKSFORME = "WORKSFORME"; //$NON-NLS-1$
    public static final String MOVED = "MOVED"; //$NON-NLS-1$
    public static final String DUPLICATE = "DUPLICATE"; //$NON-NLS-1$
    public static final String UNKNOWN = "UNKNOWN"; //$NON-NLS-1$
    public static final String CLOSED = "CLOSED"; //$NON-NLS-1$
    public static final String RESOLVED = "RESOLVED"; //$NON-NLS-1$
    public static final String NEW = "NEW"; //$NON-NLS-1$
    public static final String UNCONFIRMED = "UNCONFIRMED"; //$NON-NLS-1$

    private boolean created;
    private String bugId;
    private String bugUrl;
    private String status;
    private String resolved;
    private String information;
    private String[] keywords;

    public boolean isCreated() {
        return created;
    }

    public Optional<String> getBugId() {
        return Optional.fromNullable(bugId);
    }

    public Optional<String> getBugUrl() {
        return Optional.fromNullable(bugUrl);
    }

    public Optional<String> getInformation() {
        return Optional.fromNullable(information);
    }

    public Optional<String[]> getKeywords() {
        return Optional.fromNullable(keywords);
    }

    public Optional<String> getResolved() {
        return Optional.fromNullable(resolved);
    }

    public Optional<String> getStatus() {
        return Optional.fromNullable(status);
    }
}
