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
import static org.eclipse.recommenders.models.ModelArchiveCoordinate.UNKNOWN;
import static org.eclipse.recommenders.utils.Zips.closeQuietly;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.zip.ZipFile;

import org.eclipse.core.runtime.NullProgressMonitor;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.reflect.Reflection;

/**
 * A non-thread-safe implementation of {@link IModelProvider} that loads models from model zip files using a
 * {@link ModelRepository}. Note that {@link #acquireModel(IBasedName)} attempts to download matching model archives
 * immediately and thus blocks until the download is completed.
 */
public abstract class SimpleModelProvider<K extends IBasedName<?>, M> implements IModelProvider<K, M> {

    private final LoadingCache<ModelArchiveCoordinate, ZipFile> openZips = CacheBuilder.newBuilder()
            .maximumSize(10)
            .expireAfterAccess(1, MINUTES)
            .removalListener(new ZipRemovalListener())
            .build(new ZipCacheLoader());

    protected final ModelRepository archiveStore;
    protected final String modelType;

    public SimpleModelProvider(ModelRepository cache, String modelType) {
        this.archiveStore = cache;
        this.modelType = modelType;
    }

    @Override
    public Optional<M> acquireModel(K key) {
        try {
            ModelArchiveCoordinate coord = archiveStore.findBestModelArchive(key.getBase(), modelType).or(UNKNOWN);
            ZipFile zip = openZips.get(coord);
            return loadModel(zip, key);
        } catch (Exception e) {
            // TODO log message
            e.printStackTrace();
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
     * Fake ZipFile implementation that seves as NULL object for the cache.
     */
    private static final ZipFile FAKE = Reflection.newProxy(ZipFile.class, new InvocationHandler() {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return null;
        }
    });

    /**
     * Resolves the given model archive coordinate from models store, puts the zip file into the cache, and loads the
     * file contents completely in memory for faster data access.
     */
    private final class ZipCacheLoader extends CacheLoader<ModelArchiveCoordinate, ZipFile> {
        @Override
        public ZipFile load(ModelArchiveCoordinate key) throws Exception {
            archiveStore.resolve(key, new NullProgressMonitor());
            File location = archiveStore.getLocation(key).orNull();
            if (location == null) {
                return FAKE;
            }
            // read file in memory to speed up access
            toByteArray(newInputStreamSupplier(location));
            return new ZipFile(location);
        }
    }

    /**
     * Closes an zip file evicted from the cache.
     */
    private final class ZipRemovalListener implements RemovalListener<ModelArchiveCoordinate, ZipFile> {
        @Override
        public void onRemoval(RemovalNotification<ModelArchiveCoordinate, ZipFile> notification) {
            closeQuietly(notification.getValue());
        }
    }
}
