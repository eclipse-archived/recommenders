/**
 * Copyright (c) 2015 Pawel Nowak.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.recommenders.internal.news.rcp.menus;

import static org.eclipse.recommenders.internal.news.rcp.FeedEvents.createFeedMessageReadEvent;
import static org.eclipse.recommenders.internal.news.rcp.menus.MarkAsReadAction.*;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.recommenders.internal.news.rcp.FeedDescriptor;
import org.eclipse.recommenders.internal.news.rcp.Utils;
import org.eclipse.recommenders.internal.news.rcp.l10n.Messages;
import org.eclipse.recommenders.news.rcp.IFeedMessage;
import org.eclipse.recommenders.rcp.utils.BrowserUtils;

import com.google.common.eventbus.EventBus;

public class NewsMenuListener implements IMenuListener {
    private final EventBus eventBus;
    private Map<FeedDescriptor, List<IFeedMessage>> messages;

    public NewsMenuListener(EventBus eventBus) {
        super();
        this.eventBus = eventBus;
    }

    public void setMessages(Map<FeedDescriptor, List<IFeedMessage>> messages) {
        this.messages = messages;
    }

    @Override
    public void menuAboutToShow(IMenuManager manager) {
        for (Entry<FeedDescriptor, List<IFeedMessage>> entry : messages.entrySet()) {
            String menuName = entry.getKey().getName();
            if (containsUnreadMessages(entry.getValue())) {
                menuName = menuName.concat(" (" + Utils.getUnreadMessagesNumber(entry.getValue()) + ")");
            }
            MenuManager menu = new MenuManager(menuName, entry.getKey().getId());
            if (entry.getKey().getIcon() != null) {
                // in Kepler: The method setImageDescriptor(ImageDescriptor) is undefined for the type MenuManager
                // menu.setImageDescriptor(ImageDescriptor.createFromImage(entry.getKey().getIcon()));
            }
            for (final IFeedMessage message : entry.getValue()) {
                Action action = new Action() {

                    @Override
                    public void run() {
                        BrowserUtils.openInExternalBrowser(message.getUrl());
                        eventBus.post(createFeedMessageReadEvent(message.getId()));
                    }
                };
                action.setText(message.getTitle());
                if (!message.isRead()) {
                    action.setText(Messages.UNREAD_MESSAGE_PREFIX.concat(action.getText()));
                }
                menu.add(action);
            }
            addMarkAsReadAction(entry.getKey(), menu);
            manager.add(menu);
        }
        manager.add(new Separator());
        manager.add(newMarkAllAsReadAction(eventBus));
    }

    private void addMarkAsReadAction(FeedDescriptor feed, MenuManager menu) {
        menu.add(new Separator());
        menu.add(newMarkFeedAsReadAction(eventBus, feed));
    }

    private boolean containsUnreadMessages(List<IFeedMessage> messages) {
        for (IFeedMessage message : messages) {
            if (!message.isRead()) {
                return true;
            }
        }
        return false;
    }

}
