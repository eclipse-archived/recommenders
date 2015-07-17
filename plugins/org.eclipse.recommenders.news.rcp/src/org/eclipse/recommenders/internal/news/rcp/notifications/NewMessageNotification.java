/**
 * Copyright (c) 2015 Pawel Nowak.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Based on http://git.eclipse.org/c/epp/org.eclipse.epp.logging.git/tree/bundles/org.eclipse.epp.logging.aeri.ui/src/org/eclipse/epp/internal/logging/aeri/ui/notifications/Notification.java
 */
package org.eclipse.recommenders.internal.news.rcp.notifications;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Platform;
import org.eclipse.mylyn.commons.notifications.ui.AbstractUiNotification;
import org.eclipse.recommenders.internal.news.rcp.FeedDescriptor;
import org.eclipse.recommenders.internal.news.rcp.l10n.Messages;
import org.eclipse.recommenders.news.rcp.IFeedMessage;
import org.eclipse.swt.graphics.Image;

import com.google.common.eventbus.EventBus;

@SuppressWarnings("restriction")
public class NewMessageNotification extends AbstractUiNotification {

    private final EventBus bus;
    private final Map<FeedDescriptor, List<IFeedMessage>> messages;

    public NewMessageNotification(EventBus bus, Map<FeedDescriptor, List<IFeedMessage>> messages) {
        super("org.eclipse.recommenders.news.rcp.NewMessages");
        this.bus = bus;
        this.messages = messages;
    }

    public EventBus getBus() {
        return bus;
    }

    public Map<FeedDescriptor, List<IFeedMessage>> getMessages() {
        return messages;
    }

    @Override
    public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
        return Platform.getAdapterManager().getAdapter(this, adapter);
    }

    @Override
    public Image getNotificationImage() {
        return null;
    }

    @Override
    public Image getNotificationKindImage() {
        return null;
    }

    @Override
    public void open() {
    }

    @Override
    public Date getDate() {
        return new Date();
    }

    @Override
    public String getDescription() {
        return Messages.LABEL_DESKTOP_NOTIFICATION_DESCRIPTION;
    }

    @Override
    public String getLabel() {
        return Messages.LABEL_DESKTOP_NOTIFICATION_LABEL;
    }

}
