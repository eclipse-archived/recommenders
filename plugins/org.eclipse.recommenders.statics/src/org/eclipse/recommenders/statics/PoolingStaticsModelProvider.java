/**
 * Copyright (c) 2017 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.recommenders.statics;

import static org.eclipse.recommenders.utils.Constants.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.eclipse.recommenders.models.IInputStreamTransformer;
import org.eclipse.recommenders.models.IModelArchiveCoordinateAdvisor;
import org.eclipse.recommenders.models.IModelRepository;
import org.eclipse.recommenders.models.PoolingModelProvider;
import org.eclipse.recommenders.models.UniqueTypeName;
import org.eclipse.recommenders.utils.Constants;
import org.eclipse.recommenders.utils.Zips;

public class PoolingStaticsModelProvider extends PoolingModelProvider<UniqueTypeName, IStaticsModel>
        implements IStaticsModelProvider {

    public PoolingStaticsModelProvider(IModelRepository repo, IModelArchiveCoordinateAdvisor index,
            Map<String, IInputStreamTransformer> transformers) {
        super(repo, index, CLASS_STATICS_MODEL, transformers);
    }

    @Override
    protected void passivateModel(IStaticsModel model) {
        model.reset();
    }

    @Override
    protected IStaticsModel loadModel(InputStream in, UniqueTypeName key) throws IOException {
        return JayesStaticsModel.load(in, key.getName());
    }

    @Override
    protected String getBasePath(UniqueTypeName key) {
        return Zips.path(key.getName(), DOT_JBIF);
    }
}
