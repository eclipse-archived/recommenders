/**
 * Copyright (c) 2015 Codetrails GmbH. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Johannes Dorn - initial API and implementation.
 */
package org.eclipse.recommenders.internal.news.rcp;

public class FeedEvents {

    public static NewFeedItemsEvent createNewFeedItemsEvent() {
        return new NewFeedItemsEvent();
    }

    public static FeedMessageReadEvent createFeedMessageReadEvent(String id) {
        return new FeedMessageReadEvent(id);
    }

    public static FeedReadEvent createFeedReadEvent(FeedDescriptor feed) {
        return new FeedReadEvent(feed);
    }

    public static AllReadEvent createAllReadEvent() {
        return new AllReadEvent();
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

    public static class FeedReadEvent {
        private final FeedDescriptor feed;

        public FeedReadEvent(FeedDescriptor feed) {
            this.feed = feed;
        }

        public FeedDescriptor getFeed() {
            return feed;
        }
    }

    public static class AllReadEvent {

    }

}
