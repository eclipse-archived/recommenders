/**
 * Copyright (c) 2015 Codetrails GmbH. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Johannes Dorn - initial API and implementation.
 */
package org.eclipse.recommenders.news.rcp;

import java.util.Map;

import org.eclipse.recommenders.internal.news.rcp.FeedDescriptor;
import org.eclipse.recommenders.internal.news.rcp.FeedEvents.AllReadEvent;
import org.eclipse.recommenders.internal.news.rcp.FeedEvents.FeedMessageReadEvent;
import org.eclipse.recommenders.internal.news.rcp.FeedEvents.FeedReadEvent;

public interface INewsService {
    /**
     *
     * @param countPerFeed
     *            limit of messages per each entry
     * @return Map of Feed and PollingResult (Wrapper for polling status and feed messages)
     */
    Map<FeedDescriptor, IPollingResult> getMessages(int countPerFeed);

    /**
     * Starts polling feeds
     */
    void start();

    /**
     * Removes feed from the set of feeds to be polled. This feed won't be polled anymore, until someone sets it as feed
     * to be polled again.
     *
     * @param feed
     *            Feed to be removed
     */
    void removeFeed(FeedDescriptor feed);

    /**
     * Handles situation when job is done (i.e. grouping and filtering polled messages)
     *
     * @param job
     *            Job that has just been done
     */
    void jobDone(IPollFeedJob job);

    /**
     * Polls feeds.
     */
    void pollFeeds();

    /**
     * Stops polling feeds immediately.
     */
    void forceStop();

    /**
     * Displays notification with new (that has been polled for the first time) messages.
     */
    void displayNotification();

    /**
     * Handles situation when a single message has been marked as read.
     *
     * @param event
     *            Event that has to be published to trigger this method
     */
    void handleMessageRead(FeedMessageReadEvent event);

    /**
     * Handles situation when all messages has been marked as read.
     *
     * @param event
     *            Event that has to be published to trigger this method
     */
    void handleAllRead(AllReadEvent event);

    /**
     * Handles situation when all messages of particular feed has been marked as read.
     *
     * @param event
     *            Event that has to be published to trigger this method
     */
    void handleFeedRead(FeedReadEvent event);
}
