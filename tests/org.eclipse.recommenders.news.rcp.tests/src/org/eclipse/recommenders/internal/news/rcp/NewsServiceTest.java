/**
 * Copyright (c) 2015 Pawel Nowak.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.recommenders.internal.news.rcp;

import static org.eclipse.recommenders.internal.news.rcp.TestUtils.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.recommenders.news.rcp.IFeedMessage;
import org.eclipse.recommenders.news.rcp.IJobFacade;
import org.eclipse.recommenders.news.rcp.INewsFeedProperties;
import org.eclipse.recommenders.utils.Urls;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;

@RunWith(MockitoJUnitRunner.class)
public class NewsServiceTest {

    private static final String FIRST_ELEMENT = "first";
    private static final String SECOND_ELEMENT = "second";
    private static final int MORE_THAN_COUNT_PER_FEED = 5;
    private static final int LESS_THAN_COUNT_PER_FEED = 2;
    private static final Long POLLING_INTERVAL = 1L;
    private static final int COUNT_PER_FEED = 3;
    private NewsRcpPreferences preferences;
    private EventBus bus;
    private IJobFacade jobFacade;
    private INewsFeedProperties properties;
    private PollFeedJob job;

    @Before
    public void setUp() {
        preferences = mock(NewsRcpPreferences.class);
        bus = mock(EventBus.class);
        jobFacade = mock(JobFacade.class);
        properties = mock(NewsFeedProperties.class);
        job = mock(PollFeedJob.class);
    }

    @Test
    public void testStartEnabledFeed() {
        FeedDescriptor feed = enabled(FIRST_ELEMENT);
        mockPreferences(true, ImmutableList.of(feed));
        Set<FeedDescriptor> feeds = ImmutableSet.of(feed);
        NewsService sut = new NewsService(preferences, bus, jobFacade, properties);
        sut.start();
        verify(jobFacade, times(1)).schedule(feeds, sut);
    }

    @Test
    public void testNotStartDisabledFeed() {
        FeedDescriptor feed = disabled(FIRST_ELEMENT);
        mockPreferences(true, ImmutableList.of(feed));
        Set<FeedDescriptor> feeds = ImmutableSet.of(feed);
        NewsService sut = new NewsService(preferences, bus, jobFacade, properties);
        sut.start();
        verify(jobFacade, never()).schedule(feeds, sut);
    }

    @Test
    public void testNotStartDisabledPreferences() {
        FeedDescriptor feed = enabled(FIRST_ELEMENT);
        mockPreferences(false, ImmutableList.of(feed));
        NewsService sut = new NewsService(preferences, bus, jobFacade, properties);
        sut.start();
        verifyZeroInteractions(jobFacade);
    }

    @Test
    public void testGetMessagesIfMoreThanCountPerFeed() {
        FeedDescriptor feed = enabled(FIRST_ELEMENT);
        mockPreferences(true, ImmutableList.of(feed));
        HashMap<FeedDescriptor, List<IFeedMessage>> groupedMessages = Maps.newHashMap();
        groupedMessages.put(feed, mockFeedMessages(MORE_THAN_COUNT_PER_FEED));
        when(job.getMessages()).thenReturn(groupedMessages);
        NewsService sut = new NewsService(preferences, bus, jobFacade, properties);
        sut.jobDone(job);
        Map<FeedDescriptor, List<IFeedMessage>> sutMessages = sut.getMessages(COUNT_PER_FEED);
        assertThat(sutMessages, hasKey(feed));
        assertThat(sutMessages.get(feed), hasSize(COUNT_PER_FEED));
    }

    @Test
    public void testGetMessagesIfLessThanCountPerFeed() {
        FeedDescriptor feed = enabled(FIRST_ELEMENT);
        mockPreferences(true, ImmutableList.of(feed));
        HashMap<FeedDescriptor, List<IFeedMessage>> groupedMessages = Maps.newHashMap();
        groupedMessages.put(feed, mockFeedMessages(LESS_THAN_COUNT_PER_FEED));
        when(job.getMessages()).thenReturn(groupedMessages);
        NewsService sut = new NewsService(preferences, bus, jobFacade, properties);
        sut.jobDone(job);
        Map<FeedDescriptor, List<IFeedMessage>> sutMessages = sut.getMessages(COUNT_PER_FEED);
        assertThat(sutMessages, hasKey(feed));
        assertThat(sutMessages.get(feed), hasSize(LESS_THAN_COUNT_PER_FEED));
    }

    @Test
    public void testGetMessagesIfNoFeed() {
        FeedDescriptor feed = enabled(FIRST_ELEMENT);
        mockPreferences(true, ImmutableList.of(feed));
        HashMap<FeedDescriptor, List<IFeedMessage>> groupedMessages = Maps.newHashMap();
        when(job.getMessages()).thenReturn(groupedMessages);
        NewsService sut = new NewsService(preferences, bus, jobFacade, properties);
        sut.jobDone(job);
        assertNotNull(sut.getMessages(COUNT_PER_FEED));
        assertThat(sut.getMessages(COUNT_PER_FEED).isEmpty(), is(true));
    }

    @Test
    public void testGetMessagesIfMoreThanOneFeed() {
        FeedDescriptor feed = enabled(FIRST_ELEMENT);
        FeedDescriptor secondFeed = enabled(SECOND_ELEMENT);
        mockPreferences(true, ImmutableList.of(feed));
        HashMap<FeedDescriptor, List<IFeedMessage>> groupedMessages = Maps.newHashMap();
        groupedMessages.put(feed, mockFeedMessages(MORE_THAN_COUNT_PER_FEED));
        groupedMessages.put(secondFeed, mockFeedMessages(MORE_THAN_COUNT_PER_FEED));
        when(job.getMessages()).thenReturn(groupedMessages);
        NewsService sut = new NewsService(preferences, bus, jobFacade, properties);
        sut.jobDone(job);
        Map<FeedDescriptor, List<IFeedMessage>> sutMessages = sut.getMessages(COUNT_PER_FEED);
        assertThat(sutMessages.keySet(), containsInAnyOrder(feed, secondFeed));
        assertThat(sutMessages.size(), is(2));
        assertThat(sutMessages.get(feed), hasSize(COUNT_PER_FEED));
    }

    private void mockPreferences(boolean enabled, ImmutableList feeds) {
        when(preferences.isEnabled()).thenReturn(enabled);
        when(preferences.getFeedDescriptors()).thenReturn(feeds);
        when(preferences.getPollingInterval()).thenReturn(POLLING_INTERVAL);
    }

    private List<IFeedMessage> mockFeedMessages(int count) {
        List<IFeedMessage> messages = Lists.newArrayList();
        for (int i = 0; i < count; i++) {
            messages.add(new FeedMessage("id" + i, new Date(), "rndm", "rndm", Urls.toUrl("https://www.eclipse.org/")));
        }
        return messages;
    }
}
