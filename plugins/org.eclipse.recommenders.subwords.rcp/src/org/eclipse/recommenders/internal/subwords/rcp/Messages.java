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
package org.eclipse.recommenders.internal.subwords.rcp;

import org.eclipse.osgi.util.NLS;

public final class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.recommenders.internal.subwords.rcp.messages"; //$NON-NLS-1$
    public static String DIALOG_FAIL_TO_GUESS_METHOD_PARAMETERS;
    public static String JOB_DISABLING;
    public static String PREFPAGE_ENABLE_PROPOSALS;
    public static String PREFPAGE_INTRO;
    public static String PREFPAGE_SEE_LINK_TO_CONTENT_ASSIST;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
