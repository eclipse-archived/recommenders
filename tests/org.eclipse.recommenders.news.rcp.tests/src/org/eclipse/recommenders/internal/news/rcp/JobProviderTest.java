/**
 * Copyright (c) 2015 Pawel Nowak.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.recommenders.internal.news.rcp;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import org.eclipse.mylyn.commons.notifications.core.NotificationEnvironment;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@SuppressWarnings("restriction")
@RunWith(MockitoJUnitRunner.class)
public class JobProviderTest {

    private static final String FIRST_ELEMENT = "first";
    private NewsRcpPreferences preferences;
    private NotificationEnvironment environment;
    private FeedDescriptor feed;
    private RssService service;

    @Before
    public void setUp() {
        preferences = mock(NewsRcpPreferences.class);
        environment = mock(NotificationEnvironment.class);
        feed = mock(FeedDescriptor.class);
        service = mock(RssService.class);
    }

    @Test
    public void testGetPollFeedJob() {
        JobProvider provider = new JobProvider();
        when(feed.getId()).thenReturn(FIRST_ELEMENT);
        provider.getPollFeedJob(feed, preferences, environment, service);
        assertThat(provider.getPollFeedJob(feed, preferences, environment, service), is(not(nullValue())));
    }

    @Test
    public void testJobExists() {
        JobProvider provider = new JobProvider();
        FeedDescriptor second = mock(FeedDescriptor.class);
        when(feed.getId()).thenReturn(FIRST_ELEMENT);
        when(second.getId()).thenReturn(FIRST_ELEMENT);
        provider.schedule(provider.getPollFeedJob(feed, preferences, environment, service));
        assertThat(provider.jobExists(second, preferences, environment), is(true));
    }
}
