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
package org.eclipse.recommenders.internal.rcp;

import org.eclipse.osgi.util.NLS;

public final class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.recommenders.internal.rcp.messages"; //$NON-NLS-1$

    public static String PREFPAGE_DESCRIPTION_EMPTY;

    public static String JOB_INITIALIZE_PROJECTS;

    public static String LOG_ERROR_ACTIVE_PAGE_FINDER_TOO_EARLY;
    public static String LOG_ERROR_EXCEPTION_IN_SERVICE_HOOK;
    public static String LOG_ERROR;
    public static String LOG_WARNING;
    public static String LOG_INFO;
    public static String LOG_OK;
    public static String LOG_CANCEL;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
