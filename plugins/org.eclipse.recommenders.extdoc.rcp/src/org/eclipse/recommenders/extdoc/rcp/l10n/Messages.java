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
package org.eclipse.recommenders.extdoc.rcp.l10n;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.recommenders.extdoc.rcp.l10n.messages"; //$NON-NLS-1$
    public static String EXTDOC_OVERRIDES_INTRO;
    public static String EXTDOC_OVERRIDES_INTRO_PATTERN;
    public static String EXTDOC_OVERRIDES_PERCENTAGE;
    public static String EXTDOC_OVERRIDES_PERCENTAGE_PATTERN;
    public static String EXTDOC_OVERRIDES_OVERRIDE;
    public static String EXTDOC_OVERRIDES_OVERRIDES;
    public static String EXTDOC_SELFCALLS_CALLS;
    public static String EXTDOC_SELFCALLS_INTRO_IMPLEMENTORS;
    public static String EXTDOC_SELFCALLS_INTRO_SUBCLASSES;
    public static String EXTDOC_STATICHOCKS_NO_PUBLIC_STATIC_METHOD_FOUND;
    public static String EXTDOC_JAVADOC_NOT_FOUND;
    public static String EXTDOC_UPDATE_JOB;
    public static String EXTDOC_PERCENTAGE_TIMES;
    public static String EXTDOC_ALWAYS;
    public static String EXTDOC_USUALLY;
    public static String EXTDOC_SOMETIMES;
    public static String EXTDOC_OCCASIONALLY;
    public static String EXTDOC_RARELY;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
