/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 */
package org.eclipse.recommenders.internal.analysis.rcp.cp;

import static org.osgi.framework.Constants.BUNDLE_SYMBOLICNAME;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.eclipse.osgi.util.ManifestElement;
import org.eclipse.recommenders.utils.IOUtils;
import org.osgi.framework.BundleException;

import com.google.common.collect.Maps;

public class BundleManifestSymbolicNameFinder implements INameFinder {

    private static final String MANIFEST_PATH = "META-INF/MANIFEST.MF";
    private static final String UNKNOWN = "unknown";

    @Override
    public String find(final File file) throws IOException {
        final JarFile jarFile = new JarFile(file);
        final ZipEntry manifestEntry = jarFile.getEntry(MANIFEST_PATH);
        if (manifestEntry == null) {
            return UNKNOWN;
        }

        try {
            final Map<String, String> headers = parseManifestHeaders(jarFile, manifestEntry);
            if (!headers.containsKey(BUNDLE_SYMBOLICNAME)) {
                return UNKNOWN;
            }
            final String value = headers.get(BUNDLE_SYMBOLICNAME);
            final ManifestElement manifestElement = ManifestElement.parseHeader(BUNDLE_SYMBOLICNAME, value)[0];
            return manifestElement.getValue();
        } catch (final BundleException e) {
            return UNKNOWN;
        }
    }

    private Map<String, String> parseManifestHeaders(final JarFile jarFile, final ZipEntry manifestEntry) {
        final Map<String, String> headers = Maps.newHashMap();
        InputStream is = null;
        try {
            is = jarFile.getInputStream(manifestEntry);
            ManifestElement.parseBundleManifest(is, headers);
        } catch (final Exception e) {
            // what should to with this exception? At least print it to
            // console...
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(is);
        }
        return headers;
    }

}
