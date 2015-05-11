/**
 * Copyright (c) 2015 Pawel Nowak.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.recommenders.internal.news.rcp;

import static org.eclipse.recommenders.internal.news.rcp.TestUtils.*;
import static org.mockito.Mockito.*;

import org.eclipse.mylyn.commons.notifications.core.NotificationEnvironment;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;

@SuppressWarnings("restriction")
@RunWith(MockitoJUnitRunner.class)
public class RssServiceTest {

    private static final String FIRST_ELEMENT = "first";
    private NotificationEnvironment environment;
    private NewsRcpPreferences preferences;
    private EventBus bus;
    private PollFeedJob job;
    private JobProvider provider;

    @Before
    public void setUp() {
        environment = mock(NotificationEnvironment.class);
        preferences = mock(NewsRcpPreferences.class);
        bus = mock(EventBus.class);
        job = mock(PollFeedJob.class);
        provider = mock(JobProvider.class);
    }

    @Test
    public void testStartEnabledFeed() {
        FeedDescriptor feed = enabled(FIRST_ELEMENT);
        when(preferences.isEnabled()).thenReturn(true);
        when(preferences.getFeedDescriptors()).thenReturn(ImmutableList.of(feed));
        RssService service = new RssService(preferences, bus, environment, provider);
        when(provider.getPollFeedJob(Mockito.any(FeedDescriptor.class), Mockito.any(NewsRcpPreferences.class),
                Mockito.any(NotificationEnvironment.class), Mockito.any(RssService.class))).thenReturn(job);
        service.start(feed);
        verify(provider).getPollFeedJob(feed, preferences, environment, service);
        verify(provider, times(1)).schedule(job);

    }

    @Test
    public void testStartDisabledFeed() {
        FeedDescriptor feed = disabled(FIRST_ELEMENT);
        when(preferences.isEnabled()).thenReturn(true);
        when(preferences.getFeedDescriptors()).thenReturn(ImmutableList.of(feed));
        RssService service = new RssService(preferences, bus, environment, provider);
        when(provider.getPollFeedJob(Mockito.any(FeedDescriptor.class), Mockito.any(NewsRcpPreferences.class),
                Mockito.any(NotificationEnvironment.class), Mockito.any(RssService.class))).thenReturn(job);
        service.start();
        verify(provider, times(0)).schedule(job);
    }

    @Test
    public void testStartDisabledPreferences() {
        FeedDescriptor feed = enabled(FIRST_ELEMENT);
        when(preferences.isEnabled()).thenReturn(false);
        when(preferences.getFeedDescriptors()).thenReturn(ImmutableList.of(feed));
        RssService service = new RssService(preferences, bus, environment, provider);
        when(provider.getPollFeedJob(Mockito.any(FeedDescriptor.class), Mockito.any(NewsRcpPreferences.class),
                Mockito.any(NotificationEnvironment.class), Mockito.any(RssService.class))).thenReturn(job);
        service.start();
        verify(provider, times(0)).schedule(job);
    }
}
