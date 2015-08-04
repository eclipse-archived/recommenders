/**
 * Copyright (c) 2015 Pawel Nowak.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.recommenders.internal.news.rcp;

import static org.eclipse.recommenders.internal.news.rcp.TestUtils.enabled;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.recommenders.news.rcp.IFeedMessage;
import org.eclipse.recommenders.news.rcp.IJobFacade;
import org.eclipse.recommenders.news.rcp.INewsProperties;
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
    private final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    private NewsRcpPreferences preferences;
    private EventBus bus;
    private IJobFacade jobFacade;
    private INewsProperties properties;
    private PollFeedJob job;
    private NotificationFacade notificationFacade;

    @Before
    public void setUp() {
        preferences = mock(NewsRcpPreferences.class);
        bus = mock(EventBus.class);
        jobFacade = mock(JobFacade.class);
        properties = mock(NewsProperties.class);
        job = mock(PollFeedJob.class);
        notificationFacade = mock(NotificationFacade.class);
    }

    @Test
    public void testStartEnabledFeed() {
        FeedDescriptor feed = enabled(FIRST_ELEMENT);
        mockPreferences(true, ImmutableList.of(feed));
        Set<FeedDescriptor> feeds = ImmutableSet.of(feed);

        NewsService sut = new NewsService(preferences, bus, properties, jobFacade, notificationFacade);
        sut.doStart();

        verify(jobFacade, times(1)).schedulePollFeeds(sut, feeds);
    }

    @Test
    public void testNotStartDisabledPreferences() {
        FeedDescriptor feed = enabled(FIRST_ELEMENT);
        mockPreferences(false, ImmutableList.of(feed));

        NewsService sut = new NewsService(preferences, bus, properties, jobFacade, notificationFacade);
        sut.doStart();

        verifyZeroInteractions(jobFacade);
    }

    @Test
    public void testGetMessagesIfMoreThanCountPerFeed() throws ParseException {
        FeedDescriptor feed = enabled(FIRST_ELEMENT);
        mockPreferences(true, ImmutableList.of(feed));
        HashMap<FeedDescriptor, List<IFeedMessage>> groupedMessages = Maps.newHashMap();
        groupedMessages.put(feed, mockFeedMessages(MORE_THAN_COUNT_PER_FEED));
        when(job.getMessages()).thenReturn(groupedMessages);

        NewsService sut = new NewsService(preferences, bus, properties, jobFacade, notificationFacade);
        sut.jobDone(job);
        Map<FeedDescriptor, List<IFeedMessage>> sutMessages = sut.getMessages(COUNT_PER_FEED);

        assertThat(sutMessages, hasKey(feed));
        assertThat(sutMessages.get(feed), hasSize(COUNT_PER_FEED));
    }

    @Test
    public void testGetMessagesIfLessThanCountPerFeed() throws ParseException {
        FeedDescriptor feed = enabled(FIRST_ELEMENT);
        mockPreferences(true, ImmutableList.of(feed));
        HashMap<FeedDescriptor, List<IFeedMessage>> groupedMessages = Maps.newHashMap();
        groupedMessages.put(feed, mockFeedMessages(LESS_THAN_COUNT_PER_FEED));
        when(job.getMessages()).thenReturn(groupedMessages);

        NewsService sut = new NewsService(preferences, bus, properties, jobFacade, notificationFacade);
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

        NewsService sut = new NewsService(preferences, bus, properties, jobFacade, notificationFacade);
        sut.jobDone(job);

        assertNotNull(sut.getMessages(COUNT_PER_FEED));
        assertThat(sut.getMessages(COUNT_PER_FEED).isEmpty(), is(true));
    }

    @Test
    public void testGetMessagesIfMoreThanOneFeed() throws ParseException {
        FeedDescriptor feed = enabled(FIRST_ELEMENT);
        FeedDescriptor secondFeed = enabled(SECOND_ELEMENT);
        mockPreferences(true, ImmutableList.of(feed));
        HashMap<FeedDescriptor, List<IFeedMessage>> groupedMessages = Maps.newHashMap();
        groupedMessages.put(feed, mockFeedMessages(MORE_THAN_COUNT_PER_FEED));
        groupedMessages.put(secondFeed, mockFeedMessages(MORE_THAN_COUNT_PER_FEED));
        when(job.getMessages()).thenReturn(groupedMessages);

        NewsService sut = new NewsService(preferences, bus, properties, jobFacade, notificationFacade);
        sut.jobDone(job);
        Map<FeedDescriptor, List<IFeedMessage>> sutMessages = sut.getMessages(COUNT_PER_FEED);

        assertThat(sutMessages.keySet(), containsInAnyOrder(feed, secondFeed));
        assertThat(sutMessages.size(), is(2));
        assertThat(sutMessages.get(feed), hasSize(COUNT_PER_FEED));
    }

    @Test
    public void testShouldntPollFeedWithDateAfter() {
        FeedDescriptor feed = enabled(FIRST_ELEMENT);
        when(preferences.getPollingInterval()).thenReturn(POLLING_INTERVAL);
        when(properties.getDates(Constants.FILENAME_POLL_DATES)).thenReturn(mockPollDates(FIRST_ELEMENT, 1000));

        NewsService sut = new NewsService(preferences, bus, properties, jobFacade, notificationFacade);

        assertThat(sut.shouldPoll(feed, false), is(false));
    }

    @Test
    public void testPollFeedWithDateBefore() {
        FeedDescriptor feed = enabled(FIRST_ELEMENT);
        when(preferences.getPollingInterval()).thenReturn(POLLING_INTERVAL);
        when(properties.getDates(Constants.FILENAME_POLL_DATES)).thenReturn(mockPollDates(FIRST_ELEMENT, -1000));

        NewsService sut = new NewsService(preferences, bus, properties, jobFacade, notificationFacade);

        assertThat(sut.shouldPoll(feed, false), is(true));
    }

    @Test
    public void testShouldPollOverridenFeedWithDateAfter() {
        FeedDescriptor feed = enabled(FIRST_ELEMENT);
        when(preferences.getPollingInterval()).thenReturn(POLLING_INTERVAL);
        when(properties.getDates(Constants.FILENAME_POLL_DATES)).thenReturn(mockPollDates(FIRST_ELEMENT, 1000));

        NewsService sut = new NewsService(preferences, bus, properties, jobFacade, notificationFacade);

        assertThat(sut.shouldPoll(feed, true), is(true));
    }

    @Test
    public void testShouldDisplayNotification() throws ParseException {
        FeedDescriptor feed = enabled(FIRST_ELEMENT);
        mockPreferences(true, ImmutableList.of(feed));
        HashMap<FeedDescriptor, List<IFeedMessage>> groupedMessages = Maps.newHashMap();
        groupedMessages.put(feed, mockFeedMessages(COUNT_PER_FEED));
        when(job.getMessages()).thenReturn(groupedMessages);

        NewsService sut = new NewsService(preferences, bus, properties, jobFacade, notificationFacade);
        sut.jobDone(job);

        verify(notificationFacade).displayNotification(MessageUtils.sortByDate(groupedMessages), bus);
    }

    private Map<String, Date> mockPollDates(String id, int change) {
        Map<String, Date> doc = Maps.newConcurrentMap();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, change);
        Date date = calendar.getTime();
        doc.put(id, date);
        return doc;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void mockPreferences(boolean enabled, ImmutableList feeds) {
        when(preferences.isEnabled()).thenReturn(enabled);
        when(preferences.getFeedDescriptors()).thenReturn(feeds);
        when(preferences.getPollingInterval()).thenReturn(POLLING_INTERVAL);
    }

    private List<IFeedMessage> mockFeedMessages(int count) throws ParseException {
        List<IFeedMessage> messages = Lists.newArrayList();
        for (int i = 0; i < count; i++) {
            messages.add(new FeedMessage("id" + i, dateFormat.parse("10/06/199" + i + " 12:00:00"), "rndm", "rndm",
                    PollFeedJob.toUrl("https://www.eclipse.org/")));
        }
        return messages;
    }
}
