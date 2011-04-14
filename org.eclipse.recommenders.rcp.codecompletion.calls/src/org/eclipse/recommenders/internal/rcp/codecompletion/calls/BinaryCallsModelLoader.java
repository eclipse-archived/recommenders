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
package org.eclipse.recommenders.internal.rcp.codecompletion.calls;

import static org.eclipse.recommenders.commons.utils.Throws.throwUnhandledException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.commons.utils.names.VmTypeName;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class BinaryCallsModelLoader implements ICallsModelLoader {

    private final URL fileUrl;
    private JarFile modelsFile;

    @Inject
    public BinaryCallsModelLoader(@Named("calls.model.fileUrl") final URL fileUrl) {
        this.fileUrl = fileUrl;
    }

    @Override
    public Set<ITypeName> readAvailableTypes() {
        final Set<ITypeName> types = Sets.newTreeSet();
        final ZipFile modelsZip = getModelsFile();
        final List<? extends ZipEntry> entries = Collections.list(modelsZip.entries());
        for (final ZipEntry entry : entries) {
            final String fileName = entry.getName();
            if (!fileName.endsWith("data")) {
                continue;
            }
            final String typeNameAsString = StringUtils.substringBeforeLast(fileName, ".").replace('.', '/');
            final VmTypeName typeName = VmTypeName.get(typeNameAsString);
            // XXX workaround for errors like this one: failed to compute method
            // call probability for
            // 'Lorg/eclipse/swt/internal/cocoa/NSImage.<init>(I)V'; node
            // definition: [0.0, 1.0, 1.0E-4, 0.9999, 0.9999,
            // 1.0E-4]
            if (typeName.getPackage().getIdentifier().startsWith("Lorg/eclipse/swt/internal/cocoa/")) {
                continue;
            }
            types.add(typeName);
        }
        return types;
    }

    private JarFile getModelsFile() {
        if (modelsFile == null) {
            try {
                final String file = fileUrl.getFile();
                modelsFile = new JarFile(file);
            } catch (final IOException e) {
                throw throwUnhandledException(e);
            }
        }
        return modelsFile;
    }

    @Override
    public <T> T loadObjectForTypeName(final ITypeName name, final Type returnType) throws IOException {
        final ObjectInputStream inputStream = new ObjectInputStream(getModelInputStream(name));
        try {
            return (T) inputStream.readObject();
        } catch (final ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private InputStream getModelInputStream(final ITypeName name) throws IOException {
        final JarFile modelsFile = getModelsFile();
        final String entryName = name.getIdentifier().replace('/', '.').concat(".data");
        final ZipEntry entry = modelsFile.getEntry(entryName);
        final InputStream is = modelsFile.getInputStream(entry);
        return new BufferedInputStream(is);
    }

}
