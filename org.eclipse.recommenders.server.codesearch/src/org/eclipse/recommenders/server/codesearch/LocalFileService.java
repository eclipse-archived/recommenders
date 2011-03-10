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
package org.eclipse.recommenders.server.codesearch;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class LocalFileService {

    private ZipFile zipFile;

    @Inject
    public LocalFileService(@Named("codesearch.basedir") final File baseDir) {
        try {
            zipFile = new ZipFile(new File(baseDir, "sources/sources.zip"));

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

}
