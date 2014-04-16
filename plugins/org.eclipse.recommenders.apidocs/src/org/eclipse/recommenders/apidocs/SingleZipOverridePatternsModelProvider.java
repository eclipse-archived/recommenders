/**
 * Copyright (c) 2010, 2014 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Patrick Gottschaemmer - initial API and implementation.
 */
package org.eclipse.recommenders.apidocs;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;
import static org.eclipse.recommenders.utils.Constants.DOT_JSON;
import static org.eclipse.recommenders.utils.Zips.closeQuietly;
import static org.eclipse.recommenders.utils.Zips.readFully;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.recommenders.models.IModelProvider;
import org.eclipse.recommenders.models.IUniqueName;
import org.eclipse.recommenders.models.UniqueTypeName;
import org.eclipse.recommenders.utils.IOUtils;
import org.eclipse.recommenders.utils.Openable;
import org.eclipse.recommenders.utils.Zips;
import org.eclipse.recommenders.utils.gson.GsonUtil;
import org.eclipse.recommenders.utils.names.ITypeName;

import com.google.common.base.Optional;

/**
 * A model provider that uses a single zip file to resolve and load Override Patterns from.
 * <p>
 * Note that this provider does not implement any pooling behavior, i.e., calls to {@link #acquireModel(UniqueTypeName)}
 * may return the <b>same</b> {@link ClassOverridePatterns} independent of whether
 * {@link #releaseModel(ClassOverridePatterns)} was called or not. Thus, these <b>models should not be shared between
 * and used by several recommenders at the same time</b>.
 */
public class SingleZipOverridePatternsModelProvider implements
        IModelProvider<IUniqueName<ITypeName>, ClassOverridePatterns>, Openable {

    private final File models;
    private ZipFile zip;

    public SingleZipOverridePatternsModelProvider(File models) {
        this.models = models;
    }

    @Override
    public void open() throws IOException {
        readFully(models);
        zip = new ZipFile(models);
    }

    @Override
    public void close() throws IOException {
        closeQuietly(zip);
    }

    public Set<ITypeName> acquireableTypes() {
        return Zips.types(zip, DOT_JSON);
    }

    @Override
    public void releaseModel(ClassOverridePatterns value) {
    }

    @Override
    public Optional<ClassOverridePatterns> acquireModel(IUniqueName<ITypeName> key) {
        String path = Zips.path(key.getName(), DOT_JSON);
        ZipEntry entry = zip.getEntry(path);
        if (entry == null) {
            return absent();
        }

        InputStream is;
        try {
            is = zip.getInputStream(entry);
        } catch (IOException e) {
            return absent();
        }
        ClassOverridePatterns res = GsonUtil.deserialize(is, ClassOverridePatterns.class);
        IOUtils.closeQuietly(is);
        return of(res);
    }

}
