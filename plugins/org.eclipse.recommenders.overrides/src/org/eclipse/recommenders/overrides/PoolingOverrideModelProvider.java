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

import java.util.zip.ZipFile;

import org.eclipse.recommenders.models.IBasedName;
import org.eclipse.recommenders.models.IModelRepository;
import org.eclipse.recommenders.models.PoolingModelProvider;
import org.eclipse.recommenders.utils.Constants;
import org.eclipse.recommenders.utils.names.ITypeName;

import com.google.common.base.Optional;

public class PoolingOverrideModelProvider extends PoolingModelProvider<IBasedName<ITypeName>, IOverrideModel> implements
        IOverrideModelProvider {

    public PoolingOverrideModelProvider(IModelRepository repository) {
        super(repository, Constants.CLASS_OVRM_MODEL);
    }

    @Override
    protected Optional<IOverrideModel> loadModel(ZipFile zip, IBasedName<ITypeName> key) throws Exception {
        return JayesOverrideModel.load(zip, key.getName());
    }
}
