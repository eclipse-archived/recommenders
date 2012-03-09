/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
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
import static org.eclipse.recommenders.internal.rcp.repo.RepositoryUtils.newArtifact;
import static org.eclipse.ui.internal.misc.StatusUtil.newStatus;

import java.io.File;
import java.io.FileInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.recommenders.internal.rcp.repo.wiring.Activator;
import org.eclipse.recommenders.rcp.repo.IModelRepository;
import org.eclipse.recommenders.rcp.repo.IModelRepositoryIndex;
import org.sonatype.aether.artifact.Artifact;

import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import com.google.common.io.Files;
import com.google.common.io.InputSupplier;

@SuppressWarnings("restriction")
public class UpdateModelIndexJob extends Job {

    private final Artifact indexArtifact = newArtifact("org.eclipse.recommenders:index:zip:0.0.0");
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
            index.close();
            File location = index.getLocation();
            if (doesNotExistOrIsAlmostEmptyFolder(location)) {
                downloadAndUnzipIndex(monitor);
            } else if (!repo.isLatest(indexArtifact)) {
                repo.delete(indexArtifact);
                index.close();
                cleanDirectory(location);
                downloadAndUnzipIndex(monitor);
            }
            index.open();
        } catch (Exception e) {
            return newStatus(Activator.PLUGIN_ID, e);
        }
        return Status.OK_STATUS;
    }

    private boolean doesNotExistOrIsAlmostEmptyFolder(File location) {
        return !location.exists() || location.listFiles().length < 2;
        // 2 = if this folder contains an index, there must be more than one file...
        // on mac, we often have hidden files in the folder. This is just a simple heuristic.
    }

    private void downloadAndUnzipIndex(IProgressMonitor monitor) throws Exception {
        repo.resolve(indexArtifact, monitor);

        File f = repo.location(indexArtifact);
        if (!f.exists()) {
            return;
        }

        File basedir = index.getLocation();
        InputSupplier<FileInputStream> fis = Files.newInputStreamSupplier(f);
        ZipInputStream zis = new ZipInputStream(fis.getInput());
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            if (!entry.isDirectory()) {
                final File file = new File(basedir, entry.getName());
                Files.createParentDirs(file);
                Files.write(ByteStreams.toByteArray(zis), file);
            }
        }
        Closeables.closeQuietly(zis);
    }
}
