/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.overrides;

import static org.eclipse.recommenders.utils.Constants.DOT_JSON;
import static org.eclipse.recommenders.utils.Zips.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.recommenders.models.IInputStreamTransformer;
import org.eclipse.recommenders.models.IUniqueName;
import org.eclipse.recommenders.models.UniqueTypeName;
import org.eclipse.recommenders.utils.IOUtils;
import org.eclipse.recommenders.utils.Openable;
import org.eclipse.recommenders.utils.Zips;
import org.eclipse.recommenders.utils.names.ITypeName;

import com.google.common.base.Optional;

/**
 * A model provider that uses a single zip file to resolve and load call models from.
 * <p>
 * Note that this provider does not implement any pooling behavior, i.e., calls to {@link #acquireModel(UniqueTypeName)}
 * may return the <b>same</b> {@link ICallModel} independent of whether {@link #releaseModel(ICallModel)} was called or
 * not. Thus, these <b>models should not be shared between and used by several recommenders at the same time</b>.
 */
public class SingleZipOverrideModelProvider implements IOverrideModelProvider, Openable {

    private final File models;
    private ZipFile zip;
    private Map<String, IInputStreamTransformer> transformers;

    public SingleZipOverrideModelProvider(File models, Map<String, IInputStreamTransformer> transformers) {
        this.models = models;
        this.transformers = transformers;
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
        Set<ITypeName> acquireableTypesSet = Zips.types(zip.entries(), DOT_JSON);

        for (Entry<String, IInputStreamTransformer> transformer : transformers.entrySet()) {
            acquireableTypesSet.addAll(Zips.types(zip.entries(), DOT_JSON + "." + transformer.getKey())); //$NON-NLS-1$
        }

        return acquireableTypesSet;
    }

    @Override
    public void releaseModel(IOverrideModel value) {
    }

    @Override
    public Optional<IOverrideModel> acquireModel(IUniqueName<ITypeName> key) {
        InputStream stream = null;
        try {
            String path = Zips.path(key.getName(), DOT_JSON);
            stream = getInputStream(zip, path).orNull();
            if (stream == null) {
                return Optional.absent();
            } else {
                return Optional.of(JayesOverrideModel.load(stream, key.getName()));
            }
        } catch (IOException e) {
            return Optional.absent();
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    private Optional<InputStream> getInputStream(ZipFile zip, String path) throws IOException {
        for (Entry<String, IInputStreamTransformer> transformer : transformers.entrySet()) {
            ZipEntry toTransform = zip.getEntry(path + "." + transformer.getKey()); //$NON-NLS-1$
            if (toTransform == null) {
                continue;
            }
            return Optional.of(transformer.getValue().transform(zip.getInputStream(toTransform)));
        }
        ZipEntry entry = zip.getEntry(path);
        if (entry == null) {
            return Optional.absent();
        }
        return Optional.of(zip.getInputStream(entry));
    }
}
