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
package org.eclipse.recommenders.completion.rcp.chain.l10n;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.recommenders.completion.rcp.chain.l10n.messages"; //$NON-NLS-1$
    public static String COMPLETION_TEMPLATE_ELEMENTS;
    public static String PREFPAGE_DESCRIPTION;
    public static String PREFPAGE_IGNORE_CONSEQUENCES;
    public static String PREFPAGE_IGNORED_OBJECT_TYPES;
    public static String PREFPAGE_MAX_CHAIN_DEPTH;
    public static String PREFPAGE_MIN_CHAIN_DEPTH;
    public static String PREFPAGE_MAX_CHAINS;
    public static String PREFPAGE_SEARCH_TIMEOUT;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
