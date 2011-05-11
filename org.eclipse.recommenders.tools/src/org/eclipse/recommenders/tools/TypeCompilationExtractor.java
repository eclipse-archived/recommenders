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

import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.jar.JarFile;

import org.eclipse.recommenders.commons.utils.Fingerprints;

public class TypeCompilationExtractor extends AbstractExtractor {

    private final Collection<TypeCompilation> types = new LinkedList<TypeCompilation>();

    @Override
    public void extract(final JarFile jarFile) throws Exception {
    }

    @Override
    public void extract(final String filename, final InputStream inputStream) throws Exception {
        if (isClassFile(filename)) {
            final TypeCompilation type = new TypeCompilation();
            type.typeName = getTypeName(filename);
            type.fingerprint = Fingerprints.sha1(inputStream);
            types.add(type);
        }
    }

    private boolean isClassFile(final String filename) {
        return filename.endsWith(".class");
    }

    private String getTypeName(final String filename) {
        return filename.substring(0, filename.length() - ".class".length()).replaceAll("/", ".");
    }

    public Collection<TypeCompilation> getTypes() {
        return types;
    }
}
