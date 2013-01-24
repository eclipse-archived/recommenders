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
package org.eclipse.recommenders.utils.archive;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.util.jar.JarFile;

import org.eclipse.recommenders.utils.Fingerprints;
import org.eclipse.recommenders.utils.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArchiveDetailsExtractor {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final JarFile jarFile;
    private final File file;
    private List<JarIdExtractor> jarIdExtractors;
    private ClassIdExtractor classIdExtractor;

    private String lazyFingerprint;
    private String lazyName;
    private Version lazyVersion;
    private Collection<ClassId> lazyClassIds;

    public ArchiveDetailsExtractor(final File file) throws IOException {
        initializeExtractors();
        this.file = file;
        jarFile = new JarFile(file);
    }

    private void initializeExtractors() {
        classIdExtractor = new ClassIdExtractor();
        jarIdExtractors = new LinkedList<JarIdExtractor>();
        jarIdExtractors.add(new OsgiManifestJarIdExtractor());
        jarIdExtractors.add(new MavenPomJarIdExtractor());
        jarIdExtractors.add(new FilenameJarIdExtractor());
    }

    public String createFingerprint() {
        if (lazyFingerprint == null) {
            lazyFingerprint = Fingerprints.sha1(file);
        }
        return lazyFingerprint;
    }

    private void extractJarIds() {
        for (final JarIdExtractor extractor : jarIdExtractors) {
            try {
                extractor.extract(jarFile);
                setNameIfFound(extractor);
                setVersionIfFound(extractor);
                if (!requiresJarIdExtraction()) {
                    return;
                }
            } catch (final Exception e) {
                logger.error("Error while extracting name and version from file: " + jarFile.getName(), e);
            }
        }

        setFallbackJarIdIfNotExtracted();
    }

    private void setFallbackJarIdIfNotExtracted() {
        if (lazyName == null) {
            lazyName = "";
        }
        if (lazyVersion == null) {
            lazyVersion = Version.UNKNOWN;
        }
    }

    private boolean requiresJarIdExtraction() {
        return lazyName == null || lazyVersion == null;
    }

    private void setNameIfFound(final JarIdExtractor extractor) {
        if (lazyName == null && extractor.getName() != null) {
            lazyName = extractor.getName();
        }
    }

    private void setVersionIfFound(final JarIdExtractor extractor) {
        if (lazyVersion == null && !extractor.getVersion().isUnknown()) {
            lazyVersion = extractor.getVersion();
        }
    }

    public String extractName() {
        if (lazyName == null) {
            extractJarIds();
        }
        return lazyName;
    }

    public Version extractVersion() {
        if (lazyVersion == null) {
            extractJarIds();
        }
        return lazyVersion;
    }

    public Collection<ClassId> extractClassIds() {
        if (lazyClassIds == null) {
            try {
                classIdExtractor.extract(jarFile);
                lazyClassIds = new TreeSet<ClassId>(new Comparator<ClassId>() {

                    @Override
                    public int compare(final ClassId o1, final ClassId o2) {
                        return o1.typeName.compareTo(o2.typeName);
                    }
                });
                lazyClassIds.addAll(classIdExtractor.getClassIds());
            } catch (final Exception e) {
                logger.error("Error while extracting class ids from file: " + jarFile.getName(), e);
            }
        }

        return lazyClassIds;
    }

    public ArchiveMetaData getArchiveMetaData() {
        final ArchiveMetaData archive = new ArchiveMetaData();
        archive.fingerprint = createFingerprint();
        archive.name = extractName();
        archive.version = extractVersion();
        archive.types = extractClassIds();
        return archive;
    }
}
