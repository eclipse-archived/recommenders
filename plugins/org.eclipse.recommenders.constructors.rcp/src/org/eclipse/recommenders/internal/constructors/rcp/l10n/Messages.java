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
package org.eclipse.recommenders.internal.constructors.rcp.l10n;

import org.eclipse.osgi.util.NLS;

public final class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.recommenders.internal.constructors.rcp.l10n.messages"; //$NON-NLS-1$

    public static String PREFPAGE_DESCRIPTION_CONSTRUCTORS;

    public static String FIELD_LABEL_DECORATE_PROPOSAL_ICON;
    public static String FIELD_LABEL_DECORATE_PROPOSAL_TEXT;
    public static String FIELD_LABEL_MAX_NUMBER_OF_PROPOSALS;
    public static String FIELD_LABEL_MIN_PROPOSAL_PERCENTAGE;
    public static String FIELD_LABEL_UPDATE_PROPOSAL_RELEVANCE;

    public static String PROPOSAL_LABEL_PROMILLE;
    public static String PROPOSAL_LABEL_PERCENTAGE;

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
