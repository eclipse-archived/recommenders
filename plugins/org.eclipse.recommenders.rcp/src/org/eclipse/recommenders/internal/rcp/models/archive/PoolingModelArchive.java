/**
 * Copyright (c) 2010, 2012 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 *    Marcel Bruch - generalized API.
 */
package org.eclipse.recommenders.internal.rcp.models.archive;

import static com.google.common.base.Optional.fromNullable;
import static org.eclipse.recommenders.utils.Checks.ensureIsNotNull;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.pool.KeyedPoolableObjectFactory;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.eclipse.recommenders.internal.rcp.models.IModelArchive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

public class PoolingModelArchive<K, M> implements IModelArchive<K, M> {
    private Logger log = LoggerFactory.getLogger(getClass());
    private GenericKeyedObjectPool pool;
    private IModelFactory<K, M> factory;
    private Map<M, K> objects = Maps.newHashMap();

    @Inject
    public PoolingModelArchive(final IModelFactory<K, M> factory) {
        this.factory = ensureIsNotNull(factory);
        pool = createPool();
    }

    private GenericKeyedObjectPool createPool() {
        GenericKeyedObjectPool pool = new GenericKeyedObjectPool(new PoolFactory());
        pool.setMaxTotal(100);
        pool.setWhenExhaustedAction(GenericKeyedObjectPool.WHEN_EXHAUSTED_FAIL);
        return pool;
    }

    @Override
    public void open() {
        factory.open();
    }

    @Override
    public void close() throws IOException {
        factory.close();
    }

    @Override
    public boolean hasModel(K key) {
        return factory.hasModel(key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<M> acquireModel(final K key) {
        M model = null;
        try {
            model = (M) pool.borrowObject(key);
            if (model != null) {
                objects.put(model, key);
            }
        } catch (final Exception e) {
            log.error("Exception while loading model for key '" + key + "'", e);
        }
        return fromNullable(model);
    }

    @Override
    public void releaseModel(final M model) {
        if (model == null)
            return;
        try {
            K key = objects.get(model);
            pool.returnObject(key, model);
        } catch (final Exception e) {
            log.error("Exception while releasing model'" + model + "'", e);
        }
    }

    @SuppressWarnings("unchecked")
    private class PoolFactory implements KeyedPoolableObjectFactory {
        @Override
        public boolean validateObject(Object key, Object obj) {
            return factory.validateModel((K) key, (M) obj);
        }

        @Override
        public void passivateObject(Object key, Object obj) throws Exception {
            factory.passivateModel((K) key, (M) obj);
        }

        @Override
        public Object makeObject(final Object key) throws Exception {
            return factory.createModel((K) key);
        }

        @Override
        public void destroyObject(Object key, Object obj) throws Exception {
            factory.destroyModel((K) key, (M) obj);
        }

        @Override
        public void activateObject(Object key, Object obj) throws Exception {
            factory.activateModel((K) key, (M) obj);
        }
    }
}
