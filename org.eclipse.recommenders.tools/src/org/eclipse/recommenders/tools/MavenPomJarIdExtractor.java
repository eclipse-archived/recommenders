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

import java.io.InputStream;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.eclipse.recommenders.commons.utils.GenericEnumerationUtils;

public class MavenPomJarIdExtractor extends JarIdExtractor {

    @Override
    public void extract(final JarFile jarFile) throws Exception {
        for (final ZipEntry entry : GenericEnumerationUtils.iterable(jarFile.entries())) {
            if (isPomFile(entry.getName())) {
                extract(entry.getName(), jarFile.getInputStream(entry));
            }
        }
    }

    private void extract(final String filename, final InputStream inputStream) throws Exception {
        // final MavenXpp3Reader reader = new MavenXpp3Reader();
        // final Model model = reader.read(inputStream);
        // setVersion(Version.valueOf(model.getVersion()));
        // setName(model.getGroupId() + "." + model.getArtifactId());
    }

    private boolean isPomFile(final String filename) {
        return filename.endsWith("pom.xml");
    }

}
