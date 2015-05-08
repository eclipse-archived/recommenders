/**
 * Copyright (c) 2015 Pawel Nowak.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.recommenders.internal.news.rcp;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.mylyn.commons.notifications.core.NotificationEnvironment;
import org.eclipse.mylyn.internal.commons.notifications.feed.FeedEntry;
import org.eclipse.mylyn.internal.commons.notifications.feed.FeedReader;
import org.eclipse.recommenders.news.rcp.IFeedMessage;
import org.eclipse.recommenders.utils.Urls;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;

@SuppressWarnings("restriction")
public class PollFeedJob extends Job {
    private final NewsRcpPreferences preferences;
    private final NotificationEnvironment environment;
    private List<? extends IFeedMessage> messages = Lists.newArrayList();
    private final FeedDescriptor feed;

    public PollFeedJob(FeedDescriptor feed, NewsRcpPreferences preferences, NotificationEnvironment environment) {
        super(feed.getId());
        Preconditions.checkNotNull(feed);
        Preconditions.checkNotNull(preferences);
        Preconditions.checkNotNull(environment);
        this.feed = feed;
        this.preferences = preferences;
        this.environment = environment;
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        int status = -1;
        try {
            HttpURLConnection connection = (HttpURLConnection) Urls.toUrl(feed.getUrl()).openConnection();
            try {
                connection.connect();
                status = connection.getResponseCode();
                if (status == HttpURLConnection.HTTP_OK && !monitor.isCanceled()) {

                    InputStream in = new BufferedInputStream(connection.getInputStream());
                    try {
                        messages = Lists.newArrayList(readMessages(in, monitor, feed.getId()));
                    } finally {
                        in.close();
                    }
                }
            } finally {
                connection.disconnect();
            }
        } catch (Exception e) {
            return Status.CANCEL_STATUS;
        }
        return Status.OK_STATUS;
    }

    @Override
    public boolean belongsTo(Object job) {
        if (job == null) {
            return false;
        }
        if (!(job instanceof PollFeedJob)) {
            return false;
        }
        PollFeedJob rhs = (PollFeedJob) job;
        if (!feed.getId().equals(rhs.getFeed().getId())) {
            return false;
        }
        return true;
    }

    @Override
    public boolean shouldRun() {
        if (!preferences.isEnabled() || !isFeedEnabled(feed)) {
            return false;
        }
        return true;
    }

    private boolean isFeedEnabled(FeedDescriptor feed) {
        for (FeedDescriptor fd : preferences.getFeedDescriptors()) {
            if (feed.getId().equals(fd.getId())) {
                return fd.isEnabled();
            }
        }
        return false;
    }

    private List<? extends IFeedMessage> readMessages(InputStream in, IProgressMonitor monitor, String eventId)
            throws IOException {
        FeedReader reader = new FeedReader(eventId, environment);
        reader.parse(in, monitor);
        return FluentIterable.from(reader.getEntries()).transform(new Function<FeedEntry, IFeedMessage>() {

            @Override
            public IFeedMessage apply(FeedEntry entry) {
                return new FeedMessage(entry.getId(), entry.getDate(), entry.getDescription(), entry.getTitle(),
                        Urls.toUrl(entry.getUrl()));
            }
        }).toList();
    }

    public List<? extends IFeedMessage> getMessages() {
        return messages;
    }

    public FeedDescriptor getFeed() {
        return feed;
    }
}
