/**
 * Copyright (c) 2010, 2014 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Sewe - initial API and implementation.
 */
package org.eclipse.recommenders.internal.subwords.rcp.l10n;

import org.eclipse.osgi.util.NLS;

public final class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.recommenders.internal.subwords.rcp.l10n.messages"; //$NON-NLS-1$

    public static String PREFPAGE_TITLE_SUBWORDS;
    public static String PREFPAGE_DESCRIPTION_SUBWORDS_PREFIX_LENGTH;
    public static String PREFPAGE_LABEL_PREFIX_LENGTH;
    public static String PREFPAGE_TOOLTIP_PREFIX_LENGTH;

    public static String LOG_ERROR_EXCEPTION_DURING_CODE_COMPLETION;
    public static String LOG_ERROR_UNEXPECTED_FALLTHROUGH;
    public static String LOG_ERROR_COMPLETION_CONTEXT_NOT_COLLECTED;

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
