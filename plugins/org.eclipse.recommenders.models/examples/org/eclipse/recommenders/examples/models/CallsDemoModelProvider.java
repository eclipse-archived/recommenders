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

import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.recommenders.models.IUniqueName;
import org.eclipse.recommenders.models.IModelArchiveCoordinateAdvisor;
import org.eclipse.recommenders.models.IModelRepository;
import org.eclipse.recommenders.models.PoolingModelProvider;
import org.eclipse.recommenders.utils.Zips;
import org.eclipse.recommenders.utils.names.ITypeName;

import com.google.common.base.Optional;

public class CallsDemoModelProvider extends PoolingModelProvider<IUniqueName<ITypeName>, Object> {

    public CallsDemoModelProvider(IModelRepository repo, IModelArchiveCoordinateAdvisor index) {
        super(repo, index, "call");
    }

    @Override
    protected Optional<Object> loadModel(ZipFile zip, IUniqueName<ITypeName> key) throws Exception {
        String path = Zips.path(key.getName(), ".net");
        ZipEntry entry = zip.getEntry(path);
        if (entry == null) {
            return Optional.absent();
        }
        InputStream s = zip.getInputStream(entry);
        Object model = null; // ... do things with s to create a model
        s.close();
        return Optional.fromNullable(model);
    }

}
