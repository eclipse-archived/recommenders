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
import static org.junit.Assert.assertThat;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.eclipse.recommenders.news.rcp.INewsFeedProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

@RunWith(MockitoJUnitRunner.class)
public class NewsFeedPropertiesTest {

    private final String testId = "testID";
    private final String testIdTwo = "testIDtwo";
    private final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    @Test
    public void testWriteSingleId() {
        INewsFeedProperties sut = new NewsFeedProperties();
        Set<String> writeIds = ImmutableSet.of(testId);
        sut.writeReadIds(writeIds);
        Set<String> readIds = sut.getReadIds();
        assertThat(readIds, contains(testId));
        assertThat(readIds.size(), is(1));
    }

    @Test
    public void testWriteMultipleIds() {
        INewsFeedProperties sut = new NewsFeedProperties();
        Set<String> writeIds = ImmutableSet.of(testId, testIdTwo);
        sut.writeReadIds(writeIds);
        Set<String> readIds = sut.getReadIds();
        assertThat(readIds, containsInAnyOrder(testId, testIdTwo));
        assertThat(readIds.size(), is(2));
    }

    @Test
    public void testWriteEmptySet() {
        INewsFeedProperties sut = new NewsFeedProperties();
        Set<String> writeIds = Sets.newHashSet();
        sut.writeReadIds(writeIds);
        Set<String> readIds = sut.getReadIds();
        assertThat(readIds.isEmpty(), is(true));
    }

    @Test
    public void testWriteNullReadIdSet() {
        INewsFeedProperties sut = new NewsFeedProperties();
        Set<String> writeIds = null;
        sut.writeReadIds(writeIds);
        Set<String> readIds = sut.getReadIds();
        assertThat(readIds.isEmpty(), is(true));
    }

    @Test
    public void testWritePollDate() throws ParseException {
        INewsFeedProperties sut = new NewsFeedProperties();
        Map<FeedDescriptor, Date> writePollDates = Maps.newHashMap();
        Date date = dateFormat.parse(dateFormat.format(new Date()));
        FeedDescriptor feed = enabled(testId);
        writePollDates.put(feed, date);
        sut.writePollDates(writePollDates);
        Map<String, Date> readPollDates = sut.getPollDates();
        assertThat(readPollDates.keySet().contains(feed.getId()), is(true));
        assertThat(readPollDates.values().contains(date), is(true));
    }

    @Test
    public void testdWriteMultiplePollDates() throws ParseException {
        INewsFeedProperties sut = new NewsFeedProperties();
        Map<FeedDescriptor, Date> writePollDates = Maps.newHashMap();
        Date date = dateFormat.parse(dateFormat.format(new Date()));
        FeedDescriptor feed = enabled(testId);
        FeedDescriptor secondFeed = enabled(testIdTwo);
        writePollDates.put(feed, date);
        writePollDates.put(secondFeed, date);
        sut.writePollDates(writePollDates);
        Map<String, Date> readPollDates = sut.getPollDates();
        assertThat(readPollDates.keySet(), containsInAnyOrder(testId, testIdTwo));
        assertThat(readPollDates.values(), containsInAnyOrder(date, date));
        assertThat(readPollDates.size(), is(2));
    }

    @Test
    public void testWriteEmptyMap() {
        INewsFeedProperties sut = new NewsFeedProperties();
        Map<FeedDescriptor, Date> writePollDates = Maps.newHashMap();
        sut.writePollDates(writePollDates);
        Map<String, Date> readPollDates = sut.getPollDates();
        assertThat(readPollDates.size(), is(2));
    }

    // flickering test
    @Test
    public void testWriteNullMap() {
        INewsFeedProperties sut = new NewsFeedProperties();
        Map<FeedDescriptor, Date> writePollDates = null;
        sut.writePollDates(writePollDates);
        Map<String, Date> readPollDates = sut.getPollDates();
        assertThat(readPollDates.size(), is(2));
    }
}
