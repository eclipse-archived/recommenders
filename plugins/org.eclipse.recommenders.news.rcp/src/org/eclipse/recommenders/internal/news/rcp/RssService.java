/**
 * Copyright (c) 2015 Pawel Nowak.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.recommenders.internal.news.rcp;

import static java.lang.Long.parseLong;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.mylyn.commons.notifications.core.NotificationEnvironment;
import org.eclipse.mylyn.internal.commons.notifications.feed.FeedEntry;
import org.eclipse.mylyn.internal.commons.notifications.feed.FeedReader;
import org.eclipse.recommenders.news.rcp.IFeedMessage;
import org.eclipse.recommenders.news.rcp.IRssService;
import org.eclipse.recommenders.utils.Urls;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;

@SuppressWarnings("restriction")
public class RssService implements IRssService {

    private static final long DEFAULT_DELAY = TimeUnit.DAYS.toMinutes(1);
    private static final long START_DELAY = 0;

    private final NewsRcpPreferences preferences;
    private final EventBus bus;
    private final NotificationEnvironment environment;

    private final HashMap<FeedDescriptor, List<IFeedMessage>> groupedMessages = Maps.newHashMap();

    public RssService(NewsRcpPreferences preferences, EventBus bus, NotificationEnvironment environment) {
        this.preferences = preferences;
        this.bus = bus;
        this.environment = environment;
    }

    @Override
    public void start() {
        for (final FeedDescriptor feed : preferences.getEnabledFeedDescriptors()) {
            start(feed);
        }
    }

    @Override
    public void start(final FeedDescriptor feed) {
        final Job messageCheckJob = new Job(feed.getId()) {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    pollFeed(monitor, feed);
                    return Status.OK_STATUS;
                } catch (Throwable t) {
                    // fail silently
                    return Status.CANCEL_STATUS;
                }
            }

            @Override
            public boolean shouldRun() {
                if (!preferences.isEnabled() || !isFeedEnabled(feed)) {
                    return false;
                }
                return true;
            }

        };
        messageCheckJob.setSystem(true);
        messageCheckJob.setPriority(Job.DECORATE);
        messageCheckJob.addJobChangeListener(new JobChangeAdapter() {
            @Override
            public void done(IJobChangeEvent event) {
                if (!preferences.isEnabled() || !isFeedEnabled(feed)) {
                    return;
                }
                if (feed.getPollingInterval() != null) {
                    messageCheckJob.schedule(TimeUnit.MINUTES.toMillis(parseLong(feed.getPollingInterval())));
                    return;
                }
                messageCheckJob.schedule(TimeUnit.MINUTES.toMillis(DEFAULT_DELAY));
            }
        });
        messageCheckJob.schedule(START_DELAY);
    }

    private int pollFeed(IProgressMonitor monitor, FeedDescriptor feed) {
        int status = -1;
        boolean newMessage = false;
        try {
            HttpURLConnection connection = (HttpURLConnection) Urls.toUrl(feed.getUrl()).openConnection();
            try {
                connection.connect();
                status = connection.getResponseCode();
                if (status == HttpURLConnection.HTTP_OK && !monitor.isCanceled()) {

                    InputStream in = new BufferedInputStream(connection.getInputStream());
                    try {
                        if (!groupedMessages.containsKey(feed)) {
                            groupedMessages.put(feed, Lists.<IFeedMessage>newArrayList());
                        }
                        List<IFeedMessage> feedMessages = groupedMessages.get(feed);
                        for (IFeedMessage message : readMessages(in, monitor, feed.getId())) {
                            if (!feedMessages.contains(message)) {
                                feedMessages.add(message);
                                newMessage = true;
                            }
                        }
                    } finally {
                        in.close();
                    }
                }
            } finally {
                connection.disconnect();
            }
        } catch (Exception e) {
            // fail silently
        }

        if (groupedMessages.size() > 0 && newMessage) {
            bus.post(new NewFeedItemsEvent());
        }
        return status;
    }

    private List<? extends IFeedMessage> readMessages(InputStream in, IProgressMonitor monitor, String eventId)
            throws IOException {
        FeedReader reader = new FeedReader(eventId, environment);
        reader.parse(in, monitor);
        return FluentIterable.from(reader.getEntries()).transform(new Function<FeedEntry, IFeedMessage>() {

            @Override
            public IFeedMessage apply(FeedEntry entry) {
                return new FeedMessage(entry.getId(), entry.getDate(), entry.getDescription(), entry.getTitle(), Urls
                        .toUrl(entry.getUrl()));
            }
        }).toList();
    }

    @Override
    public Map<FeedDescriptor, List<IFeedMessage>> getMessages(final int countPerFeed) {
        return ImmutableMap.copyOf(Maps.transformValues(groupedMessages,
                new Function<List<IFeedMessage>, List<IFeedMessage>>() {

                    @Override
                    public List<IFeedMessage> apply(List<IFeedMessage> input) {
                        return FluentIterable.from(input).limit(countPerFeed).toList();
                    }
                }));
    }

    private boolean isFeedEnabled(FeedDescriptor feed) {
        for (FeedDescriptor fd : preferences.getEnabledFeedDescriptors()) {
            if (feed.getId().equals(fd.getId())) {
                return true;
            }
        }
        return false;
    }

}
