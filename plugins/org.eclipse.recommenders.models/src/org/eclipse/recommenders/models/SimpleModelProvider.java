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
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.recommenders.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
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
    private final Map<String, IInputStreamTransformer> transformers;

    public SimpleModelProvider(IModelRepository cache, IModelArchiveCoordinateAdvisor index, String modelType,
            Map<String, IInputStreamTransformer> transformers) {
        this.repository = cache;
        this.index = index;
        this.modelType = modelType;
        this.transformers = transformers;
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
                    // This is a workaround as LoadingCache cannot be made to cache hits but try again on a cache miss.
                    return absent();
                } else {
                    throw e;
                }
            }

            return doAcquireModel(key, zip);
        } catch (Exception e) {
            LOG.error("Exception while loading model " + key, e);
            return absent();
        }
    }

    @VisibleForTesting
    protected Optional<M> doAcquireModel(K key, ZipFile zip) throws IOException {
        InputStream in = null;
        try {
            String basePath = getBasePath(key);
            in = getInputStream(zip, basePath).orNull();
            if (in == null) {
                return absent();
            } else {
                return Optional.of(loadModel(in, key));
            }
        } catch (UncheckedExecutionException e) {
            if (IllegalStateException.class.equals(e.getCause().getClass())) {
                // repository.getLocation(..) returned absent. Try to load ZIP file again next time.
                return absent();
            } else {
                throw e;
            }
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    protected abstract String getBasePath(K key);

    @VisibleForTesting
    protected Optional<InputStream> getInputStream(ZipFile zip, String basePath) throws IOException {
        for (Entry<String, IInputStreamTransformer> transformer : transformers.entrySet()) {
            ZipEntry toTransform = zip.getEntry(basePath + "." + transformer.getKey()); //$NON-NLS-1$
            if (toTransform != null) {
                return Optional.of(transformer.getValue().transform(zip.getInputStream(toTransform)));
            }
        }
        ZipEntry entry = zip.getEntry(basePath);
        if (entry == null) {
            return absent();
        }
        return Optional.of(zip.getInputStream(entry));
    }

    protected abstract M loadModel(InputStream stream, K key) throws IOException;

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
