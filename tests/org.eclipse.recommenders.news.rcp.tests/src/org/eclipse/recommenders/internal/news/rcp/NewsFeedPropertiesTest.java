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

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
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

    private NewsFeedProperties sut;

    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    @Before
    public void setUp() throws IOException {
        File readMessagesFile = tmp.newFile("read-messages.properties");
        File pollDatesFile = tmp.newFile("poll-dates.properties");
        File feedDatesFile = tmp.newFile("feed-dates.properties");
        sut = new NewsFeedProperties(readMessagesFile, pollDatesFile, feedDatesFile);
    }

    @Test
    public void testWriteSingleId() {
        Set<String> writeIds = ImmutableSet.of(testId);

        sut.writeReadIds(writeIds);
        Set<String> readIds = sut.getReadIds();

        assertThat(readIds, contains(testId));
        assertThat(readIds.size(), is(1));
    }

    @Test
    public void testWriteMultipleIds() {
        Set<String> writeIds = ImmutableSet.of(testId, testIdTwo);

        sut.writeReadIds(writeIds);
        Set<String> readIds = sut.getReadIds();

        assertThat(readIds, containsInAnyOrder(testId, testIdTwo));
        assertThat(readIds.size(), is(2));
    }

    @Test
    public void testWriteEmptySet() {
        Set<String> writeIds = Sets.newHashSet();

        sut.writeReadIds(writeIds);
        Set<String> readIds = sut.getReadIds();

        assertThat(readIds.isEmpty(), is(true));
    }

    @Test
    public void testWriteNullReadIdSet() {
        Set<String> writeIds = null;

        sut.writeReadIds(writeIds);
        Set<String> readIds = sut.getReadIds();

        assertThat(readIds.isEmpty(), is(true));
    }

    @Test
    public void testWritePollDate() throws ParseException {
        Map<FeedDescriptor, Date> writePollDates = Maps.newHashMap();
        Date date = dateFormat.parse(dateFormat.format(new Date()));
        FeedDescriptor feed = enabled(testId);
        writePollDates.put(feed, date);

        sut.writeDates(writePollDates, Constants.FILENAME_POLL_DATES);
        Map<String, Date> readPollDates = sut.getDates(Constants.FILENAME_POLL_DATES);

        assertThat(readPollDates.keySet().contains(feed.getId()), is(true));
        assertThat(readPollDates.values().contains(date), is(true));
    }

    @Test
    public void testdWriteMultiplePollDates() throws ParseException {
        Map<FeedDescriptor, Date> writePollDates = Maps.newHashMap();
        Date date = dateFormat.parse(dateFormat.format(new Date()));
        FeedDescriptor feed = enabled(testId);
        FeedDescriptor secondFeed = enabled(testIdTwo);
        writePollDates.put(feed, date);
        writePollDates.put(secondFeed, date);

        sut.writeDates(writePollDates, Constants.FILENAME_POLL_DATES);
        Map<String, Date> readPollDates = sut.getDates(Constants.FILENAME_POLL_DATES);

        assertThat(readPollDates.keySet(), containsInAnyOrder(testId, testIdTwo));
        assertThat(readPollDates.values(), containsInAnyOrder(date, date));
        assertThat(readPollDates.size(), is(2));
    }

    @Test
    public void testWriteEmptyMap() {
        Map<FeedDescriptor, Date> writePollDates = Maps.newHashMap();

        sut.writeDates(writePollDates, Constants.FILENAME_POLL_DATES);
        Map<String, Date> readPollDates = sut.getDates(Constants.FILENAME_POLL_DATES);

        assertTrue(readPollDates.isEmpty());
    }

    @Test
    public void testWriteNullMap() {
        Map<FeedDescriptor, Date> writePollDates = null;

        sut.writeDates(writePollDates, Constants.FILENAME_POLL_DATES);
        Map<String, Date> readPollDates = sut.getDates(Constants.FILENAME_POLL_DATES);

        assertTrue(readPollDates.isEmpty());
    }
}
