/**
 * Copyright (c) 2015 Pawel Nowak.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.recommenders.internal.news.rcp;

import static org.eclipse.recommenders.internal.news.rcp.Proxies.*;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.mylyn.commons.notifications.core.NotificationEnvironment;
import org.eclipse.mylyn.internal.commons.notifications.feed.FeedEntry;
import org.eclipse.mylyn.internal.commons.notifications.feed.FeedReader;
import org.eclipse.recommenders.internal.news.rcp.l10n.LogMessages;
import org.eclipse.recommenders.internal.news.rcp.l10n.Messages;
import org.eclipse.recommenders.news.rcp.IFeedMessage;
import org.eclipse.recommenders.news.rcp.IPollFeedJob;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

@SuppressWarnings("restriction")
public class PollFeedJob extends Job implements IPollFeedJob {
    private final NotificationEnvironment environment;
    private final Map<FeedDescriptor, PollingResult> groupedMessages = Maps.newHashMap();
    private final Set<FeedDescriptor> feeds = Sets.newHashSet();
    private final Map<FeedDescriptor, Date> pollDates = Maps.newHashMap();

    public PollFeedJob(Collection<FeedDescriptor> feeds) {
        super(Messages.POLL_FEED_JOB_NAME);
        Preconditions.checkNotNull(feeds);
        this.environment = new NotificationEnvironment();
        this.feeds.addAll(feeds);
        setPriority(Job.DECORATE);
        setRule(new MutexRule());
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        SubMonitor sub = SubMonitor.convert(monitor, feeds.size() * 100);
        try {
            Executor executor = Executor.newInstance();
            for (FeedDescriptor feed : feeds) {
                if (monitor.isCanceled() || FrameworkUtil.getBundle(this.getClass()).getState() != Bundle.ACTIVE) {
                    return Status.CANCEL_STATUS;
                }
                pollFeed(monitor, sub, executor, feed);
            }
            return Status.OK_STATUS;
        } finally {
            monitor.done();
        }
    }

    private void pollFeed(IProgressMonitor monitor, SubMonitor sub, Executor executor, FeedDescriptor feed) {
        try {
            URI feedUri = urlToUri(feed.getUrl()).orNull();
            if (feedUri != null) {
                Response response = connectToUrl(feed, executor, feedUri);
                sub.worked(10);
                updateGroupedMessages(response.returnResponse(), feed, sub.newChild(80));
                pollDates.put(feed, new Date());
                sub.worked(10);
            } else {
                Logs.log(LogMessages.WARNING_CONNECTING_URL, feed.getUrl());
                groupedMessages.put(feed, PollingResult.newConnectionErrorResult());
            }
        } catch (IOException e) {
            Logs.log(LogMessages.WARNING_CONNECTING_URL, feed.getUrl());
            groupedMessages.put(feed, PollingResult.newConnectionErrorResult());
        }
    }

    private Response connectToUrl(FeedDescriptor feed, Executor executor, URI feedUri)
            throws ClientProtocolException, IOException {
        Request request = Request.Get(feedUri).viaProxy(getProxyHost(feedUri).orNull())
                .connectTimeout((int) Constants.CONNECTION_TIMEOUT).staleConnectionCheck(true)
                .socketTimeout((int) Constants.SOCKET_TIMEOUT);
        Response response = proxyAuthentication(executor, feedUri).execute(request);
        return response;
    }

    @Override
    public boolean belongsTo(Object job) {
        return Objects.equals(Constants.POLL_FEED_JOB_FAMILY, job);
    }

    private void updateGroupedMessages(HttpResponse httpResponse, FeedDescriptor feed, IProgressMonitor monitor) {
        monitor.subTask(MessageFormat.format(Messages.POLL_FEED_JOB_SUBTASK_POLLING_FEED, feed.getName()));
        try {
            if (monitor.isCanceled() || FrameworkUtil.getBundle(this.getClass()).getState() != Bundle.ACTIVE) {
                return;
            }
            if (httpResponse.getStatusLine().getStatusCode() >= HttpStatus.SC_BAD_REQUEST) {
                Logs.log(LogMessages.ERROR_CONNECTING_URL_WITH_STATUS_CODE, feed.getUrl(),
                        httpResponse.getStatusLine().getStatusCode());
                return;
            }
            PollingResult.Status status = PollingResult.Status.OK;
            try (InputStream in = new BufferedInputStream(httpResponse.getEntity().getContent())) {
                List<IFeedMessage> messages = Lists.newArrayList(readMessages(in, monitor, feed.getId()));
                if (messages.isEmpty()) {
                    status = PollingResult.Status.FEED_NOT_FOUND_AT_URL;
                }
                groupedMessages.put(feed, new PollingResult(status, messages));
            }
        } catch (IOException e) {
            Logs.log(LogMessages.ERROR_FETCHING_MESSAGES, e, feed.getUrl());
        } finally {
            monitor.done();
        }
    }

    private List<? extends IFeedMessage> readMessages(InputStream in, IProgressMonitor monitor, String eventId)
            throws IOException {
        FeedReader reader = new FeedReader(eventId, environment);
        reader.parse(in, monitor);
        return FluentIterable.from(reader.getEntries()).transform(new Function<FeedEntry, IFeedMessage>() {

            @Override
            public IFeedMessage apply(FeedEntry entry) {
                return new FeedMessage(entry.getId(), entry.getDate(), entry.getDescription(), entry.getTitle(),
                        toUrl(entry.getUrl()));
            }
        }).toList();
    }

    @Override
    public Map<FeedDescriptor, PollingResult> getMessages() {
        return groupedMessages;
    }

    @Override
    public Map<FeedDescriptor, Date> getPollDates() {
        return pollDates;
    }

    private static Optional<URI> urlToUri(URL url) {
        try {
            return Optional.of(url.toURI());
        } catch (URISyntaxException e) {
            return Optional.absent();
        }
    }

    @VisibleForTesting
    static URL toUrl(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw Throwables.propagate(e);
        }
    }

    class MutexRule implements ISchedulingRule {

        @Override
        public boolean contains(ISchedulingRule rule) {
            return rule == this;
        }

        @Override
        public boolean isConflicting(ISchedulingRule rule) {
            return rule == this;
        }

    }
}
