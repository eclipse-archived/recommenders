/**
 * Copyright (c) 2015 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Sewe - initial API and implementation.
 */
package org.eclipse.recommenders.constructors;

import static org.eclipse.recommenders.utils.Constants.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.eclipse.recommenders.models.IInputStreamTransformer;
import org.eclipse.recommenders.models.IModelArchiveCoordinateAdvisor;
import org.eclipse.recommenders.models.IModelRepository;
import org.eclipse.recommenders.models.PoolingModelProvider;
import org.eclipse.recommenders.models.UniqueTypeName;
import org.eclipse.recommenders.utils.Zips;
import org.eclipse.recommenders.utils.gson.GsonUtil;

public class ConstructorModelProvider extends PoolingModelProvider<UniqueTypeName, ConstructorModel> implements
        IConstructorModelProvider {

    public ConstructorModelProvider(IModelRepository repository, IModelArchiveCoordinateAdvisor index,
            Map<String, IInputStreamTransformer> transformers) {
        super(repository, index, CLASS_CTOR_MODEL, transformers);
    }

    @Override
    protected ConstructorModel loadModel(InputStream in, UniqueTypeName key) throws IOException {
        return GsonUtil.deserialize(in, ConstructorModel.class);
    }

    @Override
    protected String getBasePath(UniqueTypeName key) {
        return Zips.path(key.getName(), DOT_JSON);
    }
}
