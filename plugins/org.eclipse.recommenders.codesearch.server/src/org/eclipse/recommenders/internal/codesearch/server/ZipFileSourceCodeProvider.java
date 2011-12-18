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
package org.eclipse.recommenders.internal.codesearch.server;

import static org.eclipse.recommenders.utils.Throws.throwIllegalArgumentException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;
import org.eclipse.recommenders.internal.codesearch.server.wiring.GuiceModule.CodesearchBasedir;

import com.google.common.io.Files;
import com.google.inject.Inject;

public class ZipFileSourceCodeProvider {

    private ZipFile zipFile;

    @Inject
    public ZipFileSourceCodeProvider(@CodesearchBasedir final File baseDir) {
        try {
            final File file = new File(baseDir, "sources/sources.zip");
            if (!file.exists()) {
                throwIllegalArgumentException("no codesearch database file found at %s", file);
            }
            zipFile = new ZipFile(file);
            // load file contents into RAM to make serving requests faster:
            Files.toByteArray(file);

        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String readFile(final String filename) {
        final ZipEntry entry = zipFile.getEntry(filename);
        if (entry == null) {
            return null;
        }
        try {
            final InputStream inputStream = zipFile.getInputStream(entry);
            final String source = IOUtils.toString(inputStream);
            return source;
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public File getFile() {
        return new File(zipFile.getName());
    }

}
