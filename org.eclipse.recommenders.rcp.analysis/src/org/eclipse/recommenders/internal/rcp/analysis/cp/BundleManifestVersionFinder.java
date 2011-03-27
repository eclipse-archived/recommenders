/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 */
package org.eclipse.recommenders.internal.rcp.analysis.cp;

import static org.eclipse.recommenders.commons.utils.Throws.throwUnhandledException;

import java.io.File;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.eclipse.recommenders.commons.utils.Version;
import org.osgi.framework.Constants;

public class BundleManifestVersionFinder implements IVersionFinder {

    private static final Name ATTRIBUTE_NAME_BUNDLE_VERSION = new Attributes.Name(Constants.BUNDLE_VERSION_ATTRIBUTE);

    @Override
    public Version find(final File file) {
        try {
            return doFindVersion(file);
        } catch (final IOException e) {
            throw throwUnhandledException(e);
        }
    }

    private Version doFindVersion(final File file) throws IOException {
        final JarFile jarFile = new JarFile(file);
        final Manifest manifest = jarFile.getManifest();
        if (manifest == null) {
            return UNKNOWN;
        }
        final Attributes mainAttributes = manifest.getMainAttributes();
        final String version = (String) mainAttributes.get(ATTRIBUTE_NAME_BUNDLE_VERSION);
        return version == null ? UNKNOWN : Version.valueOf(version);
    }

}
