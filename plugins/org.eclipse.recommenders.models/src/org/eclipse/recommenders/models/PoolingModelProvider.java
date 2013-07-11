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
import static com.google.common.base.Optional.of;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.eclipse.recommenders.utils.Checks.ensureIsNotNull;
import static org.eclipse.recommenders.utils.Zips.closeQuietly;

import java.io.IOException;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.zip.ZipFile;

import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.eclipse.recommenders.models.ModelRepository.ModelRepositoryEvents.ModelArchiveInstalledEvent;
import org.eclipse.recommenders.utils.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;

public abstract class PoolingModelProvider<K extends IBasedName<?>, M> implements IModelProvider<K, M> {

    private final Logger log = LoggerFactory.getLogger(getClass());

    // which zip files are currently open?
    private final Map<ModelArchiveCoordinate, ZipFile> openZips = Maps.newHashMap();

    // which models are currently pooled?
    private final ListMultimap<ModelArchiveCoordinate, K> pooledModels = LinkedListMultimap.create();

    // which models are currently borrowed to someone?
    private final IdentityHashMap<M, K> borrowedModels = Maps.newIdentityHashMap();

    // the cache to load the model archives from
    private final ModelRepository modelCache;

    // model pool
    // REVIEW: we may want to make pool creation configurable later?
    private final GenericKeyedObjectPool<K, M> modelPool = createModelPool();

    private final String modelType;

    public PoolingModelProvider(ModelRepository modelCache, String modelType) {
        this.modelCache = modelCache;
        this.modelType = modelType;
    }

    private GenericKeyedObjectPool<K, M> createModelPool() {
        GenericKeyedObjectPool<K, M> pool = new GenericKeyedObjectPool<K, M>(new ModelPoolFactoryMediator());
        pool.setMaxTotal(30);
        pool.setMaxIdle(5);
        pool.setWhenExhaustedAction(GenericKeyedObjectPool.WHEN_EXHAUSTED_FAIL);
        // run clean up every 5 minutes:
        pool.setTimeBetweenEvictionRunsMillis(MINUTES.toMillis(5));
        // models are evictable after 5 minutes
        pool.setMinEvictableIdleTimeMillis(MINUTES.toMillis(5));
        return pool;
    }

    @Override
    public Optional<M> acquireModel(K key) {
        Optional<ModelArchiveCoordinate> opt = modelCache.findBestModelArchive(key.getBase(), modelType);
        if (!opt.isPresent()) {
            return Optional.absent();
        }
        try {
            M model = modelPool.borrowObject(key);
            return of(model);
        } catch (Exception e) {
            log.error("Couldn't obtain model for " + key, e);
            return absent();
        }
    }

    protected abstract Optional<M> createModel(K key, ZipFile modelArchive, ModelArchiveCoordinate modelId)
            throws Exception;

    @Override
    public void releaseModel(M model) {
        try {
            K key = borrowedModels.remove(model);
            modelPool.returnObject(key, model);
        } catch (Exception e) {
            log.error("Exception while releasing Couldn't release model " + model, e);
        }
    }

    protected void passivateModel(K key, M model, ModelArchiveCoordinate modelId) {
    };

    protected void destroyModel(K key, M model, ModelArchiveCoordinate modelId) {
    };

    @Subscribe
    public void onEvent(ModelArchiveInstalledEvent e) {
        ModelArchiveCoordinate modelId = e.coordinate;
        closeZipFile(modelId);
        clearPooledModels(modelId);
    }

    private void closeZipFile(@Nullable ModelArchiveCoordinate modelId) {
        ZipFile zip = openZips.remove(modelId);
        if (zip == null) {
            return;
        }
        closeQuietly(zip);
    }

    private void clearPooledModels(ModelArchiveCoordinate modelId) {
        for (K key : pooledModels.get(modelId)) {
            modelPool.clear(key);
        }
    }

    @Override
    public void close() throws IOException {
        closePool();
        closeZipFiles();
    }

    private void closePool() {
        try {
            modelPool.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeZipFiles() {
        for (ZipFile zip : openZips.values()) {
            closeQuietly(zip);
        }
    }

    /**
     * Mediates calls from Apache Commons Pool implementation to our {create,destroy,passivate}Model() methods above.
     */
    private final class ModelPoolFactoryMediator extends BaseKeyedPoolableObjectFactory<K, M> {
        @Override
        @Nullable
        public M makeObject(K key) throws Exception {
            ModelArchiveCoordinate modelId = ensureIsNotNull(modelCache.findBestModelArchive(key.getBase(), modelType)
                    .orNull());
            ZipFile zipFile = openZips.get(modelId);
            if (zipFile == null) {
                return null;
            }
            M model = createModel(key, zipFile, modelId).orNull();
            pooledModels.put(modelId, key);
            return model;
        }

        /**
         * Removes the given model from the list of tracked pooled models and closes the zip-file this model originates
         * from if no other model is loaded from this zip-file.
         * 
         * @see PoolingModelProvider#destroyModel(IBasedName, Object, ModelArchiveCoordinate)
         */
        @Override
        public void destroyObject(K key, M model) throws Exception {
            ModelArchiveCoordinate modelId = ensureIsNotNull(modelCache.findBestModelArchive(key.getBase(), modelType)
                    .orNull());
            pooledModels.remove(key, model);
            // if there are no more models loaded
            if (!pooledModels.containsKey(modelId)) {
                closeZipFile(modelId);
            }
            destroyModel(key, model, modelId);
        }

        @Override
        public void passivateObject(K key, M model) throws Exception {
            ModelArchiveCoordinate modelId = ensureIsNotNull(modelCache.findBestModelArchive(key.getBase(), modelType)
                    .orNull());
            passivateModel(key, model, modelId);
        }
    }
}
