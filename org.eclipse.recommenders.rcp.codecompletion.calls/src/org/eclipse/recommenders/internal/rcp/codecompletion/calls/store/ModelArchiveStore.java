/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.codecompletion.calls.store;

import static org.eclipse.recommenders.internal.rcp.codecompletion.calls.CallsCompletionModule.CALLS_STORE_LOCATION;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.recommenders.commons.lfm.Manifest;
import org.eclipse.recommenders.commons.utils.Checks;

import com.google.common.collect.Maps;

public class ModelArchiveStore implements IModelArchiveStore {

    private final File modelArchivesLocation;
    private final Map<Manifest, IModelArchive> manifest2archive = Maps.newConcurrentMap();

    @Inject
    public ModelArchiveStore(@Named(CALLS_STORE_LOCATION) final File modelArchivesLocation) {
        this.modelArchivesLocation = modelArchivesLocation;
        modelArchivesLocation.mkdirs();
    }

    public IModelArchive getModelArchive(final Manifest manifest) {
        IModelArchive archive = manifest2archive.get(manifest);
        if (archive == null) {
            final File file = getModelFile(manifest);
            if (file.exists()) {
                archive = new ModelArchive(file);
            } else {
                archive = IModelArchive.NULL;
            }
            manifest2archive.put(manifest, archive);
        }
        return archive;
    }

    private File getModelFile(final Manifest manifest) {
        return new File(modelArchivesLocation, manifest.getIdentifier() + ".zip").getAbsoluteFile();
    }

    @Override
    public void register(final File file) throws IOException {
        final ModelArchive archive = new ModelArchive(file);
        register(archive);
    }

    public void register(final ModelArchive archive) throws IOException {
        final Manifest manifest = archive.getManifest();
        final File destination = getModelFile(manifest);
        Checks.ensureIsFalse(destination.exists(), "Offered archive already exists: '%s'", destination);
        moveArchive(archive, destination);
        manifest2archive.put(manifest, archive);
    }

    private void moveArchive(final ModelArchive archive, final File destination) throws IOException {
        archive.close();
        final File source = archive.getFile();
        final boolean successfullyMoved = source.renameTo(destination);
        if (!successfullyMoved) {
            throw new IOException(String.format("Unable to move ModelArchive file from '%s' to '%s'",
                    source.getAbsolutePath(), destination.getAbsolutePath()));
        }
        archive.setFile(destination);
        archive.open();
    }
}
