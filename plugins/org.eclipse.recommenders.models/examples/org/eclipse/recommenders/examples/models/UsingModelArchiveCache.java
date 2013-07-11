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
package org.eclipse.recommenders.examples.models;

import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.recommenders.models.ModelArchiveCoordinate;
import org.eclipse.recommenders.models.ModelRepository;
import org.eclipse.recommenders.models.ModelRepository.ModelRepositoryEvents;
import org.eclipse.recommenders.models.ProjectCoordinate;
import org.eclipse.recommenders.utils.Pair;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.eventbus.Subscribe;

@SuppressWarnings("unused")
public class UsingModelArchiveCache {

    void downloadModelArchive(final ModelArchiveCoordinate model, final ModelRepository repository) throws IOException {
        repository.resolve(model, newMonitor());
    }

    void findLocalModelArchive(final ModelArchiveCoordinate model, final ModelRepository repository) throws IOException {
        if (!repository.getLocation(model).isPresent()) {
            repository.resolve(model, newMonitor());
        }
    }

    void deleteCachedModelArchive(final ModelArchiveCoordinate model, final ModelRepository repository)
            throws IOException {
        repository.delete(model, newMonitor());
    }

    void deleteIndex(final ModelRepository repository) throws IOException {
        repository.delete(ModelRepository.INDEX, newMonitor());
    }

    void findAllModelArtifacts(final ProjectCoordinate[] gavs, final ModelRepository cache,
            final IModelArchiveCoordinateProvider[] modelProviders) {

        Table<ProjectCoordinate, String, Pair<ModelArchiveCoordinate, Boolean>> mappings = HashBasedTable.create();
        for (ProjectCoordinate projectCoord : gavs) {
            for (IModelArchiveCoordinateProvider modelProvider : modelProviders) {
                ModelArchiveCoordinate modelCoord = modelProvider.find(projectCoord).orNull();
                if (modelCoord != null) {
                    boolean cached = cache.isCached(modelCoord);
                    mappings.put(projectCoord, modelProvider.getType(), Pair.newPair(modelCoord, cached));
                }
            }
        }
        // update ui...
    }

    @Subscribe
    void onEvent(final ModelRepositoryEvents.ModelArchiveCacheOpenedEvent e) {
        // TODO check if a new index is available and download it

    }

    @Subscribe
    void onEvent(final ModelRepositoryEvents.ModelArchiveCacheClosedEvent e) {
        // TODO persists what needs to be persisted
    }

    @Subscribe
    void onEvent(final ModelRepositoryEvents.ModelArchiveInstalledEvent e) {
        // TODO delete old cached keys, and reload the models currently required
    }

    private IProgressMonitor newMonitor() {
        // TODO Auto-generated method stub
        return null;
    }
}
