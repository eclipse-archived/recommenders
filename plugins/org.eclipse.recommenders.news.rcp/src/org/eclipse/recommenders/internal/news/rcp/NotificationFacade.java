/**
 * Copyright (c) 2015 Pawel Nowak.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.recommenders.internal.news.rcp;

import java.util.List;
import java.util.Map;

import org.eclipse.recommenders.internal.news.rcp.notifications.NewsNotificationPopup;
import org.eclipse.recommenders.news.rcp.IFeedMessage;
import org.eclipse.recommenders.news.rcp.INotificationFacade;
import org.eclipse.recommenders.rcp.utils.Shells;
import org.eclipse.swt.widgets.Display;

import com.google.common.eventbus.EventBus;

public class NotificationFacade implements INotificationFacade {

    @Override
    public void displayNotification(final Map<FeedDescriptor, List<IFeedMessage>> messages, final EventBus eventBus) {
        final Display display = Shells.getDisplay();
        display.asyncExec(new Runnable() {

            @Override
            public void run() {
                new NewsNotificationPopup(display, messages, eventBus).open();
            }
        });
    }

}
