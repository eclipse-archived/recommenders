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

import static java.util.concurrent.TimeUnit.MINUTES;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.recommenders.internal.news.rcp.NewsRcpPreferences;
import org.eclipse.recommenders.internal.news.rcp.PreferenceConstants;
import org.eclipse.recommenders.internal.news.rcp.l10n.Messages;
import org.eclipse.recommenders.news.api.poll.INewsPollingService;

@Creatable
@Singleton
public class PollingManager {

    private final NewsRcpPreferences preferences;
    private final INewsPollingService pollingService;
    private final IEventBroker eventBroker;

    private final Object jobLock = new Object();

    @Nullable
    private Job job;

    private int pollingDelay;

    @Inject
    public PollingManager(NewsRcpPreferences preferences, INewsPollingService pollingService, IEventBroker eventBroker,
            @Preference(PreferenceConstants.POLLING_DELAY) int pollingDelay) {
        this.preferences = preferences;
        this.pollingService = pollingService;
        this.eventBroker = eventBroker;
        this.pollingDelay = pollingDelay;
    }

    @Inject
    public void setEnabled(@Preference(PreferenceConstants.NEWS_ENABLED) boolean enabled) {
        synchronized (jobLock) {
            if (enabled && job == null) {
                job = new PollFeedsJob(Messages.JOB_NAME_POLLING_FEEDS, true, preferences, pollingService, eventBroker);
                job.setPriority(Job.DECORATE);
                job.schedule(MINUTES.toMillis(pollingDelay));
            } else if (!enabled && job != null) {
                job.cancel();
                job = null;
            }
        }
    }

    @Inject
    public void updatePollingDelay(@Preference(PreferenceConstants.POLLING_DELAY) int pollingDelay) {
        this.pollingDelay = pollingDelay;
    }
}
