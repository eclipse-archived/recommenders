/**
 * Copyright (c) 2010, 2012 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Patrick Gottschaemmer, Olav Lenz - initial API and implementation.
 */
package org.eclipse.recommenders.completion.rcp.calls.l10n;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.recommenders.completion.rcp.calls.l10n.messages"; //$NON-NLS-1$
    public static String PREFPAGE_DESCRIPTION;
    public static String PREFPAGE_ENABLE_CALL_COMPLETION;
    public static String PREFPAGE_MAX_NUMBER_OF_PROPOSALS;
    public static String PREFPAGE_MIN_PROBABILITY_OF_PROPOSAL;
    public static String PREFPAGE_TABLE_DESCRIPTION;
    public static String PREFPAGE_TABLE_COLUMN_FILE;
    public static String PREFPAGE_MODLE_NOT_AVAILABLE;
    public static String PREFPAGE_MODEL_AVAILABLE;
    public static String PREFPAGE_DEPENDENCY_DETAILS_UNKNOWN;
    public static String PREFPAGE_DEPENDENCY_DETAILS_KNOWN;
    public static String PREFPAGE_PACKAGE_ROOT_INFO;
    public static String PREFPAGE_PACKAGE_ROOT_NAME;
    public static String PREFPAGE_PACKAGE_ROOT_VERSION;
    public static String PREFPAGE_PACKAGE_ROOT_FINGERPRINT;
    public static String PREFPAGE_MODEL_INFO;
    public static String PREFPAGE_MODEL_CCORDINATE;
    public static String PREFPAGE_MODEL_RESOLUTION_STATUS;

    public static String EXTDOC_RECOMMENDATIONS_ARE_NOT_MADE;
    public static String EXTDOC_RECOMMENDATIONS_ARE_MADE;
    public static String EXTDOC_PECOMMENDATION_PERCENTAGE;
    public static String EXTDOC_DEFINED_BY;
    public static String EXTDOC_UNDEFINED;
    public static String EXTDOC_OBSERVED;
    public static String EXTDOC_CALL;
    public static String EXTDOC_PROPOSAL_COMPUTED_UNTRAINED;
    public static String EXTDOC_PROPOSAL_COMPUTED;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
