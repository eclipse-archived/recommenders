/**
 * Copyright (c) 2015 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Dorn - initial API and implementation.
 */
package org.eclipse.recommenders.internal.news.rcp;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.di.extensions.Preference;

@Creatable
@Singleton
@SuppressWarnings("restriction")
public class NewsRcpPreferences {

    @Inject
    @Preference(PreferenceConstants.NEWS_ENABLED)
    private boolean enabled;

    @Inject
    @Preference(PreferenceConstants.FEED_LIST_SORTED)
    private String feeds;

    @Inject
    @Preference(PreferenceConstants.CUSTOM_FEED_LIST_SORTED)
    private String customFeeds;

    @Inject
    @Preference(PreferenceConstants.POLLING_INTERVAL)
    private Long pollingInterval;

    @Inject
    @Preference(PreferenceConstants.POLLING_DELAY)
    private Long pollingDelay;

    public boolean isEnabled() {
        return enabled;
    }

    public Long getPollingInterval() {
        return pollingInterval;
    }

    public Long getPollingDelay() {
        return pollingDelay;
    }

    public List<FeedDescriptor> getFeedDescriptors() {
        List<FeedDescriptor> feeds = FeedDescriptors.load(this.feeds, FeedDescriptors.getRegisteredFeeds());
        feeds.addAll(FeedDescriptors.getFeeds(customFeeds));
        return feeds;
    }
}
