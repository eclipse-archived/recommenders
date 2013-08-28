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

import static com.google.common.base.Optional.*;
import static java.util.concurrent.TimeUnit.MINUTES;

import java.io.IOException;
import java.util.IdentityHashMap;

import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.eclipse.recommenders.utils.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;

/**
 * A model provider implementation that pools recommendation models to further improve performance. Note that models
 * need to be release by clients. Otherwise the pool may be exhausted quickly.
 */
public abstract class PoolingModelProvider<K extends IUniqueName<?>, M> extends SimpleModelProvider<K, M> {

    private final Logger log = LoggerFactory.getLogger(getClass());

    // which models are currently borrowed to someone?
    // we need this mapping for implementing releaseModel properly so that clients don't have to submit their keys too.
    private final IdentityHashMap<M, K> borrowedModels = Maps.newIdentityHashMap();

    // model pool
    // REVIEW: we may want to make pool creation configurable later?
    private GenericKeyedObjectPool<K, M> pool = createModelPool();

    private IModelArchiveCoordinateAdvisor index;

    public PoolingModelProvider(IModelRepository repository, IModelArchiveCoordinateAdvisor index, String modelType) {
        super(repository, index, modelType);
        this.index = index;
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
    public Optional<M> acquireModel(@Nullable K key) {
        if (key == null) {
            return absent();
        }
        try {
            M model = pool.borrowObject(key);
            if (model != null) {
                borrowedModels.put(model, key);
            }
            return fromNullable(model);
        } catch (Exception e) {
            log.error("Couldn't obtain model for " + key, e);
            return absent();
        }
    }

    @Override
    public void releaseModel(M model) {
        try {
            K key = borrowedModels.remove(model);
            pool.returnObject(key, model);
        } catch (Exception e) {
            log.error("Exception while releasing. Couldn't release model " + model, e);
        }
    }

    /**
     * Mediates calls from Apache Commons Pool implementation to our {create,destroy,passivate}Model() methods above.
     */
    private final class ModelPoolFactoryMediator extends BaseKeyedPoolableObjectFactory<K, M> {
        @Override
        @Nullable
        public M makeObject(K key) throws Exception {
            return PoolingModelProvider.super.acquireModel(key).orNull();
        }

        @Override
        public void activateObject(K key, M obj) throws Exception {
            PoolingModelProvider.this.activateModel(obj);
        }

        @Override
        public void passivateObject(K key, M obj) throws Exception {
            PoolingModelProvider.this.passivateModel(obj);
        }

        @Override
        public void destroyObject(K key, M obj) throws Exception {
            PoolingModelProvider.this.destroyModel(obj);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            super.close();
            pool.close();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    /**
     * Invoked before the model is returned from the pool.
     */
    protected void activateModel(M model) {
    }

    /**
     * Invoked after the model was released and returned to the pool.
     */
    protected void passivateModel(M model) {
    }

    /**
     * Invoked when the model is removed from the pool.
     */
    protected void destroyModel(M model) {
    }

}
