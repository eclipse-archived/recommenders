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
import static org.apache.commons.lang3.ArrayUtils.*;
import static org.apache.commons.lang3.StringUtils.*;
import static org.eclipse.recommenders.models.Coordinates.tryNewProjectCoordinate;
import static org.eclipse.recommenders.models.DependencyType.*;
import static org.eclipse.recommenders.utils.Versions.canonicalizeVersion;
import static org.eclipse.recommenders.utils.Zips.closeQuietly;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.commons.io.IOUtils;
import org.eclipse.recommenders.models.DependencyInfo;
import org.eclipse.recommenders.models.DependencyType;
import org.eclipse.recommenders.models.ProjectCoordinate;
import org.eclipse.recommenders.utils.Zips.DefaultJarFileConverter;
import org.eclipse.recommenders.utils.Zips.IFileToJarFileConverter;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.net.InternetDomainName;

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
        File metaInfFolder = new File(projectFolder, "META-INF");
        File manifestFile = new File(metaInfFolder, "MANIFEST.MF");
        if (manifestFile.exists()) {
            InputStream in = null;
            try {
                in = new FileInputStream(manifestFile);
                Manifest manifest = new Manifest(in);
                return of(manifest);
            } catch (IOException e) {
                return absent();
            } finally {
                IOUtils.closeQuietly(in);
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
        int indexOf = bundleName.indexOf(';');
        String artifactId = bundleName.substring(0, indexOf == -1 ? bundleName.length() : indexOf);
        Optional<String> groupId = guessGroupId(artifactId);
        Optional<String> version = OsgiVersionParser.parse(bundleVersion);

        if (groupId.isPresent() && version.isPresent()) {
            return tryNewProjectCoordinate(groupId.get(), artifactId, canonicalizeVersion(version.get()));
        }
        return absent();
    }

    @Override
    public boolean isApplicable(DependencyType type) {
        return JAR == type || PROJECT == type;
    }

    private static Optional<String> guessGroupId(String reverseDomainName) {
        String[] segments = split(reverseDomainName, ".");
        removeSlashes(segments);
        String[] reverse = copyAndReverse(segments);
        try {
            InternetDomainName name = InternetDomainName.from(join(reverse, "."));
            if (!name.isUnderPublicSuffix()) {
                return Optional.of(segments[0]);
            } else {
                InternetDomainName topPrivateDomain = name.topPrivateDomain();
                int size = topPrivateDomain.parts().size();
                int end = Math.min(segments.length, size + 1);
                return Optional.of(join(subarray(segments, 0, end), "."));
            }
        } catch (IllegalArgumentException e) {
            return Optional.absent();
        }
    }

    private static String[] copyAndReverse(String[] segments) {
        String[] reverse = segments.clone();
        reverse(reverse);
        return reverse;
    }

    private static void removeSlashes(String[] segments) {
        for (int i = segments.length; i-- > 0;) {
            segments[i] = replace(segments[i], "/", "");
        }
    }
}
