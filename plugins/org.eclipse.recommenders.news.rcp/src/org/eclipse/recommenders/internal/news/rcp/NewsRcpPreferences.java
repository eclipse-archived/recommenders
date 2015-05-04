/**
 * Copyright (c) 2015 Codetrails GmbH. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Johannes Dorn - initial API and implementation.
 */
package org.eclipse.recommenders.internal.news.rcp;

import static org.eclipse.recommenders.internal.news.rcp.Constants.PREF_FEED_LIST_SORTED;

import java.util.List;

import javax.inject.Inject;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.di.extensions.Preference;

@SuppressWarnings("restriction")
public class NewsRcpPreferences extends AbstractPreferenceInitializer {

    @Inject
    @Preference(Constants.PREF_NEWS_ENABLED)
    private boolean enabled;

    @Inject
    @Preference(Constants.PREF_FEED_LIST_SORTED)
    private String feeds;

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void initializeDefaultPreferences() {
        IEclipsePreferences s = DefaultScope.INSTANCE.getNode(Constants.PLUGIN_ID);
        s.putBoolean(Constants.PREF_NEWS_ENABLED, true);
        s.put(PREF_FEED_LIST_SORTED, FeedDescriptors.store(FeedDescriptors.getRegisteredFeeds()));
    }

    public List<FeedDescriptor> getEnabledFeedDescriptors() {
        return FeedDescriptors.load(feeds, FeedDescriptors.getRegisteredFeeds());
    }
}
