/**
 * Copyright (c) 2015 Pawel Nowak.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.recommenders.internal.news.rcp;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.recommenders.news.rcp.IFeedMessage;

import com.google.common.collect.Lists;

public class PollingResult {
    private final Status status;
    private final List<IFeedMessage> messages;

    public enum Status {
        OK,
        FEEDS_NOT_POLLED_YET,
        FEED_NOT_FOUND_AT_URL,
        ERROR_CONNECTING_TO_FEED
    }

    public PollingResult(Status status, List<IFeedMessage> messages) {
        this.status = status;
        this.messages = Lists.newArrayList(messages);
    }

    public PollingResult(Status status) {
        this.status = status;
        messages = Lists.newArrayList();
    }

    public Status getStatus() {
        return status;
    }

    public List<IFeedMessage> getMessages() {
        return messages;
    }

    public static PollingResult newConnectionErrorResult() {
        return new PollingResult(Status.ERROR_CONNECTING_TO_FEED, new ArrayList<IFeedMessage>());

    }
}
