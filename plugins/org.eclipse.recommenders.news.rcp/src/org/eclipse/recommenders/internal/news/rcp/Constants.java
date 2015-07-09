/**
 * Copyright (c) 2015 Pawel Nowak.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.recommenders.internal.news.rcp;

import java.util.concurrent.TimeUnit;

public final class Constants {

    private Constants() {
        throw new IllegalStateException("Not meant to be instantiated"); //$NON-NLS-1$
    }

    public static final String PLUGIN_ID = "org.eclipse.recommenders.news.rcp"; //$NON-NLS-1$
    public static final String PREF_FEED_LIST_SORTED = "feed.list.sorted"; //$NON-NLS-1$
    public static final String PREF_NEWS_ENABLED = "newsEnabled"; //$NON-NLS-1$
    public static final String PREF_NOTIFICATION_ENABLED = "newsNotificationEnabled"; //$NON-NLS-1$
    public static final String POLL_FEED_JOB_FAMILY = "newsFeedJob"; //$NON-NLS-1$
    public static final String PREF_POLLING_INTERVAL = "pollingInterval"; //$NON-NLS-1$
    public static final Long DEFAULT_POLLING_INTERVAL = TimeUnit.HOURS.toMinutes(3);
    public static final String FILENAME_POLL_DATES = "poll-dates.properties"; //$NON-NLS-1$
    public static final String FILENAME_FEED_DATES = "feed-dates.properties"; //$NON-NLS-1$
    public static final int COUNT_PER_FEED = 20;
    public static final String PREF_CUSTOM_FEED_LIST_SORTED = "custom.feed.list.sorted"; //$NON-NLS-1$
    public static final String ATTRIBUTE_URL = "url"; //$NON-NLS-1$
    public static final String ATTRIBUTE_ID = "id"; //$NON-NLS-1$
    public static final String ATTRIBUTE_NAME = "name"; //$NON-NLS-1$
    public static final String ATTRIBUTE_DESCRIPTION = "description"; //$NON-NLS-1$
    public static final String ATTRIBUTE_POLLING_INTERVAL = "pollingInterval"; //$NON-NLS-1$
    public static final String ATTRIBUTE_ICON = "icon"; //$NON-NLS-1$
    public static final String SYSPROP_ECLIPSE_BUILD_ID = "eclipse.buildId"; //$NON-NLS-1$

}
