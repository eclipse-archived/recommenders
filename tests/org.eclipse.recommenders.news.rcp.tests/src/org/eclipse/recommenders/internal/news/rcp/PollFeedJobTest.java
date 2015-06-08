/**
 * Copyright (c) 2015 Pawel Nowak.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.recommenders.internal.news.rcp;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PollFeedJobTest {

    private static final String FIRST_ELEMENT = "first";
    private static final String SECOND_ELEMENT = "second";

    private Set<FeedDescriptor> feeds;

    @Before
    public void setup() {
        feeds = new HashSet<>();
    }

    @Test
    public void testFeedsWithSameIdBelongsTo() throws MalformedURLException {
        assertThat(new PollFeedJob(FIRST_ELEMENT, feeds).belongsTo(new PollFeedJob(FIRST_ELEMENT, feeds)), is(true));
        assertThat(new PollFeedJob(FIRST_ELEMENT, feeds).belongsTo(new PollFeedJob(FIRST_ELEMENT, feeds)), is(true));
    }

    @Test
    public void testFeedsWithDifferentIdDoesntBelongsTo() {
        PollFeedJob firstJob = new PollFeedJob(FIRST_ELEMENT, feeds);
        PollFeedJob secondJob = new PollFeedJob(SECOND_ELEMENT, feeds);
        assertThat(firstJob.belongsTo(secondJob), is(false));
        assertThat(secondJob.belongsTo(firstJob), is(false));
    }
}
