/**
 * Copyright (c) 2015 Pawel Nowak.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.recommenders.internal.news.rcp;

import static org.eclipse.recommenders.internal.news.rcp.TestUtils.mockFeed;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.recommenders.internal.news.rcp.PollingResult.Status;
import org.eclipse.recommenders.news.rcp.IFeedMessage;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

@RunWith(Parameterized.class)
public class MessageUtilsMergeMessagesTest {

    private Map<FeedDescriptor, PollingResult> inputMap;
    private List<IFeedMessage> expectedMessages;

    public MessageUtilsMergeMessagesTest(Map<FeedDescriptor, PollingResult> inputMap,
            List<IFeedMessage> expectedMessages) {
        this.inputMap = inputMap;
        this.expectedMessages = expectedMessages;
    }

    @Parameters
    public static Collection<Object[]> scenarios() {
        List<Object[]> scenarios = Lists.newArrayList();

        IFeedMessage readMessage = mockMessage("read", true);
        IFeedMessage unreadMessage = mockMessage("unread", false);

        scenarios.add(new Object[] { null, Collections.emptyList() });
        scenarios.add(
                new Object[] { ImmutableMap.of(mockFeed("emptyFeed"), mockPollingResult()), Collections.emptyList() });
        scenarios.add(new Object[] { ImmutableMap.of(mockFeed("oneRead"), mockPollingResult(readMessage)),
                ImmutableList.of(readMessage) });
        scenarios.add(new Object[] { ImmutableMap.of(mockFeed("oneUnread"), mockPollingResult(unreadMessage)),
                ImmutableList.of(unreadMessage) });
        scenarios.add(new Object[] {
                ImmutableMap.of(mockFeed("oneUnreadOneRead"), mockPollingResult(unreadMessage, readMessage)),
                ImmutableList.of(unreadMessage, readMessage) });
        scenarios.add(new Object[] { ImmutableMap.of(mockFeed("unreadFeed"), mockPollingResult(unreadMessage),
                mockFeed("readFeed"), mockPollingResult(readMessage)), ImmutableList.of(unreadMessage, readMessage) });

        return scenarios;
    }

    private static IFeedMessage mockMessage(String id, boolean read) {
        IFeedMessage message = mock(IFeedMessage.class);
        when(message.getId()).thenReturn(id);
        when(message.isRead()).thenReturn(read);
        return message;
    }

    @Test
    public void testMergedMessages() {
        List<IFeedMessage> mergedMessages = MessageUtils.mergeMessages(inputMap);
        assertThat(mergedMessages, is(Matchers.equalTo(expectedMessages)));
        assertThat(mergedMessages, hasSize(expectedMessages.size()));
    }

    private static PollingResult mockPollingResult(IFeedMessage... messages) {
        List<IFeedMessage> feedMessages = Lists.newArrayList();
        for (IFeedMessage message : messages) {
            feedMessages.add(message);
        }
        return new PollingResult(Status.OK, feedMessages);
    }
}
