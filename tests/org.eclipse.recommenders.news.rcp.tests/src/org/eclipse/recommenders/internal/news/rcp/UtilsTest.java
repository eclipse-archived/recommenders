/**
 * Copyright (c) 2015 Pawel Nowak.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.recommenders.internal.news.rcp;

import static org.eclipse.recommenders.internal.news.rcp.TestUtils.enabled;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.recommenders.news.rcp.IFeedMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@RunWith(MockitoJUnitRunner.class)
public class UtilsTest {
    private final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"); //$NON-NLS-1$

    @Test
    public void testSortByDate() throws ParseException {
        HashMap<FeedDescriptor, List<IFeedMessage>> messages = Maps.newHashMap();
        FeedDescriptor feed = enabled("rndm");
        List<IFeedMessage> iFeedMessages = Lists.newArrayList();
        FeedMessage messageA = mock(FeedMessage.class);
        FeedMessage messageB = mock(FeedMessage.class);
        when(messageA.getDate()).thenReturn(dateFormat.parse("10/06/1991 20:30:30"));
        when(messageB.getDate()).thenReturn(dateFormat.parse("10/06/1991 20:30:31"));
        iFeedMessages.add(messageA);
        iFeedMessages.add(messageB);
        messages.put(feed, iFeedMessages);

        Map<FeedDescriptor, List<IFeedMessage>> sut = Utils.sortByDate(messages);

        assertEquals(messageB.getDate(), sut.get(feed).get(0).getDate());
    }

    @Test
    public void testSortByDateEqualDates() throws ParseException {
        HashMap<FeedDescriptor, List<IFeedMessage>> messages = Maps.newHashMap();
        FeedDescriptor feed = enabled("rndm");
        List<IFeedMessage> iFeedMessages = Lists.newArrayList();
        FeedMessage messageA = mock(FeedMessage.class);
        FeedMessage messageB = mock(FeedMessage.class);
        FeedMessage messageC = mock(FeedMessage.class);
        when(messageA.getDate()).thenReturn(dateFormat.parse("10/06/1991 20:30:30"));
        when(messageB.getDate()).thenReturn(dateFormat.parse("10/06/1991 20:30:30"));
        when(messageC.getDate()).thenReturn(dateFormat.parse("10/06/1991 20:30:29"));
        iFeedMessages.add(messageC);
        iFeedMessages.add(messageA);
        iFeedMessages.add(messageB);
        messages.put(feed, iFeedMessages);

        Map<FeedDescriptor, List<IFeedMessage>> sut = Utils.sortByDate(messages);

        assertEquals(messageC.getDate(), sut.get(feed).get(2).getDate());
        assertEquals(messageA.getDate(), sut.get(feed).get(0).getDate());
    }

    @Test
    public void testSortByDateEmptyMap() {
        HashMap<FeedDescriptor, List<IFeedMessage>> messages = Maps.newHashMap();

        Map<FeedDescriptor, List<IFeedMessage>> sut = Utils.sortByDate(messages);

        assertTrue(sut.isEmpty());
    }

    @Test
    public void testSortByDateNullMap() {
        Map<FeedDescriptor, List<IFeedMessage>> sut = Utils.sortByDate(null);

        assertTrue(sut.isEmpty());
    }
}
