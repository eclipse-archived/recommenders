/**
 * Copyright (c) 2015 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Simon Laffoy - initial API and implementation.
 */
package org.eclipse.recommenders.internal.types.rcp.l10n;

import org.eclipse.osgi.util.NLS;

public final class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.recommenders.internal.types.rcp.l10n.messages"; //$NON-NLS-1$

    public static String LOG_ERROR_ACCESSING_SEARCHINDEX_FAILED;
    public static String LOG_ERROR_CLOSING_PROJECT_TYPES_INDEXES;

    public static String LOG_INFO_REINDEXING_REQUIRED;

    public static String JOB_NAME_INDEXING;

    public static String MONITOR_NAME_INDEXING;

    public static String PROPOSAL_LABEL_ENABLE_TYPES_COMPLETION;

    public static String PROPOSAL_TOOLTIP_ENABLE_TYPES_COMPLETION;

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
