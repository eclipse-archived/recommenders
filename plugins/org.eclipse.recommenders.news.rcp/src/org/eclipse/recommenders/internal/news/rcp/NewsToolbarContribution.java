/**
 * Copyright (c) 2015 Codetrails GmbH. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Johannes Dorn - initial API and implementation.
 */
package org.eclipse.recommenders.internal.news.rcp;

import static org.eclipse.recommenders.internal.news.rcp.MessageUtils.*;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.recommenders.internal.news.rcp.FeedEvents.AllReadEvent;
import org.eclipse.recommenders.internal.news.rcp.FeedEvents.FeedMessageReadEvent;
import org.eclipse.recommenders.internal.news.rcp.FeedEvents.FeedReadEvent;
import org.eclipse.recommenders.internal.news.rcp.FeedEvents.NewFeedItemsEvent;
import org.eclipse.recommenders.internal.news.rcp.l10n.Messages;
import org.eclipse.recommenders.internal.news.rcp.menus.NewsMenuListener;
import org.eclipse.recommenders.news.rcp.INewsService;
import org.eclipse.recommenders.news.rcp.IPollingResult;
import org.eclipse.recommenders.news.rcp.IPollingResult.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;

import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;

@Creatable
public class NewsToolbarContribution extends WorkbenchWindowControlContribution {

    @Inject
    private INewsService service;
    @Inject
    private NewsRcpPreferences preferences;
    private NewsMenuListener newsMenuListener;
    private UpdatingNewsAction updatingNewsAction;
    private MenuManager menuManager;

    public NewsToolbarContribution() {
        NewsRcpModule.initiateContext(this);
    }

    @PostConstruct
    public void init() {
        NewsRcpModule.EVENT_BUS.register(this);
        newsMenuListener = new NewsMenuListener(NewsRcpModule.EVENT_BUS, service);
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
        final IWorkbench workbench = PlatformUI.getWorkbench();
        if (workbench.isClosing()) {
            return;
        }

        final Display display = workbench.getDisplay();
        if (display.isDisposed()) {
            return;
        }

        display.asyncExec(new Runnable() {

            @Override
            public void run() {
                updatingNewsAction.setAvailableNews();
            }
        });
    }

    @Subscribe
    public void handleAllRead(AllReadEvent event) {
        updatingNewsAction.checkForNews();
    }

    @Subscribe
    public void handleFeedRead(FeedReadEvent event) {
        updatingNewsAction.checkForNews();
    }

    @Subscribe
    public void handleMessageRead(FeedMessageReadEvent event) {
        updatingNewsAction.checkForNews();
    }

    private class UpdatingNewsAction extends Action {
        Map<FeedDescriptor, IPollingResult> messages = Maps.newHashMap();

        private UpdatingNewsAction() {
            setNoAvailableNews();
        }

        @Override
        public void run() {
            setNoAvailableNews();
            messages = service.getMessages(Constants.COUNT_PER_FEED);
            menuManager.getMenu().setVisible(true);
            if (!messages.isEmpty() || containsUnreadMessages(messages)) {
                setAvailableNews();
            }
        }

        private void setNoAvailableNews() {
            setImageDescriptor(CommonImages.RSS_INACTIVE);
            setToolTipText(Messages.TOOLTIP_NO_NEW_MESSAGES);
            clearMenu();
            HashMap<FeedDescriptor, IPollingResult> groupedMessages = Maps.newHashMap();
            for (FeedDescriptor feed : preferences.getFeedDescriptors()) {
                if (feed.isEnabled()) {
                    groupedMessages.put(feed, new PollingResult(Status.FEEDS_NOT_POLLED_YET));
                }
            }
            newsMenuListener.setMessages(groupedMessages);
            menuManager.addMenuListener(newsMenuListener);
        }

        private void setAvailableNews() {
            messages = service.getMessages(Constants.COUNT_PER_FEED);

            if (!containsUnreadMessages(messages)) {
                setImageDescriptor(CommonImages.RSS_INACTIVE);
            } else {
                setImageDescriptor(CommonImages.RSS_ACTIVE);
            }

            setToolTipText(MessageFormat.format(Messages.TOOLTIP_NEW_MESSAGES,
                    getUnreadMessagesNumber(mergeMessages(messages))));
            clearMenu();
            setNewsMenu(messages);
        }

        private void clearMenu() {
            menuManager.setRemoveAllWhenShown(true);
            menuManager.removeMenuListener(newsMenuListener);
        }

        private void setNewsMenu(Map<FeedDescriptor, IPollingResult> messages) {
            newsMenuListener.setMessages(messages);
            menuManager.addMenuListener(newsMenuListener);
        }

        public void checkForNews() {
            messages = service.getMessages(Constants.COUNT_PER_FEED);
            if (messages.isEmpty() || !containsUnreadMessages(messages)) {
                setNoAvailableNews();
            }
        }
    }
}
