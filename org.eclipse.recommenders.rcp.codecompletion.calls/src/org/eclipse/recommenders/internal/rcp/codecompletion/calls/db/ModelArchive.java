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
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.eclipse.recommenders.commons.utils.gson.GsonUtil;

public class ModelArchive {

    private ZipFile zipFile;
    private Manifest manifest;

    public ModelArchive(final File filename) {
        try {
            initializeZipFile(filename);
            readManifest();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void initializeZipFile(final File filename) throws ZipException, IOException {
        zipFile = new ZipFile(filename);
    }

    private void readManifest() throws IOException {
        final ZipEntry manifestEntry = zipFile.getEntry("manifest.json");
        final InputStream inputStream = zipFile.getInputStream(manifestEntry);
        manifest = GsonUtil.deserialize(inputStream, Manifest.class);
    }

    public Manifest getManifest() {
        return manifest;
    }
}
