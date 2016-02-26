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
package org.eclipse.recommenders.internal.news.rcp.notifications;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.EventTopic;
import org.eclipse.mylyn.commons.notifications.core.AbstractNotification;
import org.eclipse.mylyn.commons.notifications.ui.NotificationsUi;
import org.eclipse.recommenders.internal.news.rcp.FeedDescriptor;
import org.eclipse.recommenders.internal.news.rcp.NewsRcpPreferences;
import org.eclipse.recommenders.internal.news.rcp.TopicConstants;
import org.eclipse.recommenders.news.api.NewsItem;
import org.eclipse.recommenders.news.api.poll.PollingResult;

@Creatable
@Singleton
public class NotificationBridge {

    private final NewsRcpPreferences preferences;
    private final ECommandService commandService;
    private final EHandlerService handlerService;

    @Inject
    public NotificationBridge(NewsRcpPreferences preferences, ECommandService commandService, EHandlerService handlerService) {
        this.preferences = preferences;
        this.commandService = commandService;
        this.handlerService = handlerService;
    }

    @Inject
    @Optional
    public void handlePollingResults(@EventTopic(TopicConstants.POLLING_RESULTS) Collection<PollingResult> pollingResults) {
        List<AbstractNotification> notifications = new ArrayList<>();

        List<FeedDescriptor> feedDescriptors = preferences.getFeedDescriptors();
        Object token = new Object();

        for (PollingResult pollingResult : pollingResults) {
            for (FeedDescriptor feedDescriptor : feedDescriptors) {
                if (feedDescriptor.isEnabled() && pollingResult.getFeedUri().equals(feedDescriptor.getUri())) {
                    for (NewsItem newNewsItem : pollingResult.getNewNewsItems()) {
                        notifications.add(new NewNewsItemsNotification(newNewsItem, feedDescriptor, token, commandService, handlerService));
                    }
                    break;
                }
            }
        }

        NotificationsUi.getService().notify(notifications);
    }
}
