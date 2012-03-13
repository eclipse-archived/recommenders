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
package org.eclipse.recommenders.internal.completion.rcp.calls.models;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Optional.of;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.eclipse.recommenders.internal.completion.rcp.calls.models.CallModelResolutionData.ModelResolutionStatus.FAILED;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.inject.Inject;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.recommenders.internal.completion.rcp.calls.models.CallModelDownloadJob.JobFactory;
import org.eclipse.recommenders.internal.completion.rcp.calls.models.CallModelResolutionData.ModelResolutionStatus;
import org.eclipse.recommenders.internal.completion.rcp.calls.net.IObjectMethodCallsNet;
import org.eclipse.recommenders.internal.completion.rcp.calls.wiring.CallsCompletionModule.CallModelsIndexFile;
import org.eclipse.recommenders.internal.rcp.repo.RepositoryUtils;
import org.eclipse.recommenders.rcp.repo.IModelRepository;
import org.eclipse.recommenders.utils.gson.GsonUtil;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.rcp.JavaElementResolver;
import org.eclipse.recommenders.utils.rcp.JdtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.aether.artifact.Artifact;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.gson.reflect.TypeToken;

public class CallModelStore implements Closeable {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final Map<IObjectMethodCallsNet, IModelArchive<IObjectMethodCallsNet>> pool = Maps.newConcurrentMap();

    private final IModelRepository modelRepo;
    private final JavaElementResolver jdtCache;
    private final JobFactory factory;

    private final File mappingsFile;
    private Map<File, CallModelResolutionData> mappings;

    @Inject
    public CallModelStore(@CallModelsIndexFile File mappingsFile, IModelRepository repository,
            JavaElementResolver jdtCache, JobFactory factory) {
        this.mappingsFile = mappingsFile;
        this.modelRepo = repository;
        this.jdtCache = jdtCache;
        this.factory = factory;
        loadMappings();
    }

    private void loadMappings() {
        if (mappingsFile.exists()) {
            mappings = GsonUtil.deserialize(mappingsFile, new TypeToken<Map<File, CallModelResolutionData>>() {
            }.getType());
        } else {
            mappings = Maps.newHashMap();
        }
        // mappings.clear()
    }

    public Optional<IObjectMethodCallsNet> aquireModel(final IType type) {
        try {
            final Optional<IPackageFragmentRoot> pkgRoot = findPackageFragmentRoot(type);
            if (!pkgRoot.isPresent()) {
                return absent();
            }

            final Optional<File> location = JdtUtils.getLocation(pkgRoot.get());
            if (!location.isPresent()) {
                return absent();
            }

            Optional<IModelArchive<IObjectMethodCallsNet>> archive = findModelArchive(location.get());
            if (!archive.isPresent()) {
                return absent();
            }

            final Optional<IObjectMethodCallsNet> model = findModel(type, archive.get());
            if (!model.isPresent()) {
                return absent();
            }

            pool.put(model.get(), archive.get());
            return of(model.get());
        } catch (Exception e) {
            log.warn("Loading model for '" + type.getFullyQualifiedName() + "' failed with exception.", e);
            return absent();
        }
    }

    private Optional<IPackageFragmentRoot> findPackageFragmentRoot(final IType type) {
        IPackageFragmentRoot pkgRoot = (IPackageFragmentRoot) type.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
        return fromNullable(pkgRoot);
    }

    private Optional<IModelArchive<IObjectMethodCallsNet>> findModelArchive(File location) {
        CallModelResolutionData mapping = getModel(location);
        switch (mapping.getStatus()) {
        case UNRESOLVED:
            factory.create(location).schedule();
        case FAILED:
        case PROHIBITED:
        case UNINITIALIZED:
            return absent();
        }

        if (isEmpty(mapping.getCoordinate())) {
            return absent();
        }

        if (mapping.getModel() == null) {
            Artifact modelArtifact = RepositoryUtils.newArtifact(mapping.getCoordinate());
            File file = modelRepo.location(modelArtifact);
            if (!file.exists()) {
                mapping.setStatus(FAILED);
                mapping.setError(String.format("File %s does not exist", file.getAbsolutePath()));
                return absent();
            }
            mapping.setModel(new ModelArchive<IObjectMethodCallsNet>(file, new CallModelLoader()));
        }
        return fromNullable(mapping.getModel());
    }

    private Optional<IObjectMethodCallsNet> findModel(final IType type, IModelArchive<IObjectMethodCallsNet> archive) {
        final ITypeName rType = jdtCache.toRecType(type);
        if (!archive.hasModel(rType)) {
            return absent();
        }
        final IObjectMethodCallsNet model = archive.acquireModel(rType);
        return fromNullable(model);
    }

    public Map<File, CallModelResolutionData> getMappings() {
        return mappings;
    }

    public CallModelResolutionData getModel(File f) {
        if (f == null) {
            return CallModelResolutionData.NULL;
        }
        CallModelResolutionData ref = mappings.get(f);
        if (ref == null) {
            ref = new CallModelResolutionData();
            ref.setStatus(ModelResolutionStatus.UNRESOLVED);
            mappings.put(f, ref);
        }
        return ref;
    }

    @Override
    public void close() throws IOException {
        Files.createParentDirs(mappingsFile);
        GsonUtil.serialize(mappings, mappingsFile);
    }

    public void releaseModel(final IObjectMethodCallsNet model) {
        final IModelArchive<IObjectMethodCallsNet> archive = pool.get(model);
        archive.releaseModel(model);
    }
}
