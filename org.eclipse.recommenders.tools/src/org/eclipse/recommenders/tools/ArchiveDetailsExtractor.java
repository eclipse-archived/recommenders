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
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.eclipse.recommenders.commons.utils.Fingerprints;

public class ArchiveDetailsExtractor {

    private LinkedList<AbstractExtractor> extractors;
    private TypeCompilationExtractor typeCompilationExtractor;
    private String fingerprint;

    public ArchiveDetailsExtractor(final File file) throws IOException {
        initializeExtractors();
        createFingerprint(file);
        extract(new JarFile(file));
    }

    private void initializeExtractors() {
        extractors = new LinkedList<AbstractExtractor>();
        typeCompilationExtractor = new TypeCompilationExtractor();
        extractors.add(typeCompilationExtractor);
        extractors.add(new ManifestExtractor());
        extractors.add(new PomExtractor());
        extractors.add(new FilenameExtractor());
    }

    private void createFingerprint(final File file) {
        fingerprint = Fingerprints.sha1(file);
    }

    private void extract(final JarFile jarFile) {
        for (final AbstractExtractor extractor : extractors) {
            try {
                extractor.extract(jarFile);
            } catch (final Exception e) {
                e.printStackTrace();
            }
            extractByContent(extractor, jarFile);
        }
    }

    private void extractByContent(final AbstractExtractor extractor, final JarFile jarFile) {
        final Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            final JarEntry entry = entries.nextElement();
            try {
                extractor.extract(entry.getName(), jarFile.getInputStream(entry));
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String getName() {
        for (final AbstractExtractor extractor : extractors) {
            final String name = extractor.getName();
            if (name != null) {
                return name;
            }
        }
        return null;
    }

    public String getVersion() {
        for (final AbstractExtractor extractor : extractors) {
            final String version = extractor.getVersion();
            if (version != null) {
                return version;
            }
        }
        return null;
    }

    public Collection<TypeCompilation> getTypes() {
        return typeCompilationExtractor.getTypes();
    }

    public Archive getArchive() {
        final Archive archive = new Archive();
        archive.fingerprint = fingerprint;
        archive.name = getName();
        archive.version = getVersion();
        archive.types = getTypes();
        return archive;
    }
}
