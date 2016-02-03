/**
 * Copyright (c) 2015 Pawel Nowak.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.recommenders.internal.news.rcp;

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.time.DateUtils;
import org.eclipse.recommenders.news.rcp.IFeedMessage;
import org.eclipse.recommenders.news.rcp.IPollingResult;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class MessageUtils {

    public enum MessageAge {
        TODAY(0),
        YESTERDAY(1),
        THIS_WEEK(2),
        LAST_WEEK(3),
        THIS_MONTH(4),
        LAST_MONTH(5),
        THIS_YEAR(6),
        OLDER(7),
        UNDETERMINED(8);

        private final int index;

        MessageAge(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }

    public static boolean containsUnreadMessages(Map<FeedDescriptor, IPollingResult> map) {
        if (map == null) {
            return false;
        }
        for (Map.Entry<FeedDescriptor, IPollingResult> entry : map.entrySet()) {
            for (IFeedMessage message : entry.getValue().getMessages()) {
                if (!message.isRead()) {
                    return true;
                }
            }
        }
        return false;
    }

    public static Map<FeedDescriptor, IPollingResult> getLatestMessages(Map<FeedDescriptor, IPollingResult> messages) {
        Preconditions.checkNotNull(messages);
        Map<FeedDescriptor, IPollingResult> result = Maps.newHashMap();
        for (Entry<FeedDescriptor, IPollingResult> entry : messages.entrySet()) {
            List<IFeedMessage> list = updateMessages(entry);
            if (!list.isEmpty()) {
                result.put(entry.getKey(), new PollingResult(entry.getValue().getStatus(), list));
            }
        }
        return sortByDate(result);
    }

    public static List<IFeedMessage> updateMessages(Entry<FeedDescriptor, IPollingResult> entry) {
        NewsProperties properties = new NewsProperties();
        List<IFeedMessage> feedMessages = Lists.newArrayList();
        for (IFeedMessage message : entry.getValue().getMessages()) {
            if (properties.getDates(Constants.FILENAME_FEED_DATES).get(entry.getKey().getId()) == null) {
                feedMessages.add(message);
            } else if (message.getDate() != null && message.getDate()
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

    public static List<IFeedMessage> mergeMessages(Map<FeedDescriptor, IPollingResult> messages) {
        if (messages == null) {
            return Collections.emptyList();
        }
        List<IFeedMessage> result = Lists.newArrayList();
        for (Map.Entry<FeedDescriptor, IPollingResult> entry : messages.entrySet()) {
            result.addAll(entry.getValue().getMessages());
        }
        return result;
    }

    public static Map<FeedDescriptor, IPollingResult> sortByDate(Map<FeedDescriptor, IPollingResult> map) {
        if (map == null) {
            return Maps.newHashMap();
        }
        for (Map.Entry<FeedDescriptor, IPollingResult> entry : map.entrySet()) {
            List<IFeedMessage> list = entry.getValue().getMessages();
            Collections.sort(list, new Comparator<IFeedMessage>() {
                @Override
                public int compare(IFeedMessage lhs, IFeedMessage rhs) {
                    if (rhs.getDate() == null || lhs.getDate() == null) {
                        return 0;
                    }
                    return rhs.getDate().compareTo(lhs.getDate());
                }
            });
            entry.setValue(new PollingResult(entry.getValue().getStatus(), list));
        }
        return map;
    }

    public static List<List<IFeedMessage>> splitMessagesByAge(List<IFeedMessage> messages) {
        Locale locale = Locale.getDefault();
        Calendar calendar = Calendar.getInstance(locale);
        return splitMessagesByAge(messages, calendar.getTime(), locale);
    }

    @VisibleForTesting
    public static List<List<IFeedMessage>> splitMessagesByAge(List<IFeedMessage> messages, Date now, Locale locale) {
        List<List<IFeedMessage>> result = Lists.newArrayList();
        for (int i = 0; i < MessageAge.values().length; i++) {
            List<IFeedMessage> list = Lists.newArrayList();
            result.add(list);
        }

        if (messages == null) {
            return result;
        }
        Date today = DateUtils.truncate(now, Calendar.DAY_OF_MONTH);
        for (IFeedMessage message : messages) {
            for (MessageAge messageAge : MessageAge.values()) {
                if (message.getDate() == null) {
                    result.get(MessageAge.UNDETERMINED.getIndex()).add(message);
                    break;
                }
                if (message.getDate().after(getPeriodStartDate(messageAge, today, locale))
                        || message.getDate().equals(getPeriodStartDate(messageAge, today, locale))) {
                    result.get(messageAge.getIndex()).add(message);
                    break;
                }
            }
            if (message.getDate() != null
                    && message.getDate().before(getPeriodStartDate(MessageAge.OLDER, today, locale))) {
                result.get(MessageAge.OLDER.getIndex()).add(message);
            }
        }
        return result;
    }

    public static Date getPeriodStartDate(MessageAge messageAge, Date today, Locale locale) {
        Calendar calendar = GregorianCalendar.getInstance(locale);
        calendar.setTime(today);
        if (messageAge == MessageAge.TODAY) {
            return calendar.getTime();
        } else if (messageAge == MessageAge.YESTERDAY) {
            calendar.add(Calendar.DATE, -1);
        } else if (messageAge == MessageAge.THIS_WEEK) {
            int firstDayOfWeek = calendar.getFirstDayOfWeek();
            calendar.set(Calendar.DAY_OF_WEEK, firstDayOfWeek);
        } else if (messageAge == MessageAge.LAST_WEEK) {
            int firstDayOfWeek = calendar.getFirstDayOfWeek();
            calendar.set(Calendar.DAY_OF_WEEK, firstDayOfWeek);
            calendar.add(Calendar.DATE, -1);
            calendar.set(Calendar.DAY_OF_WEEK, firstDayOfWeek);
        } else if (messageAge == MessageAge.THIS_MONTH) {
            calendar.set(Calendar.DAY_OF_MONTH, 1);
        } else if (messageAge == MessageAge.LAST_MONTH) {
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.add(Calendar.DATE, -1);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
        } else if (messageAge == MessageAge.THIS_YEAR) {
            calendar.set(Calendar.MONTH, 0);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
        } else if (messageAge == MessageAge.OLDER) {
            calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) - 1);
            calendar.set(Calendar.MONTH, 0);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
        }
        return calendar.getTime();
    }

}
