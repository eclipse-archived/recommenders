/**
 * Copyright (c) 2011 Andreas Frankenberger.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API and implementation.
 */
package org.eclipse.recommenders.internal.udc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.recommenders.commons.utils.Checks;
import org.eclipse.recommenders.commons.utils.gson.GsonUtil;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.CompilationUnit;

public class CompilationUnitZipExporter implements ICompilationUnitExporter {

    private final File file;
    private ZipOutputStream outputStream;

    public CompilationUnitZipExporter(final File zipFile) {
        Checks.ensureIsNotNull(zipFile, "the zip file must not be null");
        ensureParentFileExists(zipFile);
        this.file = zipFile;
    }

    private void ensureParentFileExists(final File zipFile) {
        if (zipFile.getParentFile() == null || !zipFile.getParentFile().exists()) {
            throw new IllegalArgumentException("the parent of the zip file must exist.");
        }
    }

    @Override
    public void exportUnits(final IProject sourceProject, final List<CompilationUnit> units,
            final IProgressMonitor monitor) {
        monitor.beginTask("creating zip file", units.size());
        for (final CompilationUnit unit : units) {
            writeUnitEntry(sourceProject.getName(), unit);
            monitor.worked(1);
        }
        monitor.done();
    }

    private void writeUnitEntry(final String projectname, final CompilationUnit unit) {
        Checks.ensureIsNotEmpty(getUnitName(unit), "Can't save unnamed compilation units");
        try {
            addNewZipEntry(projectname, unit);
            writeUnit(unit);
            closeEntry();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void addNewZipEntry(final String projectname, final CompilationUnit unit) throws IOException {
        final ZipEntry entry = new ZipEntry(projectname + "/" + convertUnitName(getUnitName(unit)) + ".json");
        getOutputStream().putNextEntry(entry);
    }

    private String getUnitName(final CompilationUnit unit) {
        Checks.ensureIsNotNull(unit);
        if (unit.name != null) {
            return unit.name;
        } else {
            Checks.ensureIsNotNull(unit.id, "The fingerprint of a compilationunit must exist.");
            return unit.id;
        }
    }

    private void writeUnit(final CompilationUnit unit) throws IOException {
        final String unitString = GsonUtil.serialize(unit);
        getOutputStream().write(unitString.getBytes());
    }

    private void closeEntry() throws IOException {
        getOutputStream().closeEntry();
    }

    private String convertUnitName(final String name) {
        return name.replaceAll("/", ".").substring(1);
    }

    @Override
    public void done() {
        if (outputStream == null) {
            throw new IllegalStateException(
                    "No CompilationUnits exported. Check your project selection and filter settings");
        }
        try {
            getOutputStream().close();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ZipOutputStream getOutputStream() {
        if (outputStream == null) {
            try {
                outputStream = new ZipOutputStream(new FileOutputStream(file));
            } catch (final FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return outputStream;
    }
}
