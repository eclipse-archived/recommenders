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
package org.eclipse.recommenders.models;

import static com.google.common.base.Optional.absent;
import static com.google.common.io.ByteStreams.toByteArray;
import static com.google.common.io.Files.newInputStreamSupplier;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.eclipse.recommenders.utils.Zips.closeQuietly;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.util.concurrent.UncheckedExecutionException;

/**
 * A non-thread-safe implementation of {@link IModelProvider} that loads models from model ZIP files using a
 * {@link ModelRepository}. Note that {@link #acquireModel(IUniqueName)} attempts to download matching model archives
 * immediately and thus blocks until the download is completed.
 */
public abstract class SimpleModelProvider<K extends IUniqueName<?>, M> implements IModelProvider<K, M> {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleModelProvider.class);

    private static final int CACHE_SIZE = 10;

    private final LoadingCache<ModelCoordinate, ZipFile> openZips = CacheBuilder.newBuilder().maximumSize(CACHE_SIZE)
            .expireAfterAccess(1, MINUTES).removalListener(new ZipRemovalListener()).build(new ZipCacheLoader());
    private final IModelRepository repository;
    private final IModelArchiveCoordinateAdvisor index;
    private final String modelType;

    public SimpleModelProvider(IModelRepository cache, IModelArchiveCoordinateAdvisor index, String modelType) {
        this.repository = cache;
        this.index = index;
        this.modelType = modelType;
    }

    @Override
    public Optional<M> acquireModel(K key) {
        try {
            // unknown model? return immediately
            ModelCoordinate mc = index.suggest(key.getProjectCoordinate(), modelType).orNull();
            if (mc == null) {
                return absent();
            }
            final ZipFile zip;
            try {
                zip = openZips.get(mc);
            } catch (UncheckedExecutionException e) {
                if (IllegalStateException.class.equals(e.getCause().getClass())) {
                    // repository.getLocation(..) returned absent. Try to load ZIP file again next time.
                    return absent();
                } else {
                    throw e;
                }
            }
            return loadModel(zip, key);
        } catch (Exception e) {
            LOG.error("Exception while loading model " + key, e);
            return absent();
        }
    }

    protected abstract Optional<M> loadModel(ZipFile zip, K key) throws Exception;

    @Override
    public void releaseModel(M value) {
    }

    @Override
    public void open() throws IOException {
    }

    @Override
    public void close() throws IOException {
        openZips.invalidateAll();
    }

    /**
     * Resolves the given model archive coordinate from models store, puts the zip file into the cache, and loads the
     * file contents completely in memory for faster data access.
     */
    private final class ZipCacheLoader extends CacheLoader<ModelCoordinate, ZipFile> {
        @Override
        public ZipFile load(ModelCoordinate key) throws Exception {
            File location = repository.getLocation(key, true).get();
            // read file in memory to speed up access
            toByteArray(newInputStreamSupplier(location));
            return new ZipFile(location);
        }
    }

    /**
     * Closes an zip file evicted from the cache.
     */
    private final class ZipRemovalListener implements RemovalListener<ModelCoordinate, ZipFile> {
        @Override
        public void onRemoval(RemovalNotification<ModelCoordinate, ZipFile> notification) {
            closeQuietly(notification.getValue());
        }
    }
}
