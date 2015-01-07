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

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.eclipse.recommenders.models.IInputStreamTransformer;
import org.eclipse.recommenders.models.IModelArchiveCoordinateAdvisor;
import org.eclipse.recommenders.models.IModelRepository;
import org.eclipse.recommenders.models.IUniqueName;
import org.eclipse.recommenders.models.PoolingModelProvider;
import org.eclipse.recommenders.utils.Constants;
import org.eclipse.recommenders.utils.Zips;
import org.eclipse.recommenders.utils.names.ITypeName;

public class PoolingOverrideModelProvider extends PoolingModelProvider<IUniqueName<ITypeName>, IOverrideModel>
        implements IOverrideModelProvider {

    public PoolingOverrideModelProvider(IModelRepository repository, IModelArchiveCoordinateAdvisor index,
            Map<String, IInputStreamTransformer> transformers) {
        super(repository, index, Constants.CLASS_OVRM_MODEL, transformers);
    }

    @Override
    protected IOverrideModel loadModel(InputStream in, IUniqueName<ITypeName> key) throws IOException {
        return JayesOverrideModel.load(in, key.getName());
    }

    @Override
    protected void passivateModel(IOverrideModel model) {
        model.reset();
    }

    @Override
    protected String getBasePath(IUniqueName<ITypeName> key) {
        return Zips.path(key.getName(), DOT_JSON);
    }
}
