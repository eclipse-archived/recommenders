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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class MessageUtils {

    public static final int TODAY = 0;
    public static final int YESTERDAY = 1;
    public static final int THIS_WEEK = 2;
    public static final int LAST_WEEK = 3;
    public static final int THIS_MONTH = 4;
    public static final int LAST_MONTH = 5;
    public static final int THIS_YEAR = 6;
    public static final int OLDER = 7;

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
        return sortByDate(result);
    }

    public static List<IFeedMessage> updateMessages(Entry<FeedDescriptor, List<IFeedMessage>> entry) {
        NewsFeedProperties properties = new NewsFeedProperties();
        List<IFeedMessage> feedMessages = Lists.newArrayList();
        for (IFeedMessage message : entry.getValue()) {
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

    public static List<IFeedMessage> mergeMessages(Map<FeedDescriptor, List<IFeedMessage>> messages) {
        if (messages == null) {
            return Collections.emptyList();
        }
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
                    if (rhs.getDate() == null || lhs.getDate() == null) {
                        return 0;
                    }
                    return rhs.getDate().compareTo(lhs.getDate());
                }
            });
            entry.setValue(list);
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
        for (int i = 0; i <= OLDER; i++) {
            List<IFeedMessage> list = Lists.newArrayList();
            result.add(list);
        }
        if (messages == null) {
            return result;
        }
        Date today = DateUtils.truncate(now, Calendar.DAY_OF_MONTH);
        for (IFeedMessage message : messages) {
            for (int i = 0; i <= OLDER; i++) {
                if (message.getDate() == null) {
                    result.get(OLDER).add(message);
                    break;
                }
                if (message.getDate().after(getPeriodStartDate(i, today, locale))
                        || message.getDate().equals(getPeriodStartDate(i, today, locale))) {
                    result.get(i).add(message);
                    break;
                }
            }
            if (message.getDate() != null && (message.getDate().before(getPeriodStartDate(OLDER, today, locale))
                    || message.getDate().equals(getPeriodStartDate(OLDER, today, locale)))) {
                result.get(OLDER).add(message);
            }
        }
        return result;
    }

    public static Date getPeriodStartDate(int period, Date today, Locale locale) {
        Calendar calendar = GregorianCalendar.getInstance(locale);
        calendar.setTime(today);
        if (period == TODAY) {
            return calendar.getTime();
        } else if (period == YESTERDAY) {
            calendar.add(Calendar.DATE, -1);
        } else if (period == THIS_WEEK) {
            int firstDayOfWeek = calendar.getFirstDayOfWeek();
            calendar.set(Calendar.DAY_OF_WEEK, firstDayOfWeek);
        } else if (period == LAST_WEEK) {
            int firstDayOfWeek = calendar.getFirstDayOfWeek();
            calendar.set(Calendar.DAY_OF_WEEK, firstDayOfWeek);
            calendar.add(Calendar.DATE, -1);
            calendar.set(Calendar.DAY_OF_WEEK, firstDayOfWeek);
        } else if (period == THIS_MONTH) {
            calendar.set(Calendar.DAY_OF_MONTH, 1);
        } else if (period == LAST_MONTH) {
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.add(Calendar.DATE, -1);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
        } else if (period == THIS_YEAR) {
            calendar.set(Calendar.MONTH, 0);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
        } else if (period == OLDER) {
            calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) - 1);
            calendar.set(Calendar.MONTH, 11);
            calendar.set(Calendar.DAY_OF_MONTH, 31);
        }
        return calendar.getTime();
    }

}
