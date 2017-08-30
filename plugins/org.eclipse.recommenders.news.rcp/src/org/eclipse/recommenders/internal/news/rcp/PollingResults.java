/**
 * Copyright (c) 2017 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Sewe - initial API and implementation.
 */
package org.eclipse.recommenders.internal.news.rcp;

import static java.lang.Math.min;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.recommenders.news.api.NewsItem;
import org.eclipse.recommenders.news.api.poll.PollingResult;

public final class PollingResults {

    private PollingResults() {
        throw new AssertionError();
    }

    public static List<NewsItem> getAllNewsItems(PollingResult result, int limit) {
        List<NewsItem> allNewsItems = result.getAllNewsItems();
        List<NewsItem> limitedNewsItems = new ArrayList<>(min(allNewsItems.size(), limit));
        for (NewsItem item : allNewsItems) {
            if (limitedNewsItems.size() >= limit) {
                break;
            } else {
                limitedNewsItems.add(item);
            }
        }
        return limitedNewsItems;
    }

    public static List<NewsItem> getNewNewsItems(PollingResult result, int limit) {
        List<NewsItem> allNewsItems = getAllNewsItems(result, limit);
        List<NewsItem> newNewsItems = result.getNewNewsItems();
        List<NewsItem> limitedNewsItems = new ArrayList<>(min(newNewsItems.size(), limit));
        for (NewsItem item : allNewsItems) {
            if (newNewsItems.contains(item)) {
                limitedNewsItems.add(item);
            }
        }
        return limitedNewsItems;
    }
}
