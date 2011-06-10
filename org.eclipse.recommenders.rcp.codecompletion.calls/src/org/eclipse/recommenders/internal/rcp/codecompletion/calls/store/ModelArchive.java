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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.recommenders.commons.utils.Throws;
import org.eclipse.recommenders.commons.utils.gson.GsonUtil;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.net.IObjectMethodCallsNet;

public class ModelArchive implements IModelArchive {

    private static final String MANIFEST_FILENAME = "manifest.json";
    private static final String MODEL_POSTFIX = ".data";

    private final BinarySmileCallsNetLoader loader;
    private ZipFile zipFile;
    private Manifest manifest;
    private final File file;

    public ModelArchive(final File file) {
        this.file = file;
        loader = new BinarySmileCallsNetLoader();
        initializeZipFile();
        readManifest();
    }

    private void initializeZipFile() {
        try {
            zipFile = new ZipFile(file);
        } catch (final Exception e) {
            Throws.throwUnhandledException(e, "Unable to read archive '%s'", file.getAbsolutePath());
        }
    }

    private void readManifest() {
        try {
            final ZipEntry manifestEntry = zipFile.getEntry(MANIFEST_FILENAME);
            final InputStream inputStream = zipFile.getInputStream(manifestEntry);
            manifest = GsonUtil.deserialize(inputStream, Manifest.class);
        } catch (final IOException e) {
            Throws.throwUnhandledException(e, "Unable to load manifest from archive '%s'", file.getAbsolutePath());
        }
    }

    @Override
    public Manifest getManifest() {
        return manifest;
    }

    private String getFilenameFromType(final ITypeName type) {
        return type.getIdentifier().replaceAll("/", ".") + MODEL_POSTFIX;
    }

    @Override
    public boolean hasModel(final ITypeName name) {
        return zipFile.getEntry(getFilenameFromType(name)) != null;
    }

    @Override
    public IObjectMethodCallsNet loadModel(final ITypeName name) {
        final ZipEntry entry = zipFile.getEntry(getFilenameFromType(name));
        try {
            return loader.load(name, zipFile.getInputStream(entry));
        } catch (final IOException e) {
            throw Throws.throwUnhandledException(e, "Unable to load model for type '%s' from file '%s'", name,
                    file.getAbsolutePath());
        }
    }

    @Override
    public void close() throws IOException {
        zipFile.close();
    }
}
