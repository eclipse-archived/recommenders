/**
 * Copyright (c) 2015 Pawel Nowak.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.recommenders.internal.news.rcp;

import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.recommenders.internal.news.rcp.l10n.Messages;
import org.eclipse.recommenders.news.rcp.IJobFacade;
import org.eclipse.recommenders.news.rcp.INewsService;

public class JobFacade implements IJobFacade {

    @Override
    public void scheduleNewsUpdate(final INewsService service, long delay) {
        cancelPollFeeds();
        final Job job = new Job(Messages.POLL_FEED_JOB_SCHEDULER_NAME) {

            @Override
            protected IStatus run(IProgressMonitor monitor) {
                service.pollFeeds();
                return Status.OK_STATUS;
            }
        };
        job.setSystem(true);
        job.setPriority(Job.DECORATE);
        job.schedule(delay);
    }

    @Override
    public void schedulePollFeeds(final INewsService service, Set<FeedDescriptor> feeds) {
        final PollFeedJob job = new PollFeedJob(feeds);
        job.addJobChangeListener(new JobChangeAdapter() {
            @Override
            public void done(IJobChangeEvent event) {
                service.jobDone(job);
            }
        });
        job.schedule();
    }

    @Override
    public void cancelPollFeeds() {
        Job.getJobManager().cancel(Constants.POLL_FEED_JOB_FAMILY);
    }
}
