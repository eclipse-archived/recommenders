/**
 * Copyright (c) 2015 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.news.rcp.notifications;

import static org.eclipse.recommenders.internal.news.rcp.FeedEvents.createFeedMessageReadEvent;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.recommenders.internal.news.rcp.FeedDescriptor;
import org.eclipse.recommenders.internal.news.rcp.Messages;
import org.eclipse.recommenders.news.rcp.IFeedMessage;
import org.eclipse.recommenders.rcp.utils.BrowserUtils;
import org.eclipse.recommenders.rcp.utils.Shells;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;

import com.google.common.eventbus.EventBus;

public class NewsNotificationPopup extends AbstractNotificationPopup {

    private static final int DELAY_CLOSE_MS = 4000;

    private final Map<FeedDescriptor, List<IFeedMessage>> messages;
    private final EventBus eventBus;

    public NewsNotificationPopup(Map<FeedDescriptor, List<IFeedMessage>> messages, EventBus eventBus) {
        super(Shells.getDisplay());
        this.messages = messages;
        this.eventBus = eventBus;
        setFadingEnabled(true);
        setDelayClose(DELAY_CLOSE_MS);
    }

    @Override
    protected void createContentArea(Composite composite) {
        super.createContentArea(composite);
        composite.setLayout(new GridLayout(1, true));

        for (Entry<FeedDescriptor, List<IFeedMessage>> entry : messages.entrySet()) {
            Label feedTitle = new Label(composite, SWT.NONE);
            GridDataFactory.fillDefaults().hint(AbstractNotificationPopup.MAX_WIDTH, SWT.DEFAULT).applyTo(feedTitle);
            feedTitle.setFont(CommonFonts.BOLD);
            feedTitle.setText(entry.getKey().getName());
            for (final IFeedMessage message : entry.getValue()) {
                Link link = new Link(composite, SWT.WRAP);
                link.setText(MessageFormat.format("<a href=\"{1}\">{0}</a>", message.getTitle(), message.getUrl())); //$NON-NLS-1$
                GridDataFactory.fillDefaults().hint(AbstractNotificationPopup.MAX_WIDTH, SWT.DEFAULT).applyTo(link);
                link.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        BrowserUtils.openInExternalBrowser(e.text);
                        eventBus.post(createFeedMessageReadEvent(message.getId()));
                    }
                });

            }
        }
    }

    @Override
    protected String getPopupShellTitle() {
        return Messages.NOTIFICATION_TITLE;
    }
}
