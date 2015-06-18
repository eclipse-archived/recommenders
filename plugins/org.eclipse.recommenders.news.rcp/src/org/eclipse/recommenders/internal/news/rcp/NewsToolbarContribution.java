/**
 * Copyright (c) 2015 Codetrails GmbH. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Johannes Dorn - initial API and implementation.
 */
package org.eclipse.recommenders.internal.news.rcp;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.recommenders.internal.news.rcp.FeedEvents.NewFeedItemsEvent;
import org.eclipse.recommenders.internal.news.rcp.l10n.Messages;
import org.eclipse.recommenders.internal.news.rcp.menus.NewsMenuListener;
import org.eclipse.recommenders.internal.news.rcp.menus.NoNewsMenuListener;
import org.eclipse.recommenders.internal.news.rcp.notifications.CommonImages;
import org.eclipse.recommenders.news.rcp.IFeedMessage;
import org.eclipse.recommenders.news.rcp.INewsService;
import org.eclipse.recommenders.rcp.SharedImages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;

import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class NewsToolbarContribution extends WorkbenchWindowControlContribution {

    private final INewsService service;

    private UpdatingNewsAction updatingNewsAction;
    private MenuManager menuManager;
    private NoNewsMenuListener noNewsMenuListener;
    private NewsMenuListener newsMenuListener;

    @Inject
    public NewsToolbarContribution(INewsService service, SharedImages images, EventBus eventBus) {
        this.service = service;
        eventBus.register(this);
        noNewsMenuListener = new NoNewsMenuListener();
        newsMenuListener = new NewsMenuListener(eventBus);
    }

    @Override
    protected Control createControl(Composite parent) {
        menuManager = new MenuManager();
        updatingNewsAction = new UpdatingNewsAction();
        ToolBarManager manager = new ToolBarManager(SWT.FLAT | SWT.HORIZONTAL);
        manager.add(updatingNewsAction);
        manager.setContextMenuManager(menuManager);
        return manager.createControl(parent);
    }

    @Subscribe
    public void handle(NewFeedItemsEvent event) {
        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

            @Override
            public void run() {
                updatingNewsAction.setAvailableNews();
            }
        });
    }

    private class UpdatingNewsAction extends Action {
        Map<FeedDescriptor, List<IFeedMessage>> messages = Maps.newHashMap();

        private UpdatingNewsAction() {
            setNoAvailableNews();
        }

        @Override
        public void run() {
            setNoAvailableNews();
            messages = service.getMessages(Constants.COUNT_PER_FEED);
            menuManager.getMenu().setVisible(true);
            if (!messages.isEmpty() && Utils.containsUnreadMessages(messages)) {
                setAvailableNews();
            }
        }

        private void setNoAvailableNews() {
            setImageDescriptor(CommonImages.RSS_INACTIVE);
            setToolTipText(Messages.TOOLTIP_NO_NEW_MESSAGES);
            clearMenu();
            menuManager.addMenuListener(noNewsMenuListener);
        }

        private void setAvailableNews() {
            messages = service.getMessages(Constants.COUNT_PER_FEED);
            if (messages.isEmpty()) {
                return;
            }
            setImageDescriptor(CommonImages.RSS_ACTIVE);
            setToolTipText(MessageFormat.format(Messages.TOOLTIP_NEW_MESSAGES,
                    Utils.getUnreadMessagesNumber(Utils.mergeMessages(messages))));
            clearMenu();
            setNewsMenu(messages);
        }

        private void clearMenu() {
            menuManager.setRemoveAllWhenShown(true);
            menuManager.removeMenuListener(noNewsMenuListener);
            menuManager.removeMenuListener(newsMenuListener);
        }

        private void setNewsMenu(Map<FeedDescriptor, List<IFeedMessage>> messages) {
            newsMenuListener.setMessages(messages);
            menuManager.addMenuListener(newsMenuListener);
        }
    }
}
