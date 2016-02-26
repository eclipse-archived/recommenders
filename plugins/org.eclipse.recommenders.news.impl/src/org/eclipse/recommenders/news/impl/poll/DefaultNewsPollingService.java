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
package org.eclipse.recommenders.news.impl.poll;

import static org.eclipse.recommenders.news.api.poll.PollingResult.Status.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.recommenders.news.api.NewsItem;
import org.eclipse.recommenders.news.api.poll.INewsPollingService;
import org.eclipse.recommenders.news.api.poll.PollingPolicy;
import org.eclipse.recommenders.news.api.poll.PollingRequest;
import org.eclipse.recommenders.news.api.poll.PollingResult;
import org.eclipse.recommenders.news.api.poll.PollingResult.Status;

import com.google.common.annotations.VisibleForTesting;

public class DefaultNewsPollingService implements INewsPollingService {

    private final IDownloadService downloadService;
    private final IFeedItemStore feedItemStore;

    public DefaultNewsPollingService() {
        this.downloadService = new DefaultDownloadService();
        this.feedItemStore = new DefaultFeedItemStore();
    }

    @VisibleForTesting
    DefaultNewsPollingService(IDownloadService downloadService, IFeedItemStore feedItemStore) {
        this.downloadService = downloadService;
        this.feedItemStore = feedItemStore;
    }

    @Override
    public Collection<PollingResult> poll(Collection<PollingRequest> requests, @Nullable IProgressMonitor monitor) {
        SubMonitor progress = SubMonitor.convert(monitor, requests.size());
        try {
            Date now = new Date();

            List<PollingResult> results = new ArrayList<>(requests.size());
            for (PollingRequest request : requests) {
                if (progress.isCanceled()) {
                    break;
                }
                PollingResult result = poll(request, now, progress.newChild(1));
                results.add(result);
            }

            return results;
        } finally {
            if (monitor != null) {
                monitor.done();
            }
        }
    }

    private PollingResult poll(PollingRequest request, Date pollingDate, SubMonitor monitor) {
        URI feedUri = request.getFeedUri();

        SubMonitor progress = SubMonitor.convert(monitor, feedUri.toString(), 1);

        Date lastPolledDate;
        try {
            lastPolledDate = downloadService.getLastAttemptDate(feedUri);
        } catch (IOException e) {
            lastPolledDate = null;
        }

        PollingPolicy policy = request.getPollingPolicy();

        if (policy.shouldPoll(lastPolledDate, pollingDate)) {
            try {
                return performDownload(feedUri, progress.newChild(1));
            } catch (IOException e) {
                try {
                    progress.setWorkRemaining(1);
                    return fallbackToLocalStore(feedUri, progress.newChild(1));
                } catch (IOException f) {
                    progress.setWorkRemaining(1);
                    return fallbackToErrorResult(feedUri, progress.newChild(1));
                }
            }
        } else {
            try {
                return fallbackToLocalStore(feedUri, progress.newChild(1));
            } catch (IOException e) {
                progress.setWorkRemaining(1);
                return fallbackToErrorResult(feedUri, progress.newChild(1));
            }
        }
    }

    private PollingResult performDownload(URI feedUri, SubMonitor monitor) throws IOException {
        SubMonitor progress = SubMonitor.convert(monitor, 2);

        List<NewsItem> newItems;
        List<NewsItem> allItems;
        Status status;

        try (InputStream stream = downloadService.download(feedUri, progress.newChild(1))) {
            newItems = feedItemStore.udpate(feedUri, stream, progress.newChild(1));
            allItems = feedItemStore.getNewsItems(feedUri);
            status = DOWNLOADED;
            return new PollingResult(feedUri, newItems, allItems, status);
        }
    }

    private PollingResult fallbackToLocalStore(URI feedUri, SubMonitor monitor) throws IOException {
        SubMonitor progress = SubMonitor.convert(monitor, 1);

        List<NewsItem> newItems = Collections.emptyList();
        List<NewsItem> allItems;
        Status status;

        InputStream in = downloadService.read(feedUri);
        if (in != null) {
            feedItemStore.udpate(feedUri, in, progress.newChild(1));
            allItems = feedItemStore.getNewsItems(feedUri);
            status = DOWNLOADED;
        } else {
            progress.worked(1);
            allItems = Collections.emptyList();
            status = NOT_DOWNLOADED;
        }

        return new PollingResult(feedUri, newItems, allItems, status);
    }

    private PollingResult fallbackToErrorResult(URI feedUri, SubMonitor monitor) {
        SubMonitor progress = SubMonitor.convert(monitor, 1);

        List<NewsItem> newItems = Collections.emptyList();
        List<NewsItem> allItems = Collections.emptyList();
        Status status = NOT_DOWNLOADED;

        progress.worked(1);
        return new PollingResult(feedUri, newItems, allItems, status);
    }
}
