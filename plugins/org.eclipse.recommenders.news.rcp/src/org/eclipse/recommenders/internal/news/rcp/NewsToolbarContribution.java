/**
 * Copyright (c) 2015 Codetrails GmbH. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Johannes Dorn - initial API and implementation.
 */
package org.eclipse.recommenders.internal.news.rcp;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.recommenders.internal.news.rcp.FeedEvents.NewFeedItemsEvent;
import org.eclipse.recommenders.internal.news.rcp.l10n.Messages;
import org.eclipse.recommenders.internal.news.rcp.notifications.CommonImages;
import org.eclipse.recommenders.internal.news.rcp.notifications.NewsNotificationPopup;
import org.eclipse.recommenders.internal.news.rcp.notifications.NoNewsNotificationPopup;
import org.eclipse.recommenders.news.rcp.IFeedMessage;
import org.eclipse.recommenders.news.rcp.INewsService;
import org.eclipse.recommenders.rcp.SharedImages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class NewsToolbarContribution extends WorkbenchWindowControlContribution {

    private final INewsService service;
    private final SharedImages images;
    private final EventBus eventBus;

    private UpdatingNewsAction updatingNewsAction;

    @Inject
    public NewsToolbarContribution(INewsService service, SharedImages images, EventBus eventBus) {
        this.service = service;
        this.images = images;
        this.eventBus = eventBus;
        eventBus.register(this);
    }

    @Override
    protected Control createControl(Composite parent) {
        updatingNewsAction = new UpdatingNewsAction();
        ToolBarManager manager = new ToolBarManager(SWT.FLAT | SWT.HORIZONTAL);
        manager.add(updatingNewsAction);

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
        private UpdatingNewsAction() {
            setNoAvailableNews();
        }

        @Override
        public void run() {
            setNoAvailableNews();

            Map<FeedDescriptor, List<IFeedMessage>> messages = service.getMessages(3);
            if (messages.isEmpty()) {
                new NoNewsNotificationPopup().open();
            } else {
                new NewsNotificationPopup(messages, eventBus).open();
            }
        }

        private void setNoAvailableNews() {
            setImageDescriptor(CommonImages.RSS_INACTIVE);
            setToolTipText(Messages.TOOLTIP_NO_NEW_MESSAGES);
        }

        private void setAvailableNews() {
            setImageDescriptor(CommonImages.RSS_ACTIVE);
            setToolTipText(Messages.TOOLTIP_NEW_MESSAGES);
        }
    }
}
