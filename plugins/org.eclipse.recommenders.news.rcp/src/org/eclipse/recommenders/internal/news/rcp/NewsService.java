/**
 * Copyright (c) 2015 Pawel Nowak.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.recommenders.internal.news.rcp;

import static org.eclipse.recommenders.internal.news.rcp.FeedEvents.createNewFeedItemsEvent;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.eclipse.recommenders.internal.news.rcp.FeedEvents.AllReadEvent;
import org.eclipse.recommenders.internal.news.rcp.FeedEvents.FeedMessageReadEvent;
import org.eclipse.recommenders.internal.news.rcp.FeedEvents.FeedReadEvent;
import org.eclipse.recommenders.news.rcp.IFeedMessage;
import org.eclipse.recommenders.news.rcp.IJobFacade;
import org.eclipse.recommenders.news.rcp.INewsFeedProperties;
import org.eclipse.recommenders.news.rcp.INewsService;
import org.eclipse.recommenders.news.rcp.INotificationFacade;
import org.eclipse.recommenders.news.rcp.IPollFeedJob;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class NewsService implements INewsService {

    private final NewsRcpPreferences preferences;
    private final INewsFeedProperties newsFeedProperties;
    private final Set<String> readIds;
    private final IJobFacade jobFacade;
    private final EventBus bus;
    private final INotificationFacade notificationFacade;
    private final HashMap<FeedDescriptor, List<IFeedMessage>> groupedMessages = Maps.newHashMap();

    public NewsService(NewsRcpPreferences preferences, EventBus bus, INewsFeedProperties newsFeedProperties,
            IJobFacade jobFacade, INotificationFacade notificationFacade) {
        this.preferences = preferences;
        bus.register(this);
        this.bus = bus;
        readIds = newsFeedProperties.getReadIds();
        this.newsFeedProperties = newsFeedProperties;
        this.jobFacade = jobFacade;
        this.notificationFacade = notificationFacade;
    }

    @Override
    public void start() {
        if (!preferences.isEnabled()) {
            return;
        }
        jobFacade.schedulePollFeeds(this, getFeedsToPoll(true));
    }

    @Override
    public Map<FeedDescriptor, List<IFeedMessage>> getMessages(final int countPerFeed) {
        Map<FeedDescriptor, List<IFeedMessage>> transformedMap = Maps.transformValues(groupedMessages,
                new Function<List<IFeedMessage>, List<IFeedMessage>>() {

                    @Override
                    public List<IFeedMessage> apply(List<IFeedMessage> input) {
                        ImmutableList<IFeedMessage> list = FluentIterable.from(input).limit(countPerFeed).toList();
                        for (IFeedMessage message : list) {
                            if (readIds.contains(message.getId())) {
                                message.setRead(true);
                            }
                        }
                        return list;
                    }
                });
        return Maps.filterValues(transformedMap, new Predicate<List<IFeedMessage>>() {

            @Override
            public boolean apply(List<IFeedMessage> input) {
                if (input == null) {
                    return false;
                }
                return !input.isEmpty();
            }

        });
    }

    @Subscribe
    @Override
    public void handleMessageRead(FeedMessageReadEvent event) {
        if (event.getId() != null) {
            readIds.add(event.getId());
            newsFeedProperties.writeReadIds(readIds);
        }
    }

    @Subscribe
    @Override
    public void handleFeedRead(FeedReadEvent event) {
        List<IFeedMessage> messages = groupedMessages.get(event.getFeed());
        for (IFeedMessage message : messages) {
            readIds.add(message.getId());
        }
        newsFeedProperties.writeReadIds(readIds);
    }

    @Subscribe
    @Override
    public void handleAllRead(AllReadEvent event) {
        for (Map.Entry<FeedDescriptor, List<IFeedMessage>> entry : groupedMessages.entrySet()) {
            for (IFeedMessage message : entry.getValue()) {
                readIds.add(message.getId());
            }
        }
        newsFeedProperties.writeReadIds(readIds);
    }

    @Override
    public void jobDone(IPollFeedJob job) {
        boolean newMessage = false;
        Map<FeedDescriptor, List<IFeedMessage>> messages = job.getMessages();
        for (Map.Entry<FeedDescriptor, List<IFeedMessage>> entry : messages.entrySet()) {
            if (!groupedMessages.containsKey(entry.getKey())) {
                groupedMessages.put(entry.getKey(), entry.getValue());
                if (!entry.getValue().isEmpty()) {
                    newMessage = true;
                }
            }

            for (IFeedMessage message : entry.getValue()) {
                if (!groupedMessages.get(entry.getKey()).contains(message)) {
                    groupedMessages.get(entry.getKey()).add(message);
                    newMessage = true;
                }
            }
        }
        if (!groupedMessages.isEmpty() && newMessage) {
            bus.post(createNewFeedItemsEvent());
            newsFeedProperties.writeDates(job.getPollDates(), Constants.FILENAME_POLL_DATES);
            updateReadIds();
        }

        if (!preferences.isEnabled()) {
            return;
        }
        jobFacade.scheduleNewsUpdate(this, TimeUnit.MINUTES.toMillis(preferences.getPollingInterval()));
        displayNotification();
    }

    @VisibleForTesting
    protected boolean shouldPoll(FeedDescriptor feed, boolean override) {
        if (!feed.isEnabled()) {
            return false;
        }
        if (override) {
            return true;
        }
        int pollingInterval = preferences.getPollingInterval().intValue();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, pollingInterval);
        Date lhs = calendar.getTime();
        for (Map.Entry<String, Date> entry : newsFeedProperties.getDates(Constants.FILENAME_POLL_DATES).entrySet()) {
            if (entry.getKey().equals(feed.getId())) {
                if (entry.getValue().after(lhs)) {
                    return false;
                }
            }
        }
        return true;
    }

    private Set<FeedDescriptor> getFeedsToPoll(boolean override) {
        Set<FeedDescriptor> feeds = Sets.newHashSet();
        for (final FeedDescriptor feed : preferences.getFeedDescriptors()) {
            if (shouldPoll(feed, override)) {
                feeds.add(feed);
            }
        }
        return feeds;
    }

    @Override
    public void removeFeed(FeedDescriptor feed) {
        if (groupedMessages.containsKey(feed)) {
            groupedMessages.remove(feed);
        }
    }

    @Override
    public void pollFeeds() {
        jobFacade.schedulePollFeeds(this, getFeedsToPoll(false));
    }

    @Override
    public void forceStop() {
        jobFacade.cancelPollFeeds();
    }

    @Override
    public void updateFeedDates(Map<FeedDescriptor, Date> map) {
        newsFeedProperties.writeDates(map, Constants.FILENAME_FEED_DATES);
    }

    private void updateReadIds() {
        Set<String> result = Sets.newHashSet();
        Set<String> allMessages = Sets.newHashSet();
        for (Map.Entry<FeedDescriptor, List<IFeedMessage>> entry : groupedMessages.entrySet()) {
            for (IFeedMessage message : entry.getValue()) {
                allMessages.add(message.getId());
            }
        }
        for (String s : readIds) {
            if (allMessages.contains(s)) {
                result.add(s);
            }
        }
        readIds.clear();
        readIds.addAll(result);
    }

    @Override
    public void displayNotification() {
        Map<FeedDescriptor, List<IFeedMessage>> messages = MessageUtils
                .getLatestMessages(getMessages(Constants.COUNT_PER_FEED));
        if (preferences.isNotificationEnabled() && !messages.isEmpty()) {
            notificationFacade.displayNotification(messages, bus);
            Map<FeedDescriptor, Date> feedDates = Maps.newHashMap();
            for (Map.Entry<FeedDescriptor, List<IFeedMessage>> entry : messages.entrySet()) {
                Date date = new Date();
                feedDates.put(entry.getKey(), date);
            }
            updateFeedDates(feedDates);
        }
    }
}
