/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API.
 *    Johannes Lerch - implementation.
 */
package org.eclipse.recommenders.mining.calls.generation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.recommenders.commons.bayesnet.BayesianNetwork;
import org.eclipse.recommenders.commons.udc.Manifest;
import org.eclipse.recommenders.commons.utils.Checks;
import org.eclipse.recommenders.commons.utils.gson.GsonUtil;
import org.eclipse.recommenders.commons.utils.names.ITypeName;

public class ModelArchiveFileWriter implements IModelArchiveWriter {

    private final File outputDir;
    private ZipOutputStream zos;

    public ModelArchiveFileWriter(final File outputFile) throws IOException {
        final File parentDirectory = outputFile.getParentFile();
        Checks.ensureIsDirectory(parentDirectory);
        Checks.ensureExists(parentDirectory);
        this.outputDir = outputFile;
        initalizeZip();
    }

    private void initalizeZip() throws IOException {
        zos = new ZipOutputStream(new FileOutputStream(outputDir));
    }

    @Override
    public void close() throws IOException {
        zos.close();
    }

    @Override
    public void consume(final Manifest manifest) throws IOException {
        zos.putNextEntry(new ZipEntry("manifest.json"));
        final String manifestString = GsonUtil.serialize(manifest);
        zos.write(manifestString.getBytes());
        zos.closeEntry();
    }

    @Override
    public void consume(final ITypeName typeName, final BayesianNetwork bayesNet) throws IOException {
        final String filename = typeName.getIdentifier().replaceAll("/", ".") + ".data";
        zos.putNextEntry(new ZipEntry(filename));
        final ObjectOutputStream outputStream = new ObjectOutputStream(zos);
        outputStream.writeObject(bayesNet);
        outputStream.flush();
        zos.closeEntry();
    }

}
