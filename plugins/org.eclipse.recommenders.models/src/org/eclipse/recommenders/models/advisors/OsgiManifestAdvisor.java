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
package org.eclipse.recommenders.models.advisors;

import static com.google.common.base.Optional.*;
import static org.eclipse.recommenders.models.Coordinates.tryNewProjectCoordinate;
import static org.eclipse.recommenders.models.DependencyType.*;
import static org.eclipse.recommenders.utils.Versions.canonicalizeVersion;
import static org.eclipse.recommenders.utils.Zips.closeQuietly;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.eclipse.recommenders.models.DependencyInfo;
import org.eclipse.recommenders.models.DependencyType;
import org.eclipse.recommenders.models.ProjectCoordinate;
import org.eclipse.recommenders.utils.Artifacts;
import org.eclipse.recommenders.utils.Zips.DefaultJarFileConverter;
import org.eclipse.recommenders.utils.Zips.IFileToJarFileConverter;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;

public class OsgiManifestAdvisor extends AbstractProjectCoordinateAdvisor {
    public static final Name BUNDLE_NAME = new Attributes.Name("Bundle-SymbolicName");
    public static final Name BUNDLE_VERSION = new Attributes.Name("Bundle-Version");
    private IFileToJarFileConverter jarFileConverter;

    public OsgiManifestAdvisor() {
        jarFileConverter = new DefaultJarFileConverter();
    }

    @VisibleForTesting
    public OsgiManifestAdvisor(IFileToJarFileConverter fileToJarFileConverter) {
        jarFileConverter = fileToJarFileConverter;
    }

    @Override
    protected Optional<ProjectCoordinate> doSuggest(DependencyInfo dependencyInfo) {
        Optional<Manifest> optionalManifest = absent();
        if (dependencyInfo.getType() == DependencyType.JAR) {
            optionalManifest = extractManifestFromJar(dependencyInfo);
        } else if (dependencyInfo.getType() == DependencyType.PROJECT) {
            optionalManifest = extractManifestFromProject(dependencyInfo);
        }
        if (optionalManifest.isPresent()) {
            return extractProjectCoordinateFromManifest(optionalManifest.get());
        }
        return absent();
    }

    private Optional<Manifest> extractManifestFromProject(DependencyInfo dependencyInfo) {
        File projectFolder = dependencyInfo.getFile();
        File manifestFile = new File(projectFolder, "META-INF" + File.separator + "MANIFEST.MF");
        if (manifestFile.exists()) {
            try {
                FileInputStream fileInputStream = new FileInputStream(manifestFile);
                Manifest manifest = new Manifest(fileInputStream);
                fileInputStream.close();
                return of(manifest);
            } catch (IOException e) {
                return absent();
            }
        }
        return absent();
    }

    private Optional<Manifest> extractManifestFromJar(DependencyInfo dependencyInfo) {
        Optional<JarFile> optionalJarFile = jarFileConverter.createJarFile(dependencyInfo.getFile());
        if (!optionalJarFile.isPresent()) {
            return absent();
        }
        JarFile jarFile = optionalJarFile.get();
        try {
            final Manifest manifest = jarFile.getManifest();
            return fromNullable(manifest);
        } catch (IOException e) {
            return absent();
        } finally {
            closeQuietly(jarFile);
        }
    }

    private Optional<ProjectCoordinate> extractProjectCoordinateFromManifest(Manifest manifest) {
        Attributes attributes = manifest.getMainAttributes();
        String bundleName = attributes.getValue(BUNDLE_NAME);
        String bundleVersion = attributes.getValue(BUNDLE_VERSION);
        if (bundleName == null || bundleVersion == null) {
            return absent();
        }
        int indexOf = bundleName.indexOf(";");
        String artifactId = bundleName.substring(0, indexOf == -1 ? bundleName.length() : indexOf);
        String groupId = Artifacts.guessGroupId(artifactId);
        Optional<String> version = OsgiVersionParser.parse(bundleVersion);

        if (version.isPresent()) {
            return tryNewProjectCoordinate(groupId, artifactId, canonicalizeVersion(version.get()));
        }
        return absent();
    }

    @Override
    public boolean isApplicable(DependencyType type) {
        return JAR == type || PROJECT == type;
    }
}
