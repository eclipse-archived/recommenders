/**
 * Copyright (c) 2015 Pawel Nowak.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.recommenders.news.rcp;

import java.util.List;

/**
 * Result of IPollFeedJob containing its status and messages
 *
 */
public interface IPollingResult {

    public enum Status {
        // Feed has been polled successfully
        OK,
        // Feed hasn't been polled yet
        FEEDS_NOT_POLLED_YET,
        // Feed's URL doesn't contain messages or is not feed at all
        FEED_NOT_FOUND_AT_URL,
        // There was a problem connecting to feed i.e. UnkownHostException
        ERROR_CONNECTING_TO_FEED
    }

    Status getStatus();

    List<IFeedMessage> getMessages();
}
