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

import static com.google.common.base.Optional.absent;
import static org.eclipse.recommenders.models.Coordinates.tryNewProjectCoordinate;
import static org.eclipse.recommenders.models.DependencyType.JAR;
import static org.eclipse.recommenders.utils.Versions.canonicalizeVersion;
import static org.eclipse.recommenders.utils.Zips.closeQuietly;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.eclipse.recommenders.models.DependencyInfo;
import org.eclipse.recommenders.models.DependencyType;
import org.eclipse.recommenders.models.ProjectCoordinate;
import org.eclipse.recommenders.utils.IOUtils;
import org.eclipse.recommenders.utils.Zips.DefaultJarFileConverter;
import org.eclipse.recommenders.utils.Zips.IFileToJarFileConverter;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;

/**
 * Implementation based on {@link MavenPomJarIdExtractor}.
 */
public class MavenPomPropertiesAdvisor extends AbstractProjectCoordinateAdvisor {

    public static final String GROUP_ID = "groupId";
    public static final String ARTIFACT_ID = "artifactId";
    public static final String VERSION = "version";

    private static final String POM_PROPERTIES_FILE_REGEX = "META-INF/maven/.*/.*/pom.properties";

    private final IFileToJarFileConverter jarFileConverter;

    public MavenPomPropertiesAdvisor() {
        jarFileConverter = new DefaultJarFileConverter();
    }

    @VisibleForTesting
    MavenPomPropertiesAdvisor(IFileToJarFileConverter fileToJarFileConverter) {
        jarFileConverter = fileToJarFileConverter;
    }

    @Override
    protected Optional<ProjectCoordinate> doSuggest(DependencyInfo dependencyInfo) {
        JarFile jarFile = readJarFileIn(dependencyInfo.getFile()).orNull();
        if (jarFile == null) {
            return absent();
        }
        try {
            return extractProjectCoordinateOfJarFile(jarFile);
        } catch (IOException e) {
            return absent();
        } finally {
            closeQuietly(jarFile);
        }
    }

    private Optional<ProjectCoordinate> extractProjectCoordinateOfJarFile(JarFile jarFile) throws IOException {
        Optional<ProjectCoordinate> candidate = absent();
        Iterator<JarEntry> entries = pomPropertiesIterator(jarFile);
        while (entries.hasNext()) {
            JarEntry entry = entries.next();
            InputStream pomProperties = jarFile.getInputStream(entry);
            Optional<ProjectCoordinate> projectCoordinate = parseProjectCoordinate(pomProperties, entry.getName());
            IOUtils.closeQuietly(pomProperties);
            if (projectCoordinate.isPresent() && candidate.isPresent()) {
                return absent(); // We have conflicting project coordinates; abort.
            }
            candidate = candidate.or(projectCoordinate);
        }
        return candidate;
    }

    private Optional<ProjectCoordinate> parseProjectCoordinate(InputStream inputStream, String propertiesFileName) {
        final Properties properties = new Properties();
        try {
            properties.load(inputStream);
            String groupID = parseGroupID(properties);
            String artifactID = parseArtifactID(properties);
            String version = parseVersion(properties);
            if (!groupID.equals(extractGroupID(propertiesFileName))) {
                return absent();
            }
            if (!artifactID.equals(extractArtifactID(propertiesFileName))) {
                return absent();
            }
            return tryNewProjectCoordinate(groupID, artifactID, canonicalizeVersion(version));
        } catch (IOException e) {
            return absent();
        }
    }

    private Iterator<JarEntry> pomPropertiesIterator(JarFile jarFile) {
        return Iterators.filter(Iterators.forEnumeration(jarFile.entries()), new Predicate<JarEntry>() {

            @Override
            public boolean apply(JarEntry entry) {
                return entry.getName().matches(POM_PROPERTIES_FILE_REGEX);
            }
        });
    }

    private Optional<JarFile> readJarFileIn(File file) {
        return jarFileConverter.createJarFile(file);
    }

    private String parseAttribute(final Properties properties, String attributeName) {
        String value = properties.getProperty(attributeName);
        return value;
    }

    private String parseGroupID(final Properties properties) {
        return parseAttribute(properties, GROUP_ID);
    }

    private String parseArtifactID(final Properties properties) {
        return parseAttribute(properties, ARTIFACT_ID);
    }

    private String parseVersion(final Properties properties) {
        return parseAttribute(properties, VERSION);
    }

    @Override
    public boolean isApplicable(DependencyType dependencyType) {
        return dependencyType == JAR;
    }

    public static String extractGroupID(String fileName) {
        return extract(fileName, 3);
    }

    public static String extractArtifactID(String fileName) {
        return extract(fileName, 2);
    }

    public static String extract(String fileName, int index) {
        String[] split = fileName.split("/");
        if (split.length >= 4) {
            return split[split.length - index];
        }
        return "";
    }
}
