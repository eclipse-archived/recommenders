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
package org.eclipse.recommenders.internal.commons.analysis.archive;

import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.recommenders.commons.utils.parser.OsgiVersionParser;
import org.osgi.framework.Constants;

public class OsgiManifestJarIdExtractor extends JarIdExtractor {

    private static final Name BUNDLE_NAME = new Attributes.Name(Constants.BUNDLE_SYMBOLICNAME);
    private static final Name BUNDLE_VERSION = new Attributes.Name(Constants.BUNDLE_VERSION);

    private final OsgiVersionParser parser = new OsgiVersionParser();

    @Override
    public void extract(final JarFile jarFile) throws IOException {
        final Manifest manifest = jarFile.getManifest();
        if (manifest != null) {
            final Attributes attributes = manifest.getMainAttributes();
            setName(attributes.getValue(BUNDLE_NAME));
            final String version = attributes.getValue(BUNDLE_VERSION);
            if (version != null) {
                setVersion(parser.parse(version));
            }
        }
    }

    @Override
    public void setName(String name) {
        if (name != null) {
            name = StringUtils.substringBefore(name, ";");
            name = name.trim();
        }
        super.setName(name);
    }

}
