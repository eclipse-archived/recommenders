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

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.util.jar.JarFile;

import org.eclipse.recommenders.commons.utils.Fingerprints;
import org.eclipse.recommenders.commons.utils.Version;

public class ArchiveDetailsExtractor {

    private List<JarIdExtractor> jarIdExtractors;
    private ClassIdExtractor classIdExtractor;
    private String fingerprint;

    public ArchiveDetailsExtractor(final File file) throws IOException {
        initializeExtractors();
        createFingerprint(file);
        final JarFile jarFile = new JarFile(file);
        extractClassIds(jarFile);
        extractJarIds(jarFile);
    }

    private void initializeExtractors() {
        classIdExtractor = new ClassIdExtractor();
        jarIdExtractors = new LinkedList<JarIdExtractor>();
        jarIdExtractors.add(new OsgiManifestJarIdExtractor());
        jarIdExtractors.add(new MavenPomJarIdExtractor());
        jarIdExtractors.add(new FilenameJarIdExtractor());
    }

    private void createFingerprint(final File file) {
        fingerprint = Fingerprints.sha1(file);
    }

    private void extractJarIds(final JarFile jarFile) {
        for (final IExtractor extractor : jarIdExtractors) {
            try {
                extractor.extract(jarFile);
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void extractClassIds(final JarFile jarFile) {
        try {
            classIdExtractor.extract(jarFile);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        for (final JarIdExtractor extractor : jarIdExtractors) {
            final String name = extractor.getName();
            if (name != null) {
                return name;
            }
        }
        return null;
    }

    public Version getVersion() {
        for (final JarIdExtractor extractor : jarIdExtractors) {
            final Version version = extractor.getVersion();
            if (!version.isUnknown()) {
                return version;
            }
        }
        return Version.UNKNOWN;
    }

    public Collection<ClassId> getClassIds() {
        final TreeSet<ClassId> treeSet = new TreeSet<ClassId>(new Comparator<ClassId>() {

            @Override
            public int compare(final ClassId o1, final ClassId o2) {
                return o1.typeName.compareTo(o2.typeName);
            }
        });
        treeSet.addAll(classIdExtractor.getClassIds());
        return treeSet;
    }

    public ArchiveMetaData getArchiveMetaData() {
        final ArchiveMetaData archive = new ArchiveMetaData();
        archive.fingerprint = fingerprint;
        archive.name = getName();
        archive.version = getVersion();
        archive.types = getClassIds();
        return archive;
    }
}
