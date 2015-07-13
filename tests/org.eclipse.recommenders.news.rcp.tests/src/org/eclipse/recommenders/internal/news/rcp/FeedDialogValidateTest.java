/**
 * Copyright (c) 2015 Pawel Nowak.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.recommenders.internal.news.rcp;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;

import org.eclipse.recommenders.internal.news.rcp.l10n.Messages;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.Lists;

@RunWith(Parameterized.class)
public class FeedDialogValidateTest {

    private static final String EMPTY_STRING = "";
    private static final String VALID_FEED_ID = "feed";
    private static final String VALID_FEED_NAME = "123ZCERDHJD.żźęę@@!!$@#%@#%,./,l;";
    private static final String INVALID_FEED_PROTOCOL = "ftp://eclipse.org";
    private static final String INVALID_FEED_URL = "http://eclipse>org";
    private static final String DUPLICATE_FEED_URL = "http://planeteclipse.org/planet/rss20.xml";
    private static final String VALID_FEED_URL = "http://eclipse.org";
    private static final String INVALID_FEED_POLLING_INTERVAL = "12a";
    private static final String VALID_FEED_POLLING_INTERVAL = "23";
    private static final String NO_ERROR = null;

    private FeedDescriptor feed;
    private String name;
    private String url;
    private String pollingInterval;
    private String expectedMessage;
    private NewsRcpPreferences preferences;

    public FeedDialogValidateTest(FeedDescriptor feed, String name, String url, String pollingInterval,
            String expectedMessage) {
        this.feed = feed;
        this.name = name;
        this.url = url;
        this.pollingInterval = pollingInterval;
        this.expectedMessage = expectedMessage;
    }

    @Parameters
    public static Collection<Object[]> scenarios() {
        List<Object[]> scenarios = Lists.newArrayList();

        scenarios.add(
                new Object[] { null, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, Messages.FEED_DIALOG_ERROR_EMPTY_NAME });
        scenarios.add(new Object[] { null, VALID_FEED_NAME, EMPTY_STRING, EMPTY_STRING,
                Messages.FEED_DIALOG_ERROR_EMPTY_URL });
        scenarios.add(new Object[] { null, VALID_FEED_NAME, INVALID_FEED_PROTOCOL, EMPTY_STRING,
                MessageFormat.format(Messages.FEED_DIALOG_ERROR_PROTOCOL_UNSUPPORTED, INVALID_FEED_PROTOCOL) });
        scenarios.add(new Object[] { null, VALID_FEED_NAME, INVALID_FEED_URL, EMPTY_STRING,
                Messages.FEED_DIALOG_ERROR_INVALID_URL });
        scenarios.add(new Object[] { null, VALID_FEED_NAME, DUPLICATE_FEED_URL, EMPTY_STRING,
                MessageFormat.format(Messages.FEED_DIALOG_ERROR_DUPLICATE_FEED, VALID_FEED_ID) });
        scenarios.add(new Object[] { null, VALID_FEED_NAME, VALID_FEED_URL, INVALID_FEED_POLLING_INTERVAL,
                Messages.FEED_DIALOG_ERROR_POLLING_INTERVAL_DIGITS_ONLY });
        scenarios.add(new Object[] { null, VALID_FEED_NAME, VALID_FEED_URL, VALID_FEED_POLLING_INTERVAL, NO_ERROR });
        scenarios.add(new Object[] { TestUtils.enabled(VALID_FEED_ID), VALID_FEED_NAME, VALID_FEED_URL,
                VALID_FEED_POLLING_INTERVAL, NO_ERROR });

        return scenarios;
    }

    @Test
    public void testValidateFeedDialog() {
        preferences = mock(NewsRcpPreferences.class);
        List<FeedDescriptor> feeds = Lists.newArrayList(TestUtils.enabled(VALID_FEED_ID));
        when(preferences.getFeedDescriptors()).thenReturn(feeds);

        assertThat(FeedDialog.validateFeedDialog(feed, name, url, pollingInterval, preferences),
                is(equalTo(expectedMessage)));
    }
}
