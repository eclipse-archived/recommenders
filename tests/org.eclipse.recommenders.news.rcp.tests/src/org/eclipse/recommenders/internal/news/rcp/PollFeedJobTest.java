/**
 * Copyright (c) 2015 Pawel Nowak.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.recommenders.internal.news.rcp;

import static org.eclipse.recommenders.internal.news.rcp.TestUtils.enabled;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import java.net.MalformedURLException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableSet;

@RunWith(MockitoJUnitRunner.class)
public class PollFeedJobTest {

    @Test
    public void testJobBelongsToGivenTheSameFamily() throws MalformedURLException {
        assertThat(new PollFeedJob(ImmutableSet.of(enabled("test"))).belongsTo(Constants.POLL_FEED_JOB_FAMILY),
                is(true));
    }

    @Test
    public void testJobBelongsToGivenDifferentFamily() throws MalformedURLException {
        assertThat(new PollFeedJob(ImmutableSet.of(enabled("test"))).belongsTo("rndm"), is(false));
    }

    @Test
    public void testJobWillBeCanceledIfPreferencesAreDisabled() {
        IProgressMonitor monitor = mock(IProgressMonitor.class);
        NewsRcpPreferences preferences = mock(NewsRcpPreferences.class);
        FeedDescriptor feed = mock(FeedDescriptor.class);
        when(preferences.isEnabled()).thenReturn(false);
        when(monitor.isCanceled()).thenReturn(true);

        PollFeedJob sut = new PollFeedJob(ImmutableSet.of(feed));

        assertThat(sut.run(monitor), is(Status.CANCEL_STATUS));
        verifyZeroInteractions(feed);
    }
}
