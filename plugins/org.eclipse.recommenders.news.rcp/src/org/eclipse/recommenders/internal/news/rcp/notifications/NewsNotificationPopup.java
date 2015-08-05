/**
 * Copyright (c) 2015 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 *    Pawel Nowak - displaying only latest feeds, refactor
 */
package org.eclipse.recommenders.internal.news.rcp.notifications;

import static org.eclipse.recommenders.internal.news.rcp.FeedEvents.createFeedMessageReadEvent;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.recommenders.internal.news.rcp.BrowserUtils;
import org.eclipse.recommenders.internal.news.rcp.FeedDescriptor;
import org.eclipse.recommenders.internal.news.rcp.MessageUtils;
import org.eclipse.recommenders.internal.news.rcp.PollingResult;
import org.eclipse.recommenders.internal.news.rcp.l10n.Messages;
import org.eclipse.recommenders.news.rcp.IFeedMessage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;

import com.google.common.eventbus.EventBus;

public class NewsNotificationPopup extends AbstractNotificationPopup {

    private static final int DELAY_CLOSE_MS = 4000;
    private static final int DEFAULT_NOTIFICATION_MESSAGES = 6;

    private final Map<FeedDescriptor, PollingResult> messages;
    private final EventBus eventBus;

    public NewsNotificationPopup(Display display, Map<FeedDescriptor, PollingResult> messages, EventBus eventBus) {
        super(display);
        this.messages = messages;
        this.eventBus = eventBus;
        setFadingEnabled(true);
        setDelayClose(DELAY_CLOSE_MS);
    }

    @Override
    protected void createContentArea(Composite composite) {
        super.createContentArea(composite);
        composite.setLayout(new GridLayout(1, true));
        Map<FeedDescriptor, PollingResult> sortedMap = MessageUtils.sortByDate(messages);

        processNotificationData(composite, sortedMap);

        Label hint = new Label(composite, SWT.NONE);
        GridDataFactory.fillDefaults().hint(AbstractNotificationPopup.MAX_WIDTH, SWT.DEFAULT).applyTo(hint);
        hint.setText(Messages.HINT_MORE_MESSAGES);
    }

    private void processNotificationData(Composite composite, Map<FeedDescriptor, PollingResult> sortedMap) {
        int feedCounter = 0;
        int messagesPerFeed = DEFAULT_NOTIFICATION_MESSAGES < sortedMap.size() ? 1
                : DEFAULT_NOTIFICATION_MESSAGES / sortedMap.size();
        for (Entry<FeedDescriptor, PollingResult> entry : sortedMap.entrySet()) {
            if (feedCounter < DEFAULT_NOTIFICATION_MESSAGES) {
                Label feedTitle = new Label(composite, SWT.NONE);
                GridDataFactory.fillDefaults().hint(AbstractNotificationPopup.MAX_WIDTH, SWT.DEFAULT)
                        .applyTo(feedTitle);
                feedTitle.setFont(CommonFonts.BOLD);
                feedTitle.setText(entry.getKey().getName());

                feedCounter = feedCounter
                        + processMessages(composite, entry.getValue().getMessages(), messagesPerFeed, entry.getKey());
            }
        }
    }

    private int processMessages(Composite composite, List<IFeedMessage> messages, int calculatedMessagesPerFeed,
            final FeedDescriptor feed) {
        int messagesPerFeed = 0;
        for (final IFeedMessage message : messages) {
            if (messagesPerFeed < calculatedMessagesPerFeed) {
                Link link = new Link(composite, SWT.WRAP);
                link.setText(MessageFormat.format("<a href=\"{1}\">{0}</a>", message.getTitle(), message.getUrl())); //$NON-NLS-1$
                GridDataFactory.fillDefaults().hint(AbstractNotificationPopup.MAX_WIDTH, SWT.DEFAULT).applyTo(link);
                link.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        BrowserUtils.openInDefaultBrowser(message.getUrl(), feed.getParameters());
                        eventBus.post(createFeedMessageReadEvent(message.getId()));
                    }
                });
                messagesPerFeed++;
            }
        }
        return messagesPerFeed;
    }

    @Override
    protected String getPopupShellTitle() {
        return Messages.NOTIFICATION_TITLE;
    }
}
