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
package org.eclipse.recommenders.examples.models;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.recommenders.models.IBasedName;
import org.eclipse.recommenders.models.ModelArchiveCoordinate;
import org.eclipse.recommenders.models.ModelRepository;
import org.eclipse.recommenders.models.PoolingModelProvider;
import org.eclipse.recommenders.utils.Zips;
import org.eclipse.recommenders.utils.names.ITypeName;

import com.google.common.base.Optional;

public class CallsDemoModelProvider extends PoolingModelProvider<IBasedName<ITypeName>, Object> {

    public CallsDemoModelProvider(ModelRepository repo) {
        super(repo, "call");
    }

    @Override
    public void open() throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    protected Optional<Object> createModel(IBasedName<ITypeName> key, ZipFile modelArchive,
            ModelArchiveCoordinate modelId) throws IOException {
        String path = Zips.path(key.getName(), ".net");
        ZipEntry entry = new ZipEntry(path);
        InputStream s = modelArchive.getInputStream(entry);
        Object model = null; // ... do things with s to create a model
        s.close();
        return Optional.fromNullable(model);
    }

}
