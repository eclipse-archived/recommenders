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
package org.eclipse.recommenders.news.api.poll;

import java.net.URI;
import java.util.List;

import org.eclipse.recommenders.news.api.NewsItem;

public final class PollingResult {

    public enum Status {

        NOT_DOWNLOADED,
        DOWNLOADED;
    }

    private final URI feedUri;
    private final List<NewsItem> newNewsItems;
    private final List<NewsItem> allNewsItems;
    private final Status status;

    public PollingResult(URI feedUri, List<NewsItem> newNewsItems, List<NewsItem> allNewsItems, Status status) {
        this.feedUri = feedUri;
        this.newNewsItems = newNewsItems;
        this.allNewsItems = allNewsItems;
        this.status = status;
    }

    public URI getFeedUri() {
        return feedUri;
    }

    public List<NewsItem> getNewNewsItems() {
        return newNewsItems;
    }

    public List<NewsItem> getAllNewsItems() {
        return allNewsItems;
    }

    public Status getStatus() {
        return status;
    }
}
