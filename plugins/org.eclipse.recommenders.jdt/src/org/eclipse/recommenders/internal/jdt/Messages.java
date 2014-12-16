/**
 * Copyright (c) 2014 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Sewe - initial API and implementation.
 */
package org.eclipse.recommenders.internal.jdt;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.recommenders.internal.jdt.messages"; //$NON-NLS-1$

    public static String LOG_ERROR_SNIPPET_REPLACE_LEADING_WHITESPACE_FAILED;
    public static String LOG_ERROR_CANNOT_FETCH_JAVA_PROJECTS;
    public static String LOG_ERROR_CANNOT_FETCH_PACKAGE_FRAGMENT_ROOTS;
    public static String LOG_ERROR_CANNOT_FETCH_PACKAGE_FRAGMENT;
    public static String LOG_ERROR_CANNOT_FETCH_COMPILATION_UNITS;
    public static String LOG_ERROR_CANNOT_FETCH_CLASS_FILES;
    public static String LOG_ERROR_CANNOT_FETCH_SOURCE_ATTACHMENT_PATH;
    public static String LOG_ERROR_CANNOT_FIND_TYPE_IN_PROJECT;
    public static String LOG_ERROR_CANNOT_DETERMINE_LOCATION;
    public static String LOG_ERROR_FAILED_TO_CREATE_TYPENAME;
    public static String LOG_ERROR_FAILED_TO_CREATE_METHODNAME;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
