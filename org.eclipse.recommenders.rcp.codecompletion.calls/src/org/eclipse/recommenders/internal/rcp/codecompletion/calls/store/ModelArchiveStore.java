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
import java.text.SimpleDateFormat;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import com.google.common.collect.Lists;

public class ModelArchiveStore implements IModelArchiveStore {

    private final CallsModelIndex index;
    private final File modelArchivesLocation;

    @Inject
    public ModelArchiveStore(final CallsModelIndex index, @Named(CALLS_STORE_LOCATION) final File modelArchivesLocation) {
        this.index = index;
        this.modelArchivesLocation = modelArchivesLocation;
    }

    @Override
    public Manifest getManifest(final LibraryIdentifier libraryIdentifier) {
        return index.findMatchingModelArchive(libraryIdentifier).getManifest();
    }

    @Override
    public List<Manifest> getAllManifests() {
        final List<IModelArchive> archives = index.getAllArchives();
        final List<Manifest> manifests = Lists.newLinkedList();
        for (final IModelArchive archive : archives) {
            manifests.add(archive.getManifest());
        }
        return manifests;
    }

    @Override
    public boolean offer(final ModelArchive archive) throws IOException {
        final Manifest manifest = archive.getManifest();
        final File destination = new File(modelArchivesLocation, createFilename(manifest));
        if (destination.exists()) {
            return false;
        }

        moveArchive(archive, destination);
        index.register(archive);
        return true;
    }

    private void moveArchive(final ModelArchive archive, final File destination) throws IOException {
        archive.close();
        final File source = archive.getFile();
        final boolean successfulyMoved = source.renameTo(destination);
        if (!successfulyMoved) {
            throw new IOException(String.format("Unable to move ModelArchive file from '%s' to '%s'",
                    source.getAbsolutePath(), destination.getAbsolutePath()));
        }
        archive.open();
    }

    private String createFilename(final Manifest manifest) {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmm");
        final String time = dateFormat.format(manifest.getTimestamp());
        return manifest.getName() + "_" + manifest.getVersionRange() + "_" + time + ".zip";
    }
}
