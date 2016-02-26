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

import java.text.MessageFormat;
import java.util.Collections;
import java.util.Date;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.mylyn.commons.notifications.core.AbstractNotification;
import org.eclipse.mylyn.commons.notifications.ui.AbstractUiNotification;
import org.eclipse.recommenders.internal.news.rcp.CommandConstants;
import org.eclipse.recommenders.internal.news.rcp.CommonImages;
import org.eclipse.recommenders.internal.news.rcp.FeedDescriptor;
import org.eclipse.recommenders.internal.news.rcp.l10n.Messages;
import org.eclipse.recommenders.news.api.NewsItem;
import org.eclipse.swt.graphics.Image;

import com.google.common.collect.ImmutableMap;

@SuppressWarnings("restriction")
public class NewNewsItemsNotification extends AbstractUiNotification {

    private static final String EVENT_ID = "org.eclipse.recommenders.news.rcp.event.newNewsItems"; //$NON-NLS-1$

    private final NewsItem newsItem;
    private final FeedDescriptor feed;
    private final Object token;
    private final ECommandService commandService;
    private final EHandlerService handlerService;

    public NewNewsItemsNotification(NewsItem newsItem, FeedDescriptor feed, Object token,
            ECommandService commandService, EHandlerService handlerService) {
        super(EVENT_ID);
        this.newsItem = newsItem;
        this.feed = feed;
        this.token = token;
        this.commandService = commandService;
        this.handlerService = handlerService;
    }

    /**
     * If <strong>both</strong> this and the other object are {@code NewNewsItemsNotification}s, then consider older
     * notifications to be greater than newer notifications. This is the reverse of the ordering imposed by
     * {@link AbstractNotification#compareTo(AbstractNotification)}, which unfortunately violates the contract of
     *
     * @see <a href="https://www.eclipse.org/forums/index.php/m/1726175/">Mylyn forum post about a potential bug with
     *      this</a>.
     */
    @Override
    public int compareTo(AbstractNotification other) {
        if (this.getClass().equals(other.getClass())) {
            if (this.getDate().before(other.getDate())) {
                return 1;
            } else if (this.getDate().after(other.getDate())) {
                return -1;
            } else {
                return 0;
            }
        } else {
            return super.compareTo(other);
        }
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Object getAdapter(Class adapter) {
        return Platform.getAdapterManager().getAdapter(this, adapter);
    }

    @Override
    public Image getNotificationImage() {
        return null;
    }

    @Override
    public Image getNotificationKindImage() {
        return CommonImages.RSS_ACTIVE.createImage();
    }

    @Override
    public void open() {
        ParameterizedCommand command = commandService.createCommand(CommandConstants.ID_READ_NEWS_ITEMS,
                ImmutableMap.<String, Object>of(CommandConstants.PARAMETER_READ_NEWS_ITEMS_NEWS_ITEMS,
                        Collections.singleton(newsItem), CommandConstants.PARAMETER_READ_NEWS_ITEMS_OPEN_BROWSER,
                        true));
        handlerService.executeHandler(command);
    }

    @Override
    public Date getDate() {
        return newsItem.getDate();
    }

    @Override
    public String getDescription() {
        return MessageFormat.format(Messages.NOTIFICATION_DESCRIPTION, feed.getName());
    }

    @Override
    public String getLabel() {
        return newsItem.getTitle();
    }

    @Override
    public Object getToken() {
        return token;
    }
}
