/**
 * Copyright (c) 2015 Pawel Nowak.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.recommenders.internal.news.rcp;

import java.util.Collection;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.recommenders.news.rcp.IJobFacade;
import org.eclipse.recommenders.news.rcp.IPollFeedJob;

@SuppressWarnings("restriction")
public class JobFacade implements IJobFacade {
    private static final long START_DELAY = 0;

    @Override
    public boolean jobExists(IPollFeedJob job) {
        if (Job.getJobManager().find(job).length > 0) {
            return true;
        }
        return false;
    }

    @Override
    public void schedule(Collection<FeedDescriptor> feeds, final NewsService service) {
        final Job job = new PollFeedJob(Constants.JOB_FAMILY);
        if (!jobExists((IPollFeedJob) job)) {
            job.setSystem(true);
            job.setPriority(Job.DECORATE);
            job.addJobChangeListener(new JobChangeAdapter() {
                @Override
                public void done(IJobChangeEvent event) {
                    service.jobDone((IPollFeedJob) job);
                }
            });
            job.schedule();

        }

    }

}
