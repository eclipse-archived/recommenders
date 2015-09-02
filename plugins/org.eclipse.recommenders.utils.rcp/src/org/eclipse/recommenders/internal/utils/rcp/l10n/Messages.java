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
package org.eclipse.recommenders.internal.utils.rcp.l10n;

import org.eclipse.osgi.util.NLS;

public final class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.recommenders.internal.utils.rcp.l10n.messages"; //$NON-NLS-1$

    public static String PREFPAGE_LINKS_DESCRIPTION;

    public static String LOG_ERROR_FAILED_TO_EXECUTE_COMMAND;
    public static String LOG_ERROR_FAILED_TO_OPEN_BROWSER;
    public static String LOG_ERROR_FAILED_TO_READ_EXTENSION_ATTRIBUTE;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
