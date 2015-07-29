/**
 * Copyright (c) 2015 Pawel Nowak.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.recommenders.internal.news.rcp;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.List;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.recommenders.internal.news.rcp.l10n.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

public class FeedDialog extends TitleAreaDialog {

    @VisibleForTesting
    static final List<String> ACCEPTED_PROTOCOLS = ImmutableList.of("http", "https"); //$NON-NLS-1$ , //$NON-NLS-2$

    private final List<FeedDescriptor> existingDescriptors;
    private FeedDescriptor feed;
    private Text nameValue;
    private Text urlValue;
    private Text pollingIntervalValue;

    public FeedDialog(Shell parentShell, List<FeedDescriptor> existingDescriptors) {
        this(parentShell, null, existingDescriptors);
    }

    public FeedDialog(Shell parentShell, FeedDescriptor feed, List<FeedDescriptor> existingDescriptors) {
        super(parentShell);
        this.feed = feed;
        this.existingDescriptors = existingDescriptors;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        if (feed != null) {
            newShell.setText(Messages.FEED_DIALOG_TITLE_EDIT);
        } else {
            newShell.setText(Messages.FEED_DIALOG_TITLE_NEW);
        }
    }

    @Override
    public void create() {
        super.create();
        if (feed != null) {
            setTitle(Messages.FEED_DIALOG_TITLE_EDIT);
        } else {
            setTitle(Messages.FEED_DIALOG_TITLE_NEW);
        }
        setMessage(Messages.FEED_DIALOG_DESCRIPTION);
        setHelpAvailable(false);
        setTitleImage(CommonImages.RSS_DIALOG_TITLE.createImage());
        super.getButton(OK).setEnabled(false);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        Composite container = new Composite(area, SWT.NONE);
        container.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout layout = new GridLayout(2, false);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        container.setLayout(layout);
        createFeed(container);
        return area;
    }

    private void createFeed(Composite container) {
        Label name = new Label(container, SWT.NONE);
        name.setText(Messages.FIELD_LABEL_FEED_NAME);
        GridData gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        nameValue = new Text(container, SWT.BORDER);
        if (feed != null) {
            nameValue.setText(feed.getName());
        }
        nameValue.setLayoutData(gridData);
        Label url = new Label(container, SWT.NONE);
        url.setText(Messages.FIELD_LABEL_URL);
        urlValue = new Text(container, SWT.BORDER);
        if (feed != null) {
            urlValue.setText(feed.getUrl().toString());
        }
        urlValue.setLayoutData(gridData);
        Label pollingInterval = new Label(container, SWT.NONE);
        pollingInterval.setText(Messages.FIELD_LABEL_POLLING_INTERVAL);
        pollingIntervalValue = new Text(container, SWT.BORDER);
        pollingIntervalValue.setTextLimit(4);
        pollingIntervalValue.setText(Constants.DEFAULT_POLLING_INTERVAL.toString());
        if (feed != null) {
            pollingIntervalValue.setText(feed.getPollingInterval());
        }
        pollingIntervalValue.setLayoutData(gridData);
        nameValue.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                updateDialog();
            }
        });
        urlValue.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                updateDialog();
            }
        });
        pollingIntervalValue.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                updateDialog();
            }
        });
    }

    @Override
    protected boolean isResizable() {
        return false;
    }

    @Override
    protected void okPressed() {
        feed = new FeedDescriptor(urlValue.getText(), nameValue.getText(), pollingIntervalValue.getText());
        super.okPressed();
    }

    public FeedDescriptor getFeed() {
        return feed;
    }

    private void updateDialog() {
        setErrorMessage(validateFeedDialog(feed, nameValue.getText(), urlValue.getText(),
                pollingIntervalValue.getText(), existingDescriptors));
        if (getErrorMessage() == null) {
            super.getButton(OK).setEnabled(true);
        } else {
            super.getButton(OK).setEnabled(false);
        }
    }

    @VisibleForTesting
    static String validateFeedDialog(FeedDescriptor currentFeed, String name, String url, String pollingInterval,
            List<FeedDescriptor> existingDescriptors) {
        FeedDescriptor duplicateFeed = getFeedWithDuplicateUrl(url, currentFeed, existingDescriptors).orNull();
        URI feedUri = parseUriQuietly(url).orNull();
        if (Strings.isNullOrEmpty(name)) {
            return Messages.FEED_DIALOG_ERROR_EMPTY_NAME;
        } else if (Strings.isNullOrEmpty(url)) {
            return Messages.FEED_DIALOG_ERROR_EMPTY_URL;
        } else if (feedUri == null || !feedUri.isAbsolute()) {
            return Messages.FEED_DIALOG_ERROR_INVALID_URL;
        } else if (!isUriProtocolSupported(feedUri, ACCEPTED_PROTOCOLS)) {
            return MessageFormat.format(Messages.FEED_DIALOG_ERROR_PROTOCOL_UNSUPPORTED, url,
                    Joiner.on(", ").join(ACCEPTED_PROTOCOLS)); //$NON-NLS-1$
        } else if (duplicateFeed != null) {
            return MessageFormat.format(Messages.FEED_DIALOG_ERROR_DUPLICATE_FEED, duplicateFeed.getName());
        } else if (!pollingInterval.matches("[0-9]+")) { //$NON-NLS-1$
            return Messages.FEED_DIALOG_ERROR_POLLING_INTERVAL_DIGITS_ONLY;
        }

        return null;
    }

    private static Optional<URI> parseUriQuietly(String uriString) {
        try {
            return Optional.of(new URI(uriString));
        } catch (URISyntaxException e) {
            return Optional.absent();
        }
    }

    private static boolean isUriProtocolSupported(URI uri, List<String> protocols) {
        if (uri == null) {
            return false;
        }
        for (String protocol : protocols) {
            if (protocol.equalsIgnoreCase(uri.getScheme()) || protocol.equalsIgnoreCase(uri.toString())) {
                return true;
            }
        }
        return false;
    }

    private static Optional<FeedDescriptor> getFeedWithDuplicateUrl(String url, FeedDescriptor feed,
            List<FeedDescriptor> descriptors) {
        for (FeedDescriptor compare : descriptors) {
            if (feed == compare) {
                continue;
            }

            if (url.equals(compare.getUrl().toString())) {
                return Optional.of(compare);
            }
        }
        return Optional.absent();
    }
}
