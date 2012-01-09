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
package org.eclipse.recommenders.internal.completion.rcp.calls.store2.models;

import static org.eclipse.recommenders.utils.Throws.throwUnhandledException;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.eclipse.recommenders.commons.udc.Manifest;
import org.eclipse.recommenders.internal.completion.rcp.calls.store2.Events.ManifestResolutionFinished;
import org.eclipse.recommenders.internal.completion.rcp.calls.store2.Events.ModelArchiveDownloadFinished;
import org.eclipse.recommenders.internal.completion.rcp.calls.store2.Events.ModelArchiveDownloadRequested;
import org.eclipse.recommenders.internal.completion.rcp.calls.store2.Events.ModelArchiveRegistered;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

@SuppressWarnings("rawtypes")
public class ModelArchiveStore<T extends IModel> {

    private final File storageLocation;
    private final Map<Manifest, IModelArchive> index = Maps.newConcurrentMap();
    private final EventBus bus;
    private final IModelLoader<T> modelLoader;

    public ModelArchiveStore(final File storageLocation, final IModelLoader<T> modelLoader, final EventBus bus) {
        this.storageLocation = storageLocation;
        this.bus = bus;
        this.modelLoader = modelLoader;
        storageLocation.mkdirs();
        initializeArchiveIndex();
    }

    public IModelArchive getModelArchive(final Manifest manifest) {
        IModelArchive archive = index.get(manifest);
        if (archive == null) {
            archive = findOrCreateArchiveAndRegister(manifest);
        }
        return archive;
    }

    @Subscribe
    public void onEvent(final ModelArchiveDownloadFinished event) {
        ModelArchive archive = registerArchive(event);
        fireNewArchiveRegistered(archive);
    }

    @Subscribe
    public void onEvent(final ManifestResolutionFinished e) {
        if (isNewManifest(e)) {
            requestModelArchiveDownload(e);
        }
    }

    @VisibleForTesting
    protected void move(final File source, final File destination) throws IOException {
        FileUtils.moveFile(source, destination);
    }

    private ModelArchive registerArchive(final ModelArchiveDownloadFinished event) {
        @SuppressWarnings("unchecked")
        ModelArchive archive = new ModelArchive(event.archive, modelLoader);
        Manifest manifest = archive.getManifest();
        final File destination = computeModelFile(manifest);
        destination.delete();
        moveArchive(archive, destination);
        registerInIndex(archive, manifest);
        return archive;
    }

    private boolean isNewManifest(final ManifestResolutionFinished e) {
        Manifest manifest = e.manifestResolverInfo.getManifest();
        boolean known = index.containsKey(manifest);
        return !known;
    }

    private void requestModelArchiveDownload(final ManifestResolutionFinished e) {
        ModelArchiveDownloadRequested request = new ModelArchiveDownloadRequested();
        request.manifest = e.manifestResolverInfo.getManifest();
        bus.post(request);
    }

    private void registerInIndex(final IModelArchive archive, final Manifest manifest) {
        index.put(manifest, archive);
    }

    private File computeModelFile(final Manifest manifest) {
        return new File(storageLocation, manifest.getIdentifier() + ".zip").getAbsoluteFile();
    }

    private void fireNewArchiveRegistered(final IModelArchive archive) {
        ModelArchiveRegistered e = new ModelArchiveRegistered();
        e.archive = archive;
        bus.post(e);
        System.out.printf("%s: new model for %s (%s) - %s\n", new Date(), archive.getManifest().getName(),
                archive.getFile(), FileUtils.byteCountToDisplaySize(FileUtils.sizeOf(archive.getFile())));
    }

    @SuppressWarnings("unchecked")
    private IModelArchive findOrCreateArchiveAndRegister(final Manifest manifest) {
        IModelArchive<T> archive;
        final File file = computeModelFile(manifest);
        if (file.exists()) {
            archive = new ModelArchive(file, modelLoader);
        } else {
            archive = IModelArchive.NULL;
        }
        registerInIndex(archive, manifest);
        return archive;
    }

    private void initializeArchiveIndex() {
        for (File f : storageLocation.listFiles()) {
            if (f.isDirectory() || !f.getName().endsWith(".zip")) {
                continue;
            }
            ModelArchive archive = new ModelArchive(f, modelLoader);
            Manifest manifest = archive.getManifest();
            registerInIndex(archive, manifest);
        }

    }

    private void moveArchive(final IModelArchive archive, final File destination) {
        try {
            archive.close();
            final File source = archive.getFile();
            move(source, destination);
            archive.setFile(destination);
            archive.open();
        } catch (IOException e) {
            throwUnhandledException(e);
        }
    }

}
