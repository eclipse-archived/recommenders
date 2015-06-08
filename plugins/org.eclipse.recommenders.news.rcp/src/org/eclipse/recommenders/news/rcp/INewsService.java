/**
 * Copyright (c) 2015 Codetrails GmbH. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Johannes Dorn - initial API and implementation.
 */
package org.eclipse.recommenders.news.rcp;

import java.util.List;
import java.util.Map;

import org.eclipse.recommenders.internal.news.rcp.FeedDescriptor;
import org.eclipse.recommenders.internal.news.rcp.FeedEvents.FeedMessageReadEvent;

public interface INewsService {

    Map<FeedDescriptor, List<IFeedMessage>> getMessages(int countPerFeed);

    void start();

    void removeFeed(FeedDescriptor feed);

    void handleMessageRead(FeedMessageReadEvent event);

    boolean shouldPoll(FeedDescriptor feed);

    void jobDone(IPollFeedJob job);

}
