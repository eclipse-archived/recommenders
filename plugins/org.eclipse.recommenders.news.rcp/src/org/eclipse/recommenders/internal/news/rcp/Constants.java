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
        throw new AssertionError();
    }

    public static final String PREF_PAGE_ID = "org.eclipse.recommenders.news.rcp.preferencePages.news"; //$NON-NLS-1$

    public static final String PLUGIN_ID = "org.eclipse.recommenders.news.rcp"; //$NON-NLS-1$
    public static final int COUNT_PER_FEED = 20;
    public static final String ATTRIBUTE_URI = "uri"; //$NON-NLS-1$
    public static final String ATTRIBUTE_ID = "id"; //$NON-NLS-1$
    public static final String ATTRIBUTE_NAME = "name"; //$NON-NLS-1$
    public static final String ATTRIBUTE_POLLING_INTERVAL = "pollingInterval"; //$NON-NLS-1$
    public static final String ATTRIBUTE_ENABLED_BY_DEFAULT = "enabledByDefault"; //$NON-NLS-1$
    public static final String SYSPROP_ECLIPSE_BUILD_ID = "eclipse.buildId"; //$NON-NLS-1$
    public static final String BUNDLE_HEADER_NAME = "Bundle-Name"; //$NON-NLS-1$

    public static final long DEFAULT_POLLING_INTERVAL = TimeUnit.HOURS.toMinutes(8);

    public static final int MAX_FEED_ITEMS = 20;
}
