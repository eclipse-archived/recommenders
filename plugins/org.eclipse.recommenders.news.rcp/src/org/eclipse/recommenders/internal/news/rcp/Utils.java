/**
 * Copyright (c) 2015 Pawel Nowak.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.recommenders.internal.news.rcp;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.recommenders.news.rcp.IFeedMessage;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class Utils {

    public static boolean containsUnreadMessages(Map<FeedDescriptor, List<IFeedMessage>> map) {
        if (map == null) {
            return false;
        }
        for (Map.Entry<FeedDescriptor, List<IFeedMessage>> entry : map.entrySet()) {
            for (IFeedMessage message : entry.getValue()) {
                if (!message.isRead()) {
                    return true;
                }
            }
        }
        return false;
    }

    public static Map<FeedDescriptor, List<IFeedMessage>> getLatestMessages(
            Map<FeedDescriptor, List<IFeedMessage>> messages) {
        Preconditions.checkNotNull(messages);
        Map<FeedDescriptor, List<IFeedMessage>> result = Maps.newHashMap();
        for (Entry<FeedDescriptor, List<IFeedMessage>> entry : messages.entrySet()) {
            List<IFeedMessage> list = updateMessages(entry);
            if (!list.isEmpty()) {
                result.put(entry.getKey(), list);
            }
        }
        return result;
    }

    public static List<IFeedMessage> updateMessages(Entry<FeedDescriptor, List<IFeedMessage>> entry) {
        NewsFeedProperties properties = new NewsFeedProperties();
        List<IFeedMessage> feedMessages = Lists.newArrayList();
        for (IFeedMessage message : entry.getValue()) {
            if (properties.getDates(Constants.FILENAME_FEED_DATES).get(entry.getKey().getId()) == null) {
                feedMessages.add(message);
            } else if (message.getDate()
                    .after(properties.getDates(Constants.FILENAME_FEED_DATES).get(entry.getKey().getId()))) {
                feedMessages.add(message);
            }
        }
        return feedMessages;
    }

    public static int getUnreadMessagesNumber(List<IFeedMessage> messages) {
        if (messages == null) {
            return 0;
        }
        int counter = 0;
        for (IFeedMessage message : messages) {
            if (!message.isRead()) {
                counter++;
            }
        }
        return counter;
    }

    public static List<IFeedMessage> mergeMessages(Map<FeedDescriptor, List<IFeedMessage>> messages) {
        Preconditions.checkNotNull(messages);
        List<IFeedMessage> result = Lists.newArrayList();
        for (Map.Entry<FeedDescriptor, List<IFeedMessage>> entry : messages.entrySet()) {
            result.addAll(entry.getValue());
        }
        return result;
    }

    public static Map<FeedDescriptor, List<IFeedMessage>> sortByDate(Map<FeedDescriptor, List<IFeedMessage>> map) {
        if (map == null) {
            return Maps.newHashMap();
        }
        for (Map.Entry<FeedDescriptor, List<IFeedMessage>> entry : map.entrySet()) {
            List<IFeedMessage> list = entry.getValue();
            Collections.sort(list, new Comparator<IFeedMessage>() {
                @Override
                public int compare(IFeedMessage lhs, IFeedMessage rhs) {
                    return rhs.getDate().compareTo(lhs.getDate());
                }
            });
            entry.setValue(list);
        }
        return map;
    }

}
