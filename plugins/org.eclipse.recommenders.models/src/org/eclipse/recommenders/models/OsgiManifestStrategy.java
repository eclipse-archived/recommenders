/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Olav Lenz - initial API and implementation
 */
package org.eclipse.recommenders.models;

import static com.google.common.base.Optional.*;
import static org.eclipse.recommenders.models.DependencyType.JAR;
import static org.eclipse.recommenders.utils.Zips.closeQuietly;

import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.eclipse.recommenders.utils.Artifacts;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;

public class OsgiManifestStrategy extends AbstractStrategy {
    public static final Name BUNDLE_NAME = new Attributes.Name("Bundle-SymbolicName");
    public static final Name BUNDLE_VERSION = new Attributes.Name("Bundle-Version");
    private IFileToJarFileConverter jarFileConverter;

    public OsgiManifestStrategy() {
        jarFileConverter = new DefaultJarFileConverter();
    }

    @VisibleForTesting
    public OsgiManifestStrategy(IFileToJarFileConverter fileToJarFileConverter) {
        jarFileConverter = fileToJarFileConverter;
    }

    @Override
    protected Optional<ProjectCoordinate> extractProjectCoordinateInternal(DependencyInfo dependencyInfo) {
        JarFile jarFile = jarFileConverter.createJarFile(dependencyInfo.getFile()).orNull();
        if (jarFile == null) {
            return absent();
        }
        try {
            final Manifest manifest = jarFile.getManifest();
            if (manifest == null) {
                return absent();
            }
            return extractProjectCoordinateFromManifest(manifest);
        } catch (IOException e) {
            return absent();
        } finally {
            closeQuietly(jarFile);
        }
    }

    private Optional<ProjectCoordinate> extractProjectCoordinateFromManifest(Manifest manifest) {
        Attributes attributes = manifest.getMainAttributes();
        String name = attributes.getValue(BUNDLE_NAME);
        String version = attributes.getValue(BUNDLE_VERSION);
        if (name == null || version == null) {
            return absent();
        }
        int indexOf = name.indexOf(";");
        String aid = name.substring(0, indexOf == -1 ? name.length() : indexOf);
        String gid = Artifacts.guessGroupId(aid);
        return of(new ProjectCoordinate(gid, aid, version));
    }

    @Override
    public boolean isApplicable(DependencyType type) {
        return JAR == type;
    }
}
