/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.commons.analysis.analyzers;

import static java.lang.String.format;
import static org.eclipse.recommenders.commons.utils.Throws.throwUnhandledException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang3.SystemUtils;
import org.eclipse.recommenders.commons.utils.IOUtils;
import org.eclipse.recommenders.commons.utils.gson.GsonUtil;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.CompilationUnit;
import org.eclipse.recommenders.internal.commons.analysis.fixture.IAnalysisFixture;

import com.google.inject.Inject;

public class ZipCompilationUnitConsumer implements ICompilationUnitConsumer {
    private ZipOutputStream zos;

    private File dest;

    private IAnalysisFixture fixture;

    @Inject
    public ZipCompilationUnitConsumer(final IAnalysisFixture fixture) throws IOException {
        this.fixture = fixture;
        openZipStream();

    }

    private void openZipStream() throws FileNotFoundException {
        initZipFileDestination();
        initZipOutputStream();
    }

    private void initZipFileDestination() {
        final File basedir = new File(SystemUtils.getUserDir(), "target/");
        basedir.mkdirs();
        final String fileName = format("%s.zip", fixture.getName());
        dest = new File(basedir, fileName);
    }

    private void initZipOutputStream() throws FileNotFoundException {
        zos = new ZipOutputStream(new FileOutputStream(dest));
    }

    @Override
    public synchronized void consume(final CompilationUnit compilationUnit) {
        final String pretty = GsonUtil.serialize(compilationUnit);
        final ZipEntry zipEntry = createZipEntry(compilationUnit);
        try {
            zos.putNextEntry(zipEntry);
            zos.write(pretty.getBytes());
            zos.closeEntry();
        } catch (final IOException e) {
            throwUnhandledException(e);
        }
    }

    private ZipEntry createZipEntry(final CompilationUnit compilationUnit) {
        final String name = compilationUnit.name.replace('/', '.');
        return new ZipEntry(name + ".json");
    }

    public File getDestination() {
        return dest;
    }

    @Override
    public void close() {
        IOUtils.closeQuietly(zos);
    }
}
