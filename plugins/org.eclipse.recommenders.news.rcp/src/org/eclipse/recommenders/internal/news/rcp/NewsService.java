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

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.recommenders.internal.news.rcp.FeedEvents.AllReadEvent;
import org.eclipse.recommenders.internal.news.rcp.FeedEvents.FeedMessageReadEvent;
import org.eclipse.recommenders.internal.news.rcp.FeedEvents.FeedReadEvent;
import org.eclipse.recommenders.news.rcp.IFeedMessage;
import org.eclipse.recommenders.news.rcp.IJobFacade;
import org.eclipse.recommenders.news.rcp.INewsProperties;
import org.eclipse.recommenders.news.rcp.INewsService;
import org.eclipse.recommenders.news.rcp.INotificationFacade;
import org.eclipse.recommenders.news.rcp.IPollFeedJob;
import org.eclipse.recommenders.news.rcp.IPollingResult;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

@Creatable
@Singleton
public class NewsService implements INewsService {

    private final NewsRcpPreferences preferences;
    private final INewsProperties newsFeedProperties;
    private final Set<String> readIds;
    private final IJobFacade jobFacade;
    private final EventBus bus;
    private final INotificationFacade notificationFacade;
    private final Map<FeedDescriptor, IPollingResult> groupedMessages = new HashMap<>();

    @VisibleForTesting
    public NewsService(NewsRcpPreferences preferences, EventBus bus, INewsProperties newsFeedProperties,
            IJobFacade jobFacade, INotificationFacade notificationFacade) {
        this.preferences = preferences;
        bus.register(this);
        this.bus = bus;
        readIds = newsFeedProperties.getReadIds();
        this.newsFeedProperties = newsFeedProperties;
        this.jobFacade = jobFacade;
        this.notificationFacade = notificationFacade;
    }

    @Inject
    public NewsService(NewsRcpPreferences preferences, INewsProperties newsProperties, IJobFacade jobFacade,
            INotificationFacade notificationFacade) {
        this(preferences, NewsRcpModule.EVENT_BUS, newsProperties, jobFacade, notificationFacade);
    }

    @Override
    public void start() {
        if (!isRealEclipse()) {
            return;
        }
        doStart();
    }

    @VisibleForTesting
    protected void doStart() {
        if (!preferences.isEnabled()) {
            return;
        }
        jobFacade.schedulePollFeeds(this, getFeedsToPoll(true));
    }

    @Override
    public Map<FeedDescriptor, IPollingResult> getMessages(final int countPerFeed) {
        Map<FeedDescriptor, IPollingResult> transformedMap = Maps.transformValues(groupedMessages,
                new Function<IPollingResult, IPollingResult>() {

                    @Override
                    public PollingResult apply(IPollingResult input) {
                        ImmutableList<IFeedMessage> list = FluentIterable.from(input.getMessages()).limit(countPerFeed)
                                .toList();
                        for (IFeedMessage message : list) {
                            if (readIds.contains(message.getId())) {
                                message.setRead(true);
                            }
                        }
                        return new PollingResult(input.getStatus(), list);
                    }
                });
        return transformedMap;
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
        List<IFeedMessage> messages = groupedMessages.get(event.getFeed()).getMessages();
        for (IFeedMessage message : messages) {
            readIds.add(message.getId());
        }
        newsFeedProperties.writeReadIds(readIds);
    }

    @Subscribe
    @Override
    public void handleAllRead(AllReadEvent event) {
        for (Map.Entry<FeedDescriptor, IPollingResult> entry : groupedMessages.entrySet()) {
            for (IFeedMessage message : entry.getValue().getMessages()) {
                readIds.add(message.getId());
            }
        }
        newsFeedProperties.writeReadIds(readIds);
    }

    @Override
    public void jobDone(IPollFeedJob job) {
        boolean newMessage = false;
        Map<FeedDescriptor, IPollingResult> messages = job.getMessages();
        for (Map.Entry<FeedDescriptor, IPollingResult> entry : messages.entrySet()) {
            if (!groupedMessages.containsKey(entry.getKey())) {
                groupedMessages.put(entry.getKey(), entry.getValue());
                if (!entry.getValue().getMessages().isEmpty()) {
                    newMessage = true;
                }
            }
            List<IFeedMessage> feedMessages = groupedMessages.get(entry.getKey()).getMessages();
            for (IFeedMessage message : entry.getValue().getMessages()) {
                if (!feedMessages.contains(message)) {
                    feedMessages.add(message);
                    newMessage = true;
                }
            }
            groupedMessages.put(entry.getKey(), new PollingResult(entry.getValue().getStatus(), feedMessages));
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

    private void updateReadIds() {
        Set<String> result = Sets.newHashSet();
        Set<String> allMessages = Sets.newHashSet();
        for (Map.Entry<FeedDescriptor, IPollingResult> entry : groupedMessages.entrySet()) {
            for (IFeedMessage message : entry.getValue().getMessages()) {
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
        Map<FeedDescriptor, IPollingResult> messages = MessageUtils
                .getLatestMessages(getMessages(Constants.COUNT_PER_FEED));
        if (!messages.isEmpty()) {
            notificationFacade.displayNotification(messages, bus);
            Map<FeedDescriptor, Date> feedDates = Maps.newHashMap();
            for (Map.Entry<FeedDescriptor, IPollingResult> entry : messages.entrySet()) {
                feedDates.put(entry.getKey(), Calendar.getInstance().getTime());
            }
            newsFeedProperties.writeDates(feedDates, Constants.FILENAME_FEED_DATES);
        }
    }

    /**
     * A real Eclipse is an Eclipse run by a user. A non-real Eclipse is one executed by tests. Use this method to
     * prevent execution of code during tests.
     *
     * @return <code>true</code> when the eclipse.build system property is set, <code>false</code>otherwise.
     */
    public static boolean isRealEclipse() {
        return !Strings.isNullOrEmpty(System.getProperty(Constants.SYSPROP_ECLIPSE_BUILD_ID));
    }
}
