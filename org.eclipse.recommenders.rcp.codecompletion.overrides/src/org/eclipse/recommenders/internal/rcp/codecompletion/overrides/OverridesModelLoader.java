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
package org.eclipse.recommenders.internal.rcp.codecompletion.overrides;

import static org.eclipse.recommenders.commons.utils.Throws.throwUnhandledException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.recommenders.commons.utils.gson.GsonUtil;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.commons.utils.names.VmTypeName;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class OverridesModelLoader implements IOverridesModelLoader {
    private static final String FILENAME_PREFIX = "class-overrides-";
    private final URL fileUrl;

    @Inject
    public OverridesModelLoader(@Named("overrides.model.fileUrl") final URL fileUrl) {
        this.fileUrl = fileUrl;
    }

    @Override
    public Set<ITypeName> readAvailableTypes() {
        final Set<ITypeName> types = Sets.newTreeSet();
        final ZipFile modelsZip = getModelsFile();
        final List<? extends ZipEntry> entries = Collections.list(modelsZip.entries());
        for (final ZipEntry entry : entries) {
            final String fileName = entry.getName();
            if (!fileName.endsWith("json")) {
                continue;
            }
            final String typeNameAsString = StringUtils.substringBeforeLast(fileName, ".").replace('.', '/')
                    .substring(FILENAME_PREFIX.length());
            final VmTypeName typeName = VmTypeName.get(typeNameAsString);
            types.add(typeName);
        }
        return types;
    }

    private JarFile getModelsFile() {
        try {
            final String file = fileUrl.getFile();
            return new JarFile(file);
        } catch (final IOException e) {
            throw throwUnhandledException(e);
        }
    }

    @Override
    public <T> T loadObjectForTypeName(final ITypeName name, final Type returnType) throws IOException {
        final InputStream inputStream = getModelInputStream(name);
        return GsonUtil.deserialize(inputStream, returnType);
    }

    private InputStream getModelInputStream(final ITypeName name) throws IOException {
        final JarFile modelsFile = getModelsFile();
        final String entryName = FILENAME_PREFIX + name.getIdentifier().replace('/', '.').concat(".json");
        final ZipEntry entry = modelsFile.getEntry(entryName);
        final InputStream is = modelsFile.getInputStream(entry);
        return is;
    }
}
