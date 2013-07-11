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
package org.eclipse.recommenders.models;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;

import com.google.common.base.Optional;

/**
 * A {@link ModelRepository} is responsible for downloading and caching (file-based) model artifacts from a remote maven
 * repository. It has a local working directory where it stores model artifacts and some meta-data, and is configured
 * with a remote (maven) repository URL from which it fetches model artifacts on demand.
 */
public abstract class ModelRepository {

    /**
     * The coordinate under which the model search index of the remote model repository is addressable.
     */
    public static ModelArchiveCoordinate INDEX = new ModelArchiveCoordinate("org.eclipse.recommenders", "index",
            "index", "zip", "0.0.0-SNAPSHOT");

    /**
     * Utility method that checks whether the given coordinate is the index coordinate.
     */
    public static boolean isModelIndex(final ModelArchiveCoordinate coord) {
        return INDEX.equals(coord);
    }

    /**
     * Changes the remote repository used to resolve and download artifacts. This change takes effect immediately.
     */
    public abstract void setRemote(String url);

    /**
     * Resolves the given model coordinate to a local file and downloads the corresponding file from the remote
     * repository if not locally available.
     * 
     * @return the local copy of the model artifact
     * @throws IOException
     *             if no model could be downloaded due to, e.g., the coordinate does not exist on the remote repository
     *             or a network/io error occurred.
     */
    public abstract void resolve(ModelArchiveCoordinate model, IProgressMonitor monitor) throws IOException;

    /**
     * Deletes the artifact represented by the given coordinate from the local file system.
     */
    public abstract void delete(ModelArchiveCoordinate model, IProgressMonitor monitor) throws IOException;

    /**
     * Checks if the file for the given coordinate exists in the local file system.
     */
    public abstract boolean isCached(ModelArchiveCoordinate coord);

    /**
     * Returns the file for the given coordinate - if it exists. Note that this call does <b>not</b> download any
     * resources from the remote repository. It only touches the local file system.
     */
    public abstract Optional<File> getLocation(ModelArchiveCoordinate coord);

    /**
     * Searches the model index for all model archives matching the given {@link ProjectCoordinate} and model-type.
     */
    public abstract ModelArchiveCoordinate[] findModelArchives(ProjectCoordinate projectCoord, String modelType);

    /**
     * Searches the model index for the best model archive matching the given {@link ProjectCoordinate} and model-type.
     */
    public abstract Optional<ModelArchiveCoordinate> findBestModelArchive(ProjectCoordinate projectCoord,
            String modelType);

    /**
     * These events are fired whenever the state of the {@link ModelRepository} or its contents have changed to allow
     * others participants to respond to these state changes. Participants can register by subscribing themselves as
     * listeners to the Recommenders' workbench-wide event bus and adding corresponding listener methods like :
     * 
     * <pre>
     * @Subscribe
     * onEvent(ModelArchiveUpdatedEvent e){...}
     * </pre>
     * 
     * @see {@link UsingModelArchiveCache} for more example usages.
     */
    public static class ModelRepositoryEvents {

        /**
         * Base class for all events related to {@link ModelRepository}.
         */
        public abstract static class ModelArchiveCacheEvent {

            public final ModelRepository cache;

            public ModelArchiveCacheEvent(final ModelRepository cache) {
                this.cache = cache;
            }
        }

        /**
         * Fired when the remote repository URL has changed. This usually triggers a download of the model index and may
         * cause updates of existing model archives.
         */
        public static class RemoteRepositoryChangedEvent extends ModelArchiveCacheEvent {

            public RemoteRepositoryChangedEvent(final ModelRepository cache) {
                super(cache);
            }
        }

        /**
         * Fired when the given model repository instance was created. Allows listeners to trigger additional actions
         * like model index updates.
         */
        public static class ModelArchiveCacheOpenedEvent extends ModelArchiveCacheEvent {

            public ModelArchiveCacheOpenedEvent(final ModelRepository cache) {
                super(cache);
            }
        }

        /**
         * Fired when the given cache is shutdown. Allows listeners to close other resources based on this repository
         * like search indexes.
         */
        public static class ModelArchiveCacheClosedEvent extends ModelArchiveCacheEvent {

            public ModelArchiveCacheClosedEvent(final ModelRepository cache) {
                super(cache);
            }
        }

        /**
         * Fired whenever an older model archive was replaced by a newer model archive.
         * <p>
         * Note that index updates are also published as {@link ModelArchiveInstalledEvent}s.
         */
        public static class ModelArchiveInstalledEvent extends ModelArchiveCacheEvent {

            public ModelArchiveCoordinate coordinate;

            public ModelArchiveInstalledEvent(final ModelRepository cache, final ModelArchiveCoordinate model) {
                super(cache);
                coordinate = model;
            }
        }
    }

}
