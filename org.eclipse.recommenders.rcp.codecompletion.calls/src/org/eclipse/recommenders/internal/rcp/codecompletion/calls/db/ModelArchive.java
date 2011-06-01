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
package org.eclipse.recommenders.internal.rcp.codecompletion.calls.db;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.eclipse.recommenders.commons.utils.gson.GsonUtil;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.commons.utils.names.VmTypeName;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.net.IObjectMethodCallsNet;

public class ModelArchive {

    private static final String MANIFEST_FILENAME = "manifest.json";
    private static final String MODEL_POSTFIX = ".data";

    private final BinaryModelLoader loader;
    private ZipFile zipFile;
    private Manifest manifest;
    private File file;

    public ModelArchive(final File file) {
        this.file = file;
        loader = new BinaryModelLoader();
        try {
            initializeZipFile(file);
            readManifest();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void initializeZipFile(final File file) throws ZipException, IOException {
        zipFile = new ZipFile(file);
    }

    private void readManifest() throws IOException {
        final ZipEntry manifestEntry = zipFile.getEntry(MANIFEST_FILENAME);
        final InputStream inputStream = zipFile.getInputStream(manifestEntry);
        manifest = GsonUtil.deserialize(inputStream, Manifest.class);
    }

    public Manifest getManifest() {
        return manifest;
    }

    public List<ITypeName> getTypes() {
        final LinkedList<ITypeName> result = new LinkedList<ITypeName>();
        final Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            final ZipEntry entry = entries.nextElement();
            if (isTypeModel(entry)) {
                result.add(getTypeNameFromFilename(entry.getName()));
            }
        }

        return result;
    }

    private ITypeName getTypeNameFromFilename(final String filename) {
        return VmTypeName.get(filename.substring(0, filename.length() - MODEL_POSTFIX.length()).replaceAll("\\.", "/"));
    }

    private String getFilenameFromType(final ITypeName type) {
        return type.getIdentifier().replaceAll("/", ".") + MODEL_POSTFIX;
    }

    private boolean isTypeModel(final ZipEntry entry) {
        return !entry.getName().equals(MANIFEST_FILENAME);
    }

    public IObjectMethodCallsNet load(final ITypeName name) {
        final ZipEntry entry = zipFile.getEntry(getFilenameFromType(name));
        try {
            return loader.load(name, zipFile.getInputStream(entry));
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void move(final File newFile) {
        closeZipFile();
        final boolean moveSuccessful = file.renameTo(newFile);
        if (!moveSuccessful) {
            throw new RuntimeException(String.format("Unable to move file %s to %s.", file.getAbsolutePath(),
                    newFile.getAbsolutePath()));
        }
        file = newFile;
        try {
            initializeZipFile(file);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void delete() {
        closeZipFile();
    }

    private void closeZipFile() {
        try {
            zipFile.close();
        } catch (final IOException e) {
        }
    }
}
