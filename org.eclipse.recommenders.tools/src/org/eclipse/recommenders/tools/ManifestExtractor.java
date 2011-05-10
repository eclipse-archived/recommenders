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

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.osgi.framework.Constants;

public class ManifestExtractor extends AbstractExtractor {

    private static final Name BUNDLE_NAME = new Attributes.Name(Constants.BUNDLE_SYMBOLICNAME);
    private static final Name BUNDLE_VERSION = new Attributes.Name(Constants.BUNDLE_VERSION);

    @Override
    public void extract(final JarFile jarFile) throws IOException {
        final Manifest manifest = jarFile.getManifest();
        if (manifest != null) {
            final Attributes attributes = manifest.getMainAttributes();
            setName(attributes.getValue(BUNDLE_NAME));
            setVersion(attributes.getValue(BUNDLE_VERSION));
        }
    }

    @Override
    public void extract(final String filename, final InputStream inputStream) {
    }

    @Override
    public void setName(String name) {
        if (name != null) {
            final int index = name.indexOf(";");
            if (index >= 0) {
                name = name.substring(0, index);
            }
            name = name.trim();
        }
        super.setName(name);
    }

}
