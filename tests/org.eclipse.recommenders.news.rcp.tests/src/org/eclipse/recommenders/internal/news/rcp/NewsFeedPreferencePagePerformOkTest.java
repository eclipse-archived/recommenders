/**
 * Copyright (c) 2015 Pawel Nowak.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.recommenders.internal.news.rcp;

import static org.eclipse.recommenders.internal.news.rcp.TestUtils.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class NewsFeedPreferencePagePerformOkTest {
    private static final String FIRST = "first";
    private static final String SECOND = "second";
    private static final boolean ENABLED = true;
    private static final boolean DISABLED = false;
    private final NewsService service = mock(NewsService.class);
    private final NewsRcpPreferences preferences = mock(NewsRcpPreferences.class);
    private final List<FeedDescriptor> storedFeeds = Lists.newArrayList(mockFeed(FIRST));
    private final List<FeedDescriptor> storedDisabledFeeds = Lists.newArrayList(disabled(FIRST));
    private final List<FeedDescriptor> dirtyFeeds = Lists.newArrayList(mockFeed(FIRST), mockFeed(SECOND));

    @Test
    public void testWhenFeedIsAddedThenServiceStarts() {
        NewsPreferencePage sut = new NewsPreferencePage(service, preferences);
        sut.doPerformOK(ENABLED, ENABLED, storedFeeds, dirtyFeeds);

        verify(service).start();
    }

    @Test
    public void testWhenPluginIsDisabledServiceStops() {
        NewsPreferencePage sut = new NewsPreferencePage(service, preferences);
        sut.doPerformOK(ENABLED, DISABLED, storedFeeds, dirtyFeeds);

        verify(service).forceStop();
    }

    @Test
    public void testDoNothingIfNothingHasChanged() {
        NewsPreferencePage sut = new NewsPreferencePage(service, preferences);
        sut.doPerformOK(ENABLED, ENABLED, storedFeeds, storedFeeds);

        verifyZeroInteractions(service);
    }

    @Test
    public void testWhenPluginIsEnabledServiceStarts() {
        NewsPreferencePage sut = new NewsPreferencePage(service, preferences);
        sut.doPerformOK(DISABLED, ENABLED, storedFeeds, dirtyFeeds);

        verify(service).start();
    }

    @Test
    public void testRemoveFeed() {
        NewsPreferencePage sut = new NewsPreferencePage(service, preferences);
        sut.doPerformOK(ENABLED, ENABLED, dirtyFeeds, storedFeeds);

        verify(service).removeFeed(any(FeedDescriptor.class));
    }

    @Test
    public void testServiceStartsWhenFeedIsEnabled() {
        NewsPreferencePage sut = new NewsPreferencePage(service, preferences);
        sut.doPerformOK(ENABLED, ENABLED, storedDisabledFeeds, storedFeeds);

        verify(service).start();
    }

    @Test
    public void testServiceRemovesFeedWhenFeedIsDisabled() {
        NewsPreferencePage sut = new NewsPreferencePage(service, preferences);
        sut.doPerformOK(ENABLED, ENABLED, storedFeeds, storedDisabledFeeds);

        verify(service).removeFeed(any(FeedDescriptor.class));
    }

}
