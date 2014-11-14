/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.stacktraces.rcp;

import static com.google.common.collect.Iterables.size;
import static java.util.Arrays.asList;

import java.util.Arrays;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;

public final class Jobs {

    private Jobs() {
        throw new IllegalStateException("Not meant to be instantiated"); //$NON-NLS-1$
    }

    public static final ISchedulingRule EXCLUSIVE = new SequentialSchedulingRule();

    public static IProgressMonitor getProgressGroup() {
        return Job.getJobManager().createProgressGroup();
    }

    public static void parallel(String task, Job... jobs) {
        parallel(task, asList(jobs));
    }

    public static void parallel(String task, Iterable<Job> jobs) {
        IProgressMonitor group = getProgressGroup();
        group.beginTask(task, size(jobs));
        for (Job job : jobs) {
            job.setProgressGroup(group, 1);
            job.schedule();
        }
    }

    public static void sequential(String task, Job... jobs) {
        sequential(task, Arrays.asList(jobs));
    }

    public static void sequential(String task, Iterable<Job> jobs) {
        ISchedulingRule rule = new SequentialSchedulingRule();
        IProgressMonitor group = getProgressGroup();
        group.beginTask(task, size(jobs));
        for (Job job : jobs) {
            job.setRule(rule);
            job.setProgressGroup(group, 1);
            job.schedule();
        }
    }

    public static final class SequentialSchedulingRule implements ISchedulingRule {
        @Override
        public boolean isConflicting(ISchedulingRule rule) {
            return rule == this;
        }

        @Override
        public boolean contains(ISchedulingRule rule) {
            return rule == this;
        }
    }

}
