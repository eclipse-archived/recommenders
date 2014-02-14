/**
 * Copyright (c) 2010, 2012 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Olav Lenz - initial API and implementation.
 */
package org.eclipse.recommenders.internal.calls.rcp;

import org.eclipse.osgi.util.NLS;

public final class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.recommenders.internal.calls.rcp.messages"; //$NON-NLS-1$

    public static String PROPOSAL_LABEL_USED;
    public static String PROPOSAL_LABEL_PERCENTAGE;

    public static String PREFPAGE_DESCRIPTION_CALLS;

    public static String FIELD_LABEL_DECORATE_PROPOSAL_ICON;
    public static String FIELD_LABEL_DECORATE_PROPOSAL_TEXT;
    public static String FIELD_LABEL_HIGHLIGHT_USED_PROPOSALS;
    public static String FIELD_LABEL_MAX_NUMBER_OF_PROPOSALS;
    public static String FIELD_LABEL_MIN_PROPOSAL_PROBABILITY;
    public static String FIELD_LABEL_UPDATE_PROPOSAL_RELEVANCE;

    public static String PROVIDER_INTRO_NO_RECOMMENDATIONS;
    public static String PROVIDER_INTRO_RECOMMENDATIONS;

    public static String PROVIDER_INFO_LOCAL_VAR_CONTEXT;
    public static String PROVIDER_INFO_UNTRAINED_CONTEXT;

    public static String TABLE_CELL_DEFINITION_UNTRAINED;
    public static String TABLE_CELL_RELATION_CALL;
    public static String TABLE_CELL_RELATION_DEFINED_BY;
    public static String TABLE_CELL_RELATION_OBSERVED;
    public static String TABLE_CELL_SUFFIX_PERCENTAGE;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
