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
    public static final String JOB_FAMILY = "newsFeedJob"; //$NON-NLS-1$
    public static final String PREF_POLLING_INTERVAL = "pollingInterval"; //$NON-NLS-1$
    public static final Long DEFAULT_POLLING_INTERVAL = TimeUnit.HOURS.toMinutes(3);

}
