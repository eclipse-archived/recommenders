/**
 * Copyright (c) 2015 Pawel Nowak.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.recommenders.news.rcp;

import java.util.Set;

import org.eclipse.recommenders.internal.news.rcp.FeedDescriptor;

public interface IJobFacade {
    /**
     * Schedules an update of all feeds after the given delay (in miliseconds)
     *
     * @param service
     *            news service that control the flow
     * @param delay
     *            update will be run after specified delay
     */
    void scheduleNewsUpdate(INewsService service, long delay);

    /**
     * Polls feeds.
     *
     * @param service
     *            news service that control the flow
     * @param feeds
     *            {@code Set} of feeds to be polled
     */
    void schedulePollFeeds(INewsService service, Set<FeedDescriptor> feeds);

    /**
     * Cancels polling feeds.
     */
    void cancelPollFeeds();
}
