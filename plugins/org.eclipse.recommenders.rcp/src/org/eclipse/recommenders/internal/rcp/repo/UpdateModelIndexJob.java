/**
 * Copyright (c) 2010, 2012 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.repo;

import static org.apache.commons.io.FileUtils.cleanDirectory;
import static org.eclipse.recommenders.internal.rcp.repo.ModelRepositoryIndex.INDEX_COORDINATE;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
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
        super("Recommenders: Updating model index.");
        this.index = index;
        this.repo = repo;
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        try {
            File location = index.getLocation();
            if (doesNotExistOrIsAlmostEmptyFolder(location)) {
                downloadAndUnzipIndex(monitor);
            } else if (!repo.isLatest(INDEX_COORDINATE)) {
                repo.delete(INDEX_COORDINATE);
                downloadAndUnzipIndex(monitor);
            }
        } catch (Exception e) {
            log.debug("Updating index cancelled.", e);
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
            repo.resolve(INDEX_COORDINATE, monitor);
        } catch (NullPointerException e) {
            // we may have no internet... XXX this needs investigation
        }

        File f = repo.location(INDEX_COORDINATE);
        if (!f.exists()) {
            return;
        }
        index.close();
        File basedir = index.getLocation();
        cleanDirectory(basedir);
        Zips.unzip(f, basedir);
    }
}
