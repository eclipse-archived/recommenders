/**
 * Copyright (c) 2016 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Sewe - initial API and implementation.
 */
package org.eclipse.recommenders.internal.news.rcp.poll;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.recommenders.internal.news.rcp.NewsRcpPreferences;
import org.eclipse.recommenders.internal.news.rcp.l10n.Messages;
import org.eclipse.recommenders.news.api.poll.INewsPollingService;

public class PollNewsFeedsHandler {

    @Execute
    public void execute(NewsRcpPreferences preferences, INewsPollingService pollingService, IEventBroker eventBroker) {
        Job job = new PollFeedsJob(Messages.JOB_NAME_POLLING_FEEDS, false, preferences, pollingService, eventBroker);
        job.setPriority(Job.LONG);
        job.schedule();
    }
}
