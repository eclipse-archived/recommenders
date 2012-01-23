/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.completion.rcp.calls.store2;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.recommenders.commons.udc.Manifest;
import org.eclipse.recommenders.internal.completion.rcp.calls.net.IObjectMethodCallsNet;
import org.eclipse.recommenders.internal.completion.rcp.calls.store2.classpath.DependencyInfoComputerService;
import org.eclipse.recommenders.internal.completion.rcp.calls.store2.classpath.DependencyInfoStore;
import org.eclipse.recommenders.internal.completion.rcp.calls.store2.classpath.ManifestResolverService;
import org.eclipse.recommenders.internal.completion.rcp.calls.store2.models.IModelArchive;
import org.eclipse.recommenders.internal.completion.rcp.calls.store2.models.ModelArchiveDownloadService;
import org.eclipse.recommenders.internal.completion.rcp.calls.store2.models.ModelArchiveStore;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.rcp.JavaElementResolver;
import org.eclipse.recommenders.utils.rcp.JdtUtils;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;

public class CallModelStore implements Closeable {

    private final ModelArchiveStore<IObjectMethodCallsNet> modelStore;
    private final DependencyInfoStore depStore;
    private final DependencyInfoComputerService depComputer;
    private final JavaElementResolver jdtCache;

    private final Map<IObjectMethodCallsNet, IModelArchive<IObjectMethodCallsNet>> index = Maps.newHashMap();
    private final ModelArchiveDownloadService modelDownloadService;
    private final ManifestResolverService manifestResolver;

    public CallModelStore(final DependencyInfoStore depStore, final DependencyInfoComputerService depComputer,
            final ModelArchiveStore<IObjectMethodCallsNet> modelStore, final ManifestResolverService manifestResolver,
            final ModelArchiveDownloadService modelDownloadService, final JavaElementResolver jdtCache) {
        this.depStore = depStore;
        this.depComputer = depComputer;
        this.modelStore = modelStore;
        this.manifestResolver = manifestResolver;
        this.modelDownloadService = modelDownloadService;
        this.jdtCache = jdtCache;
    }

    public Optional<IObjectMethodCallsNet> aquireModel(final IType type) {
        final IPackageFragmentRoot fragmentRoot = (IPackageFragmentRoot) type
                .getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
        final Optional<File> location = JdtUtils.getLocation(fragmentRoot);
        if (!location.isPresent()) {
            return absent();
        }
        final Optional<Manifest> manifest = depStore.getManifest(location.get());
        if (!manifest.isPresent()) {
            return absent();
        }
        @SuppressWarnings("unchecked")
        final IModelArchive<IObjectMethodCallsNet> archive = modelStore.getModelArchive(manifest.get());
        final ITypeName recType = jdtCache.toRecType(type);
        if (!archive.hasModel(recType)) {
            return absent();
        }
        final IObjectMethodCallsNet model = archive.acquireModel(recType);
        index.put(model, archive);
        return of(model);
    }

    public void releaseModel(final IObjectMethodCallsNet model) {
        final IModelArchive<IObjectMethodCallsNet> archive = index.get(model);
        archive.releaseModel(model);
    }

    @Override
    public void close() throws IOException {
        depStore.close();
    }

    public DependencyInfoStore getDependencyInfoStore() {
        return depStore;
    }

    public DependencyInfoComputerService getDependencyInfoComputerService() {
        return depComputer;
    }

    public ModelArchiveDownloadService getModelArchiveDownloadService() {
        return modelDownloadService;
    }

    public ManifestResolverService getManifestResolverService() {
        return manifestResolver;
    }
}
