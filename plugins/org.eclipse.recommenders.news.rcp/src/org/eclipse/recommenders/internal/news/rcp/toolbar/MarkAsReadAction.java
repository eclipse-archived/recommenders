/**
 * Copyright (c) 2015 Pawel Nowak.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.recommenders.internal.news.rcp.toolbar;

import static org.eclipse.recommenders.internal.news.rcp.FeedEvents.*;

import org.eclipse.jface.action.Action;
import org.eclipse.recommenders.internal.news.rcp.FeedDescriptor;
import org.eclipse.recommenders.internal.news.rcp.l10n.Messages;

import com.google.common.eventbus.EventBus;

public final class MarkAsReadAction extends Action {

    private final EventBus eventBus;
    private final Boolean allFeeds;
    private final FeedDescriptor feed;

    private MarkAsReadAction(EventBus eventBus, FeedDescriptor feed, boolean allFeeds) {
        super();
        this.eventBus = eventBus;
        this.allFeeds = allFeeds;
        this.feed = feed;
    }

    public static MarkAsReadAction newMarkFeedAsReadAction(EventBus eventBus, FeedDescriptor feed) {
        return new MarkAsReadAction(eventBus, feed, false);
    }

    public static MarkAsReadAction newMarkAllAsReadAction(EventBus eventBus) {
        return new MarkAsReadAction(eventBus, null, true);
    }

    @Override
    public void run() {
        if (allFeeds) {
            eventBus.post(createAllReadEvent());
            return;
        }
        eventBus.post(createFeedReadEvent(feed));
    }

    @Override
    public String getText() {
        return Messages.LABEL_MARK_AS_READ;
    }
}
