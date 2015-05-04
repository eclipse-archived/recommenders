/**
 * Copyright (c) 2015 Pawel Nowak.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.recommenders.internal.news.rcp;

import org.eclipse.osgi.util.NLS;

public final class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.recommenders.internal.news.rcp.messages"; //$NON-NLS-1$

    public static String PREFPAGE_TITLE;
    public static String PREFPAGE_DESCRIPTION;
    public static String FIELD_LABEL_FEEDS;
    public static String FIELD_LABEL_NEWS_ENABLED;
    public static String BUTTON_LABEL_UP;
    public static String BUTTON_LABEL_DOWN;

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
