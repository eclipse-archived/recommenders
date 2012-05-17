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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.recommenders.internal.rcp.models.IModelArchive;
import org.eclipse.recommenders.internal.rcp.models.IModelArchiveStore;
import org.eclipse.recommenders.internal.rcp.models.ModelArchiveMetadata;
import org.eclipse.recommenders.internal.rcp.models.ModelArchiveMetadata.ModelArchiveResolutionStatus;
import org.eclipse.recommenders.internal.rcp.repo.RepositoryUtils;
import org.eclipse.recommenders.internal.rcp.wiring.RecommendersModule.AutoCloseOnWorkbenchShutdown;
import org.eclipse.recommenders.rcp.repo.IModelRepository;
import org.eclipse.recommenders.utils.gson.GsonUtil;
import org.eclipse.recommenders.utils.rcp.JdtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.aether.artifact.Artifact;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;

@AutoCloseOnWorkbenchShutdown
public class DefaultModelArchiveStore<K extends IMember, V> implements
		Closeable, IModelArchiveStore<K, V> {

	private Logger log = LoggerFactory.getLogger(getClass());
	private Map<File, ModelArchiveMetadata<K, V>> mappings;
	private File store;
	private final String classifier;
	private Map<V, IModelArchive<K, V>> pool = Maps.newHashMap();

	private IModelRepository repository;

	private IDependenciesFactory factory;

	@Inject
	public DefaultModelArchiveStore(File store, String classifier,
			IModelRepository repository, IDependenciesFactory factory) {
		this.store = store;
		this.classifier = classifier;
		this.repository = repository;
		this.factory = factory;
		open();
	}

	public void open() {
		if (store.exists()) {
			mappings = GsonUtil.deserialize(store,
					new TypeToken<Map<File, ModelArchiveMetadata<K, V>>>() {
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

			final Optional<File> location = getLocation(pkgRoot.get());
			if (!location.isPresent()) {
				return absent();
			}

			Optional<IModelArchive<K, V>> archive = findModelArchive(location
					.get());
			if (!archive.isPresent()) {
				return absent();
			}

			final Optional<V> model = findModel(key, archive.get());
			if (!model.isPresent()) {
				return absent();
			}

			return of(model.get());
		} catch (Exception e) {
			log.warn("Loading model for '" + getElementLabel(key, ALL_DEFAULT)
					+ "' failed with exception.", e);
			return absent();
		}
	}

	@VisibleForTesting
	protected Optional<File> getLocation(final IPackageFragmentRoot root) {
		return JdtUtils.getLocation(root);
	}

	private Optional<IPackageFragmentRoot> findPackageFragmentRoot(final K key) {
		IPackageFragmentRoot pkgRoot = (IPackageFragmentRoot) key
				.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
		return fromNullable(pkgRoot);
	}

	@SuppressWarnings("unchecked")
	private Optional<IModelArchive<K, V>> findModelArchive(File location)
			throws IOException {
		ModelArchiveMetadata<K, V> meta = findOrCreateMetadata(location);

		switch (meta.getStatus()) {
		case UNRESOLVED:
			requestModelResolution(meta);
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

		IModelArchive<K, V> model = meta.getModelArchive();
		if (model == null) {
			model = loadModel(meta);
		}
		return fromNullable(model);
	}

	@SuppressWarnings("unchecked")
	private IModelArchive<K, V> loadModel(ModelArchiveMetadata<K, V> meta)
			throws IOException {
		IModelArchive<K, V> model = null;
		Artifact modelArtifact = RepositoryUtils.newArtifact(meta
				.getCoordinate());
		File file = repository.location(modelArtifact);
		if (file.exists()) {
			// just load it:
			model = factory.newModelArchive(file);
			meta.setModelArchive(model);
		} else {
			// mark it as invalid and request resolution:
			// this happens when someone deletes the repository but not the
			// configuration files.
			meta.setStatus(ModelArchiveResolutionStatus.UNRESOLVED);
			requestModelResolution(meta);
		}
		return model;
	}

	private void requestModelResolution(ModelArchiveMetadata<K, V> meta) {
		if (!meta.isResolutionRequestedSinceStartup()) {
			meta.setResolutionRequestedSinceStartup(true);
			factory.newResolutionJob(meta, classifier).schedule();
		}
	}

	private Optional<V> findModel(final K key, IModelArchive<K, V> archive) {
		if (!archive.hasModel(key)) {
			return absent();
		}
		final V model = archive.acquireModel(key).orNull();
		pool.put(model, archive);
		return fromNullable(model);
	}

	@Override
	public ModelArchiveMetadata<K, V> findOrCreateMetadata(File f) {
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
		if (archive != null)
			archive.releaseModel(model);
	}

	@Override
	public Collection<ModelArchiveMetadata<K, V>> getMetadata() {
		return mappings.values();
	}
}
