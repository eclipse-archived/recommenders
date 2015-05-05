/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 *    Olav Lenz - Move to new file.
 */
package org.eclipse.recommenders.models.rcp.actions;

import static java.text.MessageFormat.format;

import java.util.Set;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.recommenders.internal.models.rcp.DownloadModelArchiveJob;
import org.eclipse.recommenders.internal.models.rcp.EclipseModelRepository;
import org.eclipse.recommenders.internal.models.rcp.l10n.Messages;
import org.eclipse.recommenders.models.ModelCoordinate;
import org.eclipse.recommenders.rcp.utils.Jobs;

import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;

public class TriggerModelDownloadForModelCoordinatesAction extends Action {

    private EclipseModelRepository repo;

    private Set<ModelCoordinate> mcs = Sets.newHashSet();

    private EventBus bus;

    public TriggerModelDownloadForModelCoordinatesAction(String text, EclipseModelRepository repo, EventBus bus) {
        super(text);
        this.repo = repo;
        this.bus = bus;
    }

    public TriggerModelDownloadForModelCoordinatesAction(String text, Set<ModelCoordinate> mcs,
            EclipseModelRepository repo, EventBus bus) {
        this(text, repo, bus);
        this.mcs = mcs;
    }

    @Override
    public void run() {
        triggerDownloadForModelCoordinates(mcs);
    }

    public void triggerDownloadForModelCoordinates(Set<ModelCoordinate> mcs) {
        Set<Job> jobs = Sets.newHashSet();
        for (ModelCoordinate mc : mcs) {
            jobs.add(new DownloadModelArchiveJob(repo, mc, false, bus));
        }
        Jobs.sequential(format(Messages.JOB_DOWNLOADING_MODELS, jobs.size()), jobs);
    }
}
