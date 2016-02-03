/**
 * Copyright (c) 2015 Pawel Nowak.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.recommenders.news.rcp;

import java.util.Date;
import java.util.Map;

import org.eclipse.recommenders.internal.news.rcp.FeedDescriptor;

/**
 * Implementors are responsible for polling feeds.
 *
 */
public interface IPollFeedJob {
    /**
     *
     * @return Messages that have been polled
     */
    Map<FeedDescriptor, IPollingResult> getMessages();

    /**
     *
     * @return Map of Feed and its polling date.
     */
    Map<FeedDescriptor, Date> getPollDates();

}
