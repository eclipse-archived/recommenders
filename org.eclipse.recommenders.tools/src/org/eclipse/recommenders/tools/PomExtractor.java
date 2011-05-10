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

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

public class PomExtractor extends AbstractExtractor {

    @Override
    public void extract(final JarFile jarFile) throws Exception {
    }

    @Override
    public void extract(final String filename, final InputStream inputStream) throws Exception {
        if (isPomFile(filename)) {
            final MavenXpp3Reader reader = new MavenXpp3Reader();
            final Model model = reader.read(inputStream);
            setVersion(model.getVersion());
            setName(model.getGroupId() + "." + model.getArtifactId());
        }
    }

    private boolean isPomFile(final String filename) {
        return filename.endsWith("pom.xml");
    }

}
