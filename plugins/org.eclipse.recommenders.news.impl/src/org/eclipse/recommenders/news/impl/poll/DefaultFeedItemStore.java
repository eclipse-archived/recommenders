/**
 * Copyright (c) 2016 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Dorn - initial API and implementation.
 */
package org.eclipse.recommenders.news.impl.poll;

import static java.util.Arrays.asList;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.mylyn.commons.notifications.core.NotificationEnvironment;
import org.eclipse.mylyn.internal.commons.notifications.feed.FeedEntry;
import org.eclipse.mylyn.internal.commons.notifications.feed.FeedReader;
import org.eclipse.mylyn.internal.commons.notifications.feed.RSSItem;
import org.eclipse.recommenders.news.api.NewsItem;

@SuppressWarnings("restriction")
public class DefaultFeedItemStore implements IFeedItemStore {

    private final Map<URI, List<NewsItem>> cache = new ConcurrentHashMap<>();

    private final NotificationEnvironment environment = new NotificationEnvironment();

    @Override
    public List<NewsItem> udpate(URI feedUri, InputStream stream, @Nullable IProgressMonitor monitor)
            throws IOException {
        SubMonitor progress = SubMonitor.convert(monitor, 1);
        try {
            List<NewsItem> updatedItems = parseNewsItems(stream, feedUri.toString(), progress.newChild(1));

            List<NewsItem> oldItems;
            if (updatedItems.isEmpty()) {
                oldItems = cache.remove(feedUri);
            } else {
                oldItems = cache.put(feedUri, updatedItems);
            }

            Set<String> oldIds = getIds(oldItems);
            List<NewsItem> newItems = new ArrayList<>();

            for (NewsItem updatedItem : updatedItems) {
                if (!oldIds.contains(updatedItem.getId())) {
                    newItems.add(updatedItem);
                }
            }

            return newItems;
        } finally {
            stream.close();

            if (monitor != null) {
                monitor.done();
            }
        }
    }

    @Override
    public List<NewsItem> getNewsItems(URI feedUri) {
        List<NewsItem> cachedItems = cache.get(feedUri);
        return cachedItems != null ? cachedItems : Collections.<NewsItem>emptyList();
    }

    private List<NewsItem> parseNewsItems(InputStream in, String eventId, SubMonitor monitor) throws IOException {
        SubMonitor progress = SubMonitor.convert(monitor, 100);

        FeedReader reader = new FeedReader(eventId, environment) {

            @Override
            protected FeedEntry createEntry(RSSItem rssItem) {
                fixUpPubDate(rssItem);
                return super.createEntry(rssItem);
            }

            private List<SimpleDateFormat> alternativeFormats = asList(
                    // Tue, 18 Sept 2007 09:00:00 EST
                    new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss ZZZZ", Locale.US),
                    // Wed, Feb 03, 2010 14:34:37 EST
                    new SimpleDateFormat("EEE, MMM dd, yyyy HH:mm:ss ZZZZ", Locale.US),
                    // Fri, 04 Jun 2010 13:55 EST
                    new SimpleDateFormat("EEE, dd MMM yyyy HH:mm ZZZZ", Locale.US),
                    // Wed, Feb 23, 2011 09:40:00 AM EST
                    new SimpleDateFormat("EEE, MMM dd, yyyy hh:mm:ss aa ZZZZ", Locale.US),
                    // Tue, 1 Mar 2011 12:25:00 PM EST
                    new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss aa ZZZZ", Locale.US),
                    // August 22, 2017 9:00:00 am EST
                    new SimpleDateFormat("MMM dd, yyyy hh:mm:ss aa ZZZZ", Locale.US));

            /**
             * Fix up the {@code <pubDate>}, as {@link FeedReader} does not support all date formats occurring in the
             * wild.
             */
            private void fixUpPubDate(RSSItem rssItem) {
                String originalPubDate = rssItem.getPubDate();
                // Ugly hack to treat "Sept" as synonym of "Sep".
                originalPubDate = originalPubDate.replaceAll("Sept\\b", "Sep");

                SimpleDateFormat canonicalFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss ZZZZ", Locale.US); //$NON-NLS-1$
                for (SimpleDateFormat alternativeFormat : alternativeFormats) {
                    try {
                        Date parsedDate = alternativeFormat.parse(originalPubDate);
                        String fixedPubDate = canonicalFormat.format(parsedDate);
                        rssItem.setPubDate(fixedPubDate);
                        break;
                    } catch (ParseException e) {
                        // Try the next format.
                        continue;
                    }
                }
            }
        };
        IStatus status = reader.parse(in, progress.newChild(80));

        if (status.isOK()) {
            return convertEntries(reader.getEntries(), progress.newChild(20));
        } else {
            progress.setWorkRemaining(0);

            Throwable exception = status.getException();
            if (exception instanceof Exception) {
                throw new IOException(exception);
            }

            return Collections.emptyList();
        }
    }

    private List<NewsItem> convertEntries(List<FeedEntry> entries, SubMonitor monitor) {
        SubMonitor progress = SubMonitor.convert(monitor, entries.size());

        List<NewsItem> items = new ArrayList<>(entries.size());

        for (FeedEntry entry : entries) {
            try {
                String title = entry.getTitle();
                if (title == null) {
                    continue;
                }
                Date date = entry.getDate();
                if (date == null) {
                    continue;
                }
                URI uri = new URI(entry.getUrl());
                NewsItem item = new NewsItem(title, date, uri, entry.getId());
                items.add(item);
            } catch (URISyntaxException e) {
                continue;
            } finally {
                progress.worked(1);
            }
        }

        return items;
    }

    private Set<String> getIds(@Nullable List<NewsItem> items) {
        if (items == null) {
            return Collections.emptySet();
        }

        Set<String> ids = new HashSet<>();

        for (NewsItem item : items) {
            ids.add(item.getId());
        }

        return ids;
    }
}
