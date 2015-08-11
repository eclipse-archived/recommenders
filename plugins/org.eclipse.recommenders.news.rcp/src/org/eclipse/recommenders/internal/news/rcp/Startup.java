/**
 * Copyright (c) 2015 Codetrails GmbH. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Johannes Dorn - initial API and implementation.
 */
package org.eclipse.recommenders.internal.news.rcp;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.recommenders.internal.news.rcp.l10n.Messages;
import org.eclipse.recommenders.news.rcp.INewsService;
import org.eclipse.ui.IStartup;

public class Startup implements IStartup {

    @Inject
    private INewsService service;
    @Inject
    private NewsRcpPreferences preferences;

    public Startup() {
        NewsRcpModule.initiateContext(this);
    }

    @Override
    public void earlyStartup() {
        final Job job = new Job(Messages.STARTUP_JOB_NAME) {

            @Override
            protected IStatus run(IProgressMonitor monitor) {
                service.start();
                return Status.OK_STATUS;
            }

            @Override
            public boolean shouldRun() {
                return preferences.isEnabled();
            }
        };
        job.setSystem(true);
        job.setPriority(Job.DECORATE);
        job.schedule(TimeUnit.MINUTES.toMillis(preferences.getStartupDelay()));
    }

}
