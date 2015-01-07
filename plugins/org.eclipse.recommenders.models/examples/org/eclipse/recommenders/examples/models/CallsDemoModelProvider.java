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
import java.util.Map;

import org.eclipse.recommenders.models.IInputStreamTransformer;
import org.eclipse.recommenders.models.IModelArchiveCoordinateAdvisor;
import org.eclipse.recommenders.models.IModelRepository;
import org.eclipse.recommenders.models.IUniqueName;
import org.eclipse.recommenders.models.PoolingModelProvider;
import org.eclipse.recommenders.utils.Zips;
import org.eclipse.recommenders.utils.names.ITypeName;

public class CallsDemoModelProvider extends PoolingModelProvider<IUniqueName<ITypeName>, Object> {

    public CallsDemoModelProvider(IModelRepository repo, IModelArchiveCoordinateAdvisor index,
            Map<String, IInputStreamTransformer> transformers) {
        super(repo, index, "call", transformers);
    }

    @Override
    protected Object loadModel(InputStream in, IUniqueName<ITypeName> key) throws IOException {
        Object model = null; // ... do things with s to create a model
        return model;
    }

    @Override
    protected String getBasePath(IUniqueName<ITypeName> key) {
        return Zips.path(key.getName(), ".net");
    }
}
