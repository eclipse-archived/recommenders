/**
 * Copyright (c) 2016 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Sewe - initial API and implementation.
 */
package org.eclipse.recommenders.internal.news.rcp.preferences;

import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.recommenders.internal.news.rcp.Constants;
import org.eclipse.recommenders.internal.news.rcp.FeedDescriptors;
import org.eclipse.recommenders.internal.news.rcp.PreferenceConstants;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

    private static final long DEFAULT_POLLING_INTERVAL = TimeUnit.MINUTES.toMinutes(5);
    private static final long DEFAULT_POLLING_DELAY = TimeUnit.MINUTES.toMinutes(5);

    @Override
    public void initializeDefaultPreferences() {
        IEclipsePreferences s = DefaultScope.INSTANCE.getNode(Constants.PLUGIN_ID);
        s.putBoolean(PreferenceConstants.NEWS_ENABLED, true);
        s.putLong(PreferenceConstants.POLLING_INTERVAL, DEFAULT_POLLING_INTERVAL);
        s.putLong(PreferenceConstants.POLLING_DELAY, DEFAULT_POLLING_DELAY);
        s.put(PreferenceConstants.FEED_LIST_SORTED, FeedDescriptors.feedsToString(FeedDescriptors.getRegisteredFeeds()));
    }
}
