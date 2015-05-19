/**
 * Copyright (c) 2015 Pawel Nowak.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.recommenders.internal.news.rcp;

import static java.lang.Long.parseLong;
import static org.eclipse.recommenders.internal.news.rcp.FeedEvents.createNewFeedItemsEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.eclipse.mylyn.commons.notifications.core.NotificationEnvironment;
import org.eclipse.recommenders.internal.news.rcp.FeedEvents.FeedMessageReadEvent;
import org.eclipse.recommenders.news.rcp.IFeedMessage;
import org.eclipse.recommenders.news.rcp.IRssService;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

@SuppressWarnings("restriction")
public class RssService implements IRssService {

    private final NewsRcpPreferences preferences;
    private final EventBus bus;
    private final NotificationEnvironment environment;
    private final JobProvider provider;

    private final Set<String> readIds;

    private final HashMap<FeedDescriptor, List<IFeedMessage>> groupedMessages = Maps.newHashMap();

    public RssService(NewsRcpPreferences preferences, EventBus bus, NotificationEnvironment environment,
            JobProvider provider) {
        this.preferences = preferences;
        this.bus = bus;
        this.environment = environment;
        this.provider = provider;
        bus.register(this);

        readIds = ReadFeedMessagesProperties.getReadIds();
    }

    @Override
    public void start() {
        if (!preferences.isEnabled()) {
            return;
        }
        for (final FeedDescriptor feed : preferences.getFeedDescriptors()) {
            if (feed.isEnabled()) {
                start(feed);
            }
        }
    }

    @Override
    public void start(final FeedDescriptor feed) {
        final PollFeedJob job = provider.getPollFeedJob(feed, preferences, environment, this);
        provider.schedule(job);
    }

    @Override
    public Map<FeedDescriptor, List<IFeedMessage>> getMessages(final int countPerFeed) {
        Map<FeedDescriptor, List<IFeedMessage>> transformedMap = Maps.transformValues(groupedMessages,
                new Function<List<IFeedMessage>, List<IFeedMessage>>() {

                    @Override
                    public List<IFeedMessage> apply(List<IFeedMessage> input) {
                        return FluentIterable.from(input).limit(countPerFeed).filter(new Predicate<IFeedMessage>() {

                            @Override
                            public boolean apply(IFeedMessage input) {
                                return !readIds.contains(input.getId());
                            }
                        }).toList();
                    }
                });
        Map<FeedDescriptor, List<IFeedMessage>> filteredMap = Maps.filterValues(transformedMap,
                new Predicate<List<IFeedMessage>>() {

                    @Override
                    public boolean apply(List<IFeedMessage> input) {
                        if (input == null) {
                            return false;
                        }
                        return !input.isEmpty();
                    }

                });
        return ImmutableMap.copyOf(filteredMap);
    }

    private boolean isFeedEnabled(FeedDescriptor feed) {
        for (FeedDescriptor fd : preferences.getFeedDescriptors()) {
            if (feed.getId().equals(fd.getId())) {
                return true;
            }
        }
        return false;
    }

    public void jobDone(FeedDescriptor feed, PollFeedJob job) {
        boolean newMessage = false;
        if (!groupedMessages.containsKey(feed)) {
            groupedMessages.put(feed, Lists.<IFeedMessage>newArrayList());
        }
        List<IFeedMessage> feedMessages = groupedMessages.get(feed);
        for (IFeedMessage message : job.getMessages()) {
            if (!feedMessages.contains(message)) {
                feedMessages.add(message);
                if (!readIds.contains(message.getId())) {
                    newMessage = true;
                }
            }
        }

        if (groupedMessages.size() > 0 && newMessage) {
            bus.post(createNewFeedItemsEvent());
        }

        if (!preferences.isEnabled() || !isFeedEnabled(feed)) {
            return;
        }
        if (feed.getPollingInterval() != null) {
            job.schedule(TimeUnit.MINUTES.toMillis(parseLong(feed.getPollingInterval())));
            return;
        }
        provider.schedule(job);
    }

    @Subscribe
    public void handle(FeedMessageReadEvent event) {
        readIds.add(event.getId());
        ReadFeedMessagesProperties.writeReadIds(readIds);
    }

    @Override
    public void removeFeed(FeedDescriptor feed) {
        if (groupedMessages.containsKey(feed)) {
            groupedMessages.remove(feed);
        }
    }
}
