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

import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.eclipse.recommenders.commons.utils.Fingerprints;
import org.eclipse.recommenders.commons.utils.GenericEnumerationUtils;
import org.eclipse.recommenders.commons.utils.names.VmTypeName;

public class ClassIdExtractor implements IExtractor {

    private final Collection<ClassId> types = new LinkedList<ClassId>();

    @Override
    public void extract(final JarFile jarFile) throws Exception {
        for (final ZipEntry entry : GenericEnumerationUtils.iterable(jarFile.entries())) {
            if (isClassFile(entry.getName())) {
                extract(entry.getName(), jarFile.getInputStream(entry));
            }
        }
    }

    private void extract(final String filename, final InputStream inputStream) throws Exception {
        final ClassId type = new ClassId();
        type.typeName = getTypeName(filename);
        type.fingerprint = Fingerprints.sha1(inputStream);
        types.add(type);
    }

    private boolean isClassFile(final String filename) {
        return filename.endsWith(".class");
    }

    private VmTypeName getTypeName(final String filename) {
        return VmTypeName.get("L" + filename.substring(0, filename.length() - ".class".length()));
    }

    public Collection<ClassId> getClassIds() {
        return types;
    }
}
