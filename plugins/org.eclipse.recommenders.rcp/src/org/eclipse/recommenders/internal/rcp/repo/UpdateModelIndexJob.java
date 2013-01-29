/**
 * Copyright (c) 2010, 2012 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 *    Olav Lenz - externalize Strings.
 */
package org.eclipse.recommenders.internal.rcp.repo;

import static org.apache.commons.io.FileUtils.cleanDirectory;
import static org.eclipse.recommenders.internal.rcp.repo.ModelRepositoryIndex.INDEX_ARTIFACT;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.recommenders.rcp.l10n.Messages;
import org.eclipse.recommenders.rcp.repo.IModelRepository;
import org.eclipse.recommenders.rcp.repo.IModelRepositoryIndex;
import org.eclipse.recommenders.utils.Zips;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateModelIndexJob extends Job {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final IModelRepositoryIndex index;
    private final IModelRepository repo;

    public UpdateModelIndexJob(IModelRepositoryIndex index, IModelRepository repo) {
        super(Messages.JOB_UPDATE_MODEL_INDEX);
        this.index = index;
        this.repo = repo;
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        try {
            File location = index.getLocation();
            if (doesNotExistOrIsAlmostEmptyFolder(location)) {
                downloadAndUnzipIndex(monitor);
            } else if (!repo.isLatest(INDEX_ARTIFACT)) {
                repo.delete(INDEX_ARTIFACT);
                downloadAndUnzipIndex(monitor);
            }
        } catch (Exception e) {
            log.warn("Updating index cancelled.", e); //$NON-NLS-1$
            return Status.CANCEL_STATUS;
        } finally {
            index.open();
        }
        return Status.OK_STATUS;
    }

    private boolean doesNotExistOrIsAlmostEmptyFolder(File location) {
        return !location.exists() || location.listFiles().length < 2;
        // 2 = if this folder contains an index, there must be more than one file...
        // on mac, we often have hidden files in the folder. This is just a simple heuristic.
    }

    private void downloadAndUnzipIndex(IProgressMonitor monitor) throws Exception {
        try {
            repo.resolve(INDEX_ARTIFACT, monitor);
        } catch (NullPointerException e) {
            // we probably don't have internet... XXX this needs investigation
            log.warn("Couldn't download search index. No (direct) internet connection? Need a proxy?", e); //$NON-NLS-1$
        }

        File f = repo.location(INDEX_ARTIFACT);
        if (!f.exists()) {
            return;
        }
        index.close();
        File basedir = index.getLocation();
        cleanDirectory(basedir);
        Zips.unzip(f, basedir);
    }
}
