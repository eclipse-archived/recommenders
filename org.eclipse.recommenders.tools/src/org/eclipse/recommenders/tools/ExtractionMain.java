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

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.recommenders.commons.client.ClientConfiguration;

public class ExtractionMain {

    public static void main(final String[] args) {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.ALL);

        checkArgs(args);
        final File basedir = getBasedir(args);
        final String dbUrl = getDatabaseUrl(args);
        startExtraction(basedir, dbUrl);
    }

    private static void checkArgs(final String[] args) {
        if (args.length != 2) {
            throw new IllegalArgumentException(
                    "Exactly 2 arguments required. First argument should be the directory containing jar files and the second one should be the database url.");
        }
    }

    private static File getBasedir(final String[] args) {
        final File basedir = new File(args[0]);
        if (!basedir.exists() || !basedir.isDirectory()) {
            throw new RuntimeException("Directory does not exist: " + basedir.getAbsolutePath());
        }
        return basedir;
    }

    private static String getDatabaseUrl(final String[] args) {
        return args[1];
    }

    private static void startExtraction(final File basedir, final String dbUrl) {
        final ClientConfiguration configuration = new ClientConfiguration();
        configuration.setBaseUrl(dbUrl);
        final StorageService storageService = new StorageService(configuration);
        final ExtractionRunnable extractionRunnable = new ExtractionRunnable(basedir, storageService);
        extractionRunnable.run();
    }

}
