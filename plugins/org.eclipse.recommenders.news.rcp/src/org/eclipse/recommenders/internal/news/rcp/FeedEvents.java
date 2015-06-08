/**
 * Copyright (c) 2015 Codetrails GmbH. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Johannes Dorn - initial API and implementation.
 */
package org.eclipse.recommenders.internal.news.rcp;

import java.util.Date;
import java.util.Map;

public class FeedEvents {

    public static NewFeedItemsEvent createNewFeedItemsEvent() {
        return new NewFeedItemsEvent();
    }

    public static FeedMessageReadEvent createFeedMessageReadEvent(String id) {
        return new FeedMessageReadEvent(id);
    }

    public static FeedJobDoneEvent createFeedJobDoneEvent(Map<FeedDescriptor, Date> pollDates) {
        return new FeedJobDoneEvent(pollDates);
    }

    public static class NewFeedItemsEvent {
    }

    public static class FeedMessageReadEvent {
        private final String id;

        public FeedMessageReadEvent(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }
    }

    public static class FeedJobDoneEvent {
        private final Map<FeedDescriptor, Date> pollDates;

        public FeedJobDoneEvent(Map<FeedDescriptor, Date> pollDates) {
            this.pollDates = pollDates;
        }

        public Map<FeedDescriptor, Date> getPollDates() {
            return pollDates;
        }
    }

}
