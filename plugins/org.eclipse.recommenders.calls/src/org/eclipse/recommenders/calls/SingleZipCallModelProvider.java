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
package org.eclipse.recommenders.calls;

import static com.google.common.base.Optional.of;
import static org.eclipse.recommenders.utils.Zips.*;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipFile;

import org.eclipse.recommenders.models.UniqueTypeName;
import org.eclipse.recommenders.utils.Openable;
import org.eclipse.recommenders.utils.Zips;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.Beta;
import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * A model provider that uses a single zip file to resolve and load call models from.
 * <p>
 * Note that this provider does not implement any pooling behavior, i.e., calls to {@link #acquireModel(UniqueTypeName)}
 * may return the <b>same</b> {@link ICallModel} independent of whether {@link #releaseModel(ICallModel)} was called or
 * not. Thus, these <b>models should not be shared between and used by several recommenders at the same time</b>.
 */
@Beta
public class SingleZipCallModelProvider implements ICallModelProvider, Openable {

    private static final Logger LOG = LoggerFactory.getLogger(SingleZipCallModelProvider.class);

    private final File models;
    private ZipFile zip;
    private final LoadingCache<ITypeName, ICallModel> cache = CacheBuilder.newBuilder()
            .expireAfterAccess(3, TimeUnit.MINUTES).maximumSize(30).build(new CallNetCacheLoader());

    public SingleZipCallModelProvider(File models) {
        this.models = models;
    }

    @Override
    public void open() throws IOException {
        readFully(models);
        zip = new ZipFile(models);
    }

    @Override
    public void close() throws IOException {
        closeQuietly(zip);
    }

    @Override
    public Optional<ICallModel> acquireModel(UniqueTypeName key) {
        try {
            ICallModel net = cache.get(key.getName());
            net.reset();
            return of(net);
        } catch (ExecutionException e) {
            LOG.error("Failed to acquire model for " + key, e);
            return Optional.absent();
        }
    }

    public Set<ITypeName> acquireableTypes() {
        return Zips.types(zip.entries(), ".data");
    }

    @Override
    public void releaseModel(ICallModel value) {

    }

    private final class CallNetCacheLoader extends CacheLoader<ITypeName, ICallModel> {
        @Override
        public ICallModel load(ITypeName type) throws Exception {
            return JayesCallModel.load(zip, type).or(NullCallModel.NULL_MODEL);
        }
    }
}
