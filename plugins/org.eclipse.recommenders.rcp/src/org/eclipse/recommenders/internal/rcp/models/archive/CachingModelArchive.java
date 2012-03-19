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
package org.eclipse.recommenders.internal.rcp.models.archive;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.fromNullable;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.eclipse.recommenders.internal.rcp.models.IModelArchive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;

public class CachingModelArchive<K, M> implements IModelArchive<K, M> {
    private Logger log = LoggerFactory.getLogger(getClass());

    private Cache<K, M> cache = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).maximumSize(100)
            .build(new CacheLoaderWrapper());

    private final IModelFactory<K, M> loader;

    public CachingModelArchive(IModelFactory<K, M> loader) {
        this.loader = loader;
    }

    @Override
    public boolean hasModel(K key) {
        try {
            return loader.hasModel(key);
        } catch (Exception e) {
            log.debug("Exception occurred while checking model existence for key " + key, e);
            return false;
        }
    }

    @Override
    public Optional<M> acquireModel(K key) {
        try {
            return fromNullable(cache.get(key));
        } catch (Exception e) {
            log.debug("Exception occurred while fetching model for key " + key, e);
            return absent();
        }
    }

    @Override
    public void releaseModel(M value) {
        // ignore that event.
    }

    @Override
    public void open() {
        loader.open();
    }

    @Override
    public void close() throws IOException {
        loader.close();
    }

    private final class CacheLoaderWrapper extends CacheLoader<K, M> {
        @Override
        public M load(K key) throws Exception {
            M model = loader.createModel(key);
            loader.activateModel(key, model);
            return model;
        }
    }
}
