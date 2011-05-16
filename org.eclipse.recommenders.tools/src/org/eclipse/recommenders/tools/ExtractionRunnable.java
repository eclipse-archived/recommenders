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
package org.eclipse.recommenders.tools;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

public class ExtractionRunnable implements Runnable {

    private final Logger logger = Logger.getLogger(getClass());
    private final File basedir;
    private final StorageService storage;

    public ExtractionRunnable(final File basedir, final StorageService storage) {
        this.basedir = basedir;
        this.storage = storage;
    }

    @Override
    public void run() {
        final File[] files = basedir.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().endsWith(".jar")) {
                tryExtraction(files[i]);
            }
        }
    }

    private void tryExtraction(final File file) {
        try {
            final ArchiveDetailsExtractor extractor = new ArchiveDetailsExtractor(file);
            logger.info(String.format("Extraction from file %s\nName: %s\nVersion: %s\n", file.getName(),
                    extractor.getName(), extractor.getVersion()));
            storage.store(extractor.getArchiveMetaData());
        } catch (final IOException e) {
            logger.warn("Extraction for file " + file.getName() + " failed.", e);
        }
    }

}
