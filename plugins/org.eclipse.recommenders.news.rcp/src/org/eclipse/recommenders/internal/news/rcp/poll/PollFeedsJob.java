/**
 * Copyright (c) 2016 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Sewe - initial API and implementation.
 */
package org.eclipse.recommenders.internal.news.rcp.poll;

import static java.util.concurrent.TimeUnit.MINUTES;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.recommenders.internal.news.rcp.FeedDescriptor;
import org.eclipse.recommenders.internal.news.rcp.NewsRcpPreferences;
import org.eclipse.recommenders.internal.news.rcp.TopicConstants;
import org.eclipse.recommenders.news.api.poll.INewsPollingService;
import org.eclipse.recommenders.news.api.poll.PollingPolicy;
import org.eclipse.recommenders.news.api.poll.PollingRequest;
import org.eclipse.recommenders.news.api.poll.PollingResult;

public class PollFeedsJob extends Job {

    private final boolean periodic;
    private final NewsRcpPreferences preferences;
    private final INewsPollingService pollingService;
    private final IEventBroker eventBroker;

    public PollFeedsJob(String name, boolean periodic, NewsRcpPreferences preferences,
            INewsPollingService pollingService, IEventBroker eventBroker) {
        super(name);
        this.periodic = periodic;
        this.preferences = preferences;
        this.pollingService = pollingService;
        this.eventBroker = eventBroker;
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        if (periodic && preferences.isEnabled()) {
            schedule(MINUTES.toMillis(preferences.getPollingInterval()));
        }

        List<FeedDescriptor> feeds = preferences.getFeedDescriptors();

        List<PollingRequest> requests = new ArrayList<>();
        for (FeedDescriptor feed : feeds) {
            if (!feed.isEnabled()) {
                continue;
            }

            PollingPolicy policy = periodic ? PollingPolicy.every(feed.getPollingInterval(), MINUTES)
                    : PollingPolicy.always();
            PollingRequest request = new PollingRequest(feed.getUri(), policy);
            requests.add(request);
        }

        Collection<PollingResult> results = pollingService.poll(requests, monitor);

        eventBroker.post(TopicConstants.POLLING_RESULTS, results);

        return Status.OK_STATUS; // TODO Real error status
    }
}
