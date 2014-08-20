/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 *    Andreas Sewe - adapted to API changes.
 */
package org.eclipse.recommenders.examples.models;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.eclipse.recommenders.models.DownloadCallback;
import org.eclipse.recommenders.models.IModelRepository;
import org.eclipse.recommenders.models.ModelCoordinate;

import com.google.common.base.Optional;

@SuppressWarnings("unused")
public class UsingModelRepository {

    private static final boolean PREFETCH = true;
    private static final boolean FORCE_DOWNLOAD = true;

    /**
     * Gets a cached model archive if available. It also requests that the model archive be prefetch in the background
     * if possible.
     *
     * This operation can be assumed to finish quickly.
     */
    public void getCachedModelArchive(final ModelCoordinate mc, final IModelRepository repository) {
        Optional<File> modelArchive = repository.getLocation(mc, PREFETCH);
    }

    /**
     * Updates a model archive if necessary, i.e., if the one currently cached is out-dated. When exactly a cached model
     * archive is considered to be out-dated and will be downloaded anew is up to the <code>IModelRepository</code>
     * implementation.
     *
     * This operation can potentially, if network I/O is necessary, take some time.
     */
    public void updateModelArchiveIfNecessary(final ModelCoordinate mc, final IModelRepository repository) {
        Optional<File> modelArchive = repository.resolve(mc, !FORCE_DOWNLOAD);
        if (modelArchive.isPresent()) {
            // This may be a freshly downloaded model archive or a cached model archive that is not yet considered
            // out-dated.
            modelArchive.get();
        } else {
            // If a model archive is not present, this means that it either does not exist remotely or that the cached
            // information that it does not exist is not yet consider out-dated.
        }
    }

    /**
     * Updates a model archive if necessary, i.e., if the one currently cached is out-dated, in a background thread. The
     * <code>IModelRepository</code> is thread-safe but not necessarily lock-free. Also, having more than a small number
     * of resolutions in progress is often undesirable.
     *
     * Reporting the progress of the initiated downloads (if any) can be done using a <code>DownloadCallback</code>.
     * Care needs to be taken during progress reporting as multiple downloads may run concurrently.
     */
    public void updateModelArchiveIfNecessaryInBackground(final ModelCoordinate mc, final IModelRepository repository) {
        final DownloadCallback callback = new DownloadCallback() {

            @Override
            public void downloadInitiated(String path) {
                // Zero or more than one resource (path) may be downloading when resolving a single model archive. The
                // path allows to distinguish between them.
                System.out.println("Now downloading " + path);
            }
        };

        ExecutorService executor = Executors.newFixedThreadPool(2);
        Future<Optional<File>> modelArchive = executor.submit(new Callable<Optional<File>>() {

            @Override
            public Optional<File> call() throws Exception {
                return repository.resolve(mc, !FORCE_DOWNLOAD, callback);
            }
        });
    }

    void deleteCachedModelArchive(final ModelCoordinate mc, final IModelRepository repository) throws IOException {
        // repository.delete(model);
    }

    void deleteIndex(final IModelRepository repository) throws IOException {
        // not working anymore
        // repository.delete(IModelRepository.INDEX);
    }
}
