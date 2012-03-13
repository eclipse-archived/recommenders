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
package org.eclipse.recommenders.utils.archive;

import java.io.InputStream;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.eclipse.recommenders.utils.GenericEnumerationUtils;
import org.eclipse.recommenders.utils.parser.MavenVersionParser;

public class MavenPomJarIdExtractor extends JarIdExtractor {

    private final MavenVersionParser parser = new MavenVersionParser();

    @Override
    public void extract(final JarFile jarFile) throws Exception {
        for (final ZipEntry entry : GenericEnumerationUtils.iterable(jarFile.entries())) {
            if (isPomFile(entry.getName())) {
                extract(entry.getName(), jarFile.getInputStream(entry));
            }
        }
    }

    private void extract(final String filename, final InputStream inputStream) throws Exception {
        final Properties properties = new Properties();
        properties.load(inputStream);
        parseVersion(properties);
        parseName(properties);
    }

    private void parseName(final Properties properties) {
        final String groupId = properties.getProperty("groupId");
        final String artifactId = properties.getProperty("artifactId");

        if (artifactId == null) {
            return;
        }

        if (groupId == null || artifactId.startsWith(groupId)) {
            setName(artifactId);
        } else {
            setName(groupId + "." + artifactId);
        }
    }

    private void parseVersion(final Properties properties) {
        final String version = properties.getProperty("version");
        if (version != null) {
            setVersion(parser.parse(version));
        }
    }

    private boolean isPomFile(final String filename) {
        return filename.endsWith("pom.properties");
    }

}
