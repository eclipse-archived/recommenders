/**
 * Copyright (c) 2015 Pawel Nowak.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.recommenders.internal.news.rcp.l10n;

import org.eclipse.osgi.util.NLS;

public final class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.recommenders.internal.news.rcp.l10n.messages"; //$NON-NLS-1$

    public static String FEED_DESCRIPTOR_MALFORMED_URL;

    public static String PREFPAGE_TITLE;
    public static String PREFPAGE_DESCRIPTION;
    public static String FIELD_LABEL_FEEDS;
    public static String FIELD_LABEL_NEWS_ENABLED;

    public static String JOB_NAME_CLOSE;
    public static String JOB_NAME_FADE;
    public static String LABEL_NOTIFICATION_NAME;
    public static String LABEL_NO_NEW_MESSAGS;
    public static String TOOLTIP_NO_NEW_MESSAGES;
    public static String TOOLTIP_NEW_MESSAGES;

    public static String NOTIFICATION_TITLE;

    public static String LOG_ERROR_READING_PROPERTIES;
    public static String LOG_ERROR_WRITING_PROPERTIES;

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
