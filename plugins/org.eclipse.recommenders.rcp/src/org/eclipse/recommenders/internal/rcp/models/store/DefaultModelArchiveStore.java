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
package org.eclipse.recommenders.internal.rcp.models.store;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Optional.of;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.eclipse.jdt.ui.JavaElementLabels.ALL_DEFAULT;
import static org.eclipse.jdt.ui.JavaElementLabels.getElementLabel;
import static org.eclipse.recommenders.internal.rcp.models.ModelArchiveMetadata.ModelArchiveResolutionStatus.UNRESOLVED;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.recommenders.internal.rcp.models.IModelArchive;
import org.eclipse.recommenders.internal.rcp.models.IModelArchiveStore;
import org.eclipse.recommenders.internal.rcp.models.ModelArchiveMetadata;
import org.eclipse.recommenders.internal.rcp.models.ModelArchiveMetadata.ModelArchiveResolutionStatus;
import org.eclipse.recommenders.internal.rcp.models.archive.NullModelArchive;
import org.eclipse.recommenders.internal.rcp.repo.RepositoryUtils;
import org.eclipse.recommenders.rcp.repo.IModelRepository;
import org.eclipse.recommenders.utils.gson.GsonUtil;
import org.eclipse.recommenders.utils.rcp.JdtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.aether.artifact.Artifact;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;

public class DefaultModelArchiveStore<K extends IMember, V> implements Closeable, IModelArchiveStore<K, V> {

    private Logger log = LoggerFactory.getLogger(getClass());
    private Map<File, ModelArchiveMetadata<K, V>> mappings;
    private File store;
    private final String classifier;
    private Map<V, IModelArchive<K, V>> pool = Maps.newHashMap();

    private IModelRepository repository;

    private IDependenciesFactory factory;

    @Inject
    public DefaultModelArchiveStore(File store, String classifier, IModelRepository repository,
            IDependenciesFactory factory) {
        this.store = store;
        this.classifier = classifier;
        this.repository = repository;
        this.factory = factory;
        open();
    }

    public void open() {
        if (store.exists()) {
            mappings = GsonUtil.deserialize(store, new TypeToken<Map<File, ModelArchiveMetadata<K, V>>>() {
            }.getType());
        } else {
            mappings = Maps.newHashMap();
        }
        // mappings.clear()
    }

    @Override
    public Optional<V> aquireModel(final K key) {
        try {
            final Optional<IPackageFragmentRoot> pkgRoot = findPackageFragmentRoot(key);
            if (!pkgRoot.isPresent()) {
                return absent();
            }

            final Optional<File> location = JdtUtils.getLocation(pkgRoot.get());
            if (!location.isPresent()) {
                return absent();
            }

            Optional<IModelArchive<K, V>> archive = findModelArchive(location.get());
            if (!archive.isPresent()) {
                return absent();
            }

            final Optional<V> model = findModel(key, archive.get());
            if (!model.isPresent()) {
                return absent();
            }

            return of(model.get());
        } catch (Exception e) {
            log.warn("Loading model for '" + getElementLabel(key, ALL_DEFAULT) + "' failed with exception.", e);
            return absent();
        }
    }

    private Optional<IPackageFragmentRoot> findPackageFragmentRoot(final K key) {
        IPackageFragmentRoot pkgRoot = (IPackageFragmentRoot) key.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
        return fromNullable(pkgRoot);
    }

    @SuppressWarnings("unchecked")
    private Optional<IModelArchive<K, V>> findModelArchive(File location) {
        ModelArchiveMetadata<K, V> meta = findOrCreateMetadata(location);
        switch (meta.getStatus()) {
        case UNRESOLVED:
            factory.newResolutionJob(meta, classifier).schedule();
        case FAILED:
        case PROHIBITED:
        case UNINITIALIZED:
            return absent();
        default:
            // go ahead and load the archive...
        }

        if (isEmpty(meta.getCoordinate())) {
            return absent();
        }

        IModelArchive<K, V> model = meta.getModel();
        if (model == null) {
            Artifact modelArtifact = RepositoryUtils.newArtifact(meta.getCoordinate());
            File file = repository.location(modelArtifact);
            if (!file.exists()) {
                meta.setStatus(ModelArchiveResolutionStatus.FAILED);
                meta.setError(String.format("File %s does not exist", file.getAbsolutePath()));
                return absent();
            }
            try {
                model = factory.newModelArchive(file);
            } catch (Exception e) {
                e.printStackTrace();
                model = NullModelArchive.empty();
            }
            meta.setModel(model);
        }
        return fromNullable(model);
    }

    private Optional<V> findModel(final K key, IModelArchive<K, V> archive) {
        if (!archive.hasModel(key)) {
            return absent();
        }
        final V model = archive.acquireModel(key).orNull();
        pool.put(model, archive);
        return fromNullable(model);
    }

    // public Map<File, ModelArchiveMetadata<K, V>> getMappings() {
    // return mappings;
    // }

    @SuppressWarnings("unchecked")
    public ModelArchiveMetadata<K, V> findOrCreateMetadata(File f) {
        if (f == null) {
            return ModelArchiveMetadata.NULL;
        }
        ModelArchiveMetadata<K, V> ref = mappings.get(f);
        if (ref == null) {
            ref = new ModelArchiveMetadata<K, V>();
            ref.setStatus(UNRESOLVED);
            ref.setLocation(f);
            mappings.put(f, ref);
        }
        return ref;
    }

    @Override
    public void close() throws IOException {
        Files.createParentDirs(store);
        GsonUtil.serialize(mappings, store);
    }

    @Override
    public void releaseModel(final V model) {
        final IModelArchive<K, V> archive = pool.get(model);
        archive.releaseModel(model);
    }

    @Override
    public Collection<ModelArchiveMetadata<K, V>> getMetadata() {
        return mappings.values();
    }
}
