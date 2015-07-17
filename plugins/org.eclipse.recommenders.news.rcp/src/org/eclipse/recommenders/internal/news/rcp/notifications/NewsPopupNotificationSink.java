/**
 * Copyright (c) 2015 Pawel Nowak.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Based on http://git.eclipse.org/c/epp/org.eclipse.epp.logging.git/tree/bundles/org.eclipse.epp.logging.aeri.ui/src/org/eclipse/epp/internal/logging/aeri/ui/notifications/PopupNotificationSink.java
 */
package org.eclipse.recommenders.internal.news.rcp.notifications;

import java.util.List;
import java.util.Map;

import org.eclipse.mylyn.commons.notifications.core.AbstractNotification;
import org.eclipse.mylyn.commons.notifications.core.NotificationSink;
import org.eclipse.mylyn.commons.notifications.core.NotificationSinkEvent;
import org.eclipse.recommenders.internal.news.rcp.FeedDescriptor;
import org.eclipse.recommenders.news.rcp.IFeedMessage;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import com.google.common.eventbus.EventBus;

@SuppressWarnings("restriction")
public class NewsPopupNotificationSink extends NotificationSink {

    @Override
    public void notify(final NotificationSinkEvent event) {
        final Display display = PlatformUI.getWorkbench().getDisplay();
        display.asyncExec(new Runnable() {

            @Override
            public void run() {
                Map<FeedDescriptor, List<IFeedMessage>> messages = null;
                EventBus eventBus = null;
                for (AbstractNotification notification : event.getNotifications()) {
                    if (notification instanceof NewMessageNotification) {
                        messages = ((NewMessageNotification) notification).getMessages();
                        eventBus = ((NewMessageNotification) notification).getBus();
                        break;
                    }
                }
                if (messages != null && eventBus != null) {
                    new NewsNotificationPopup(display, messages, eventBus).open();
                }
            }
        });
    }
}
