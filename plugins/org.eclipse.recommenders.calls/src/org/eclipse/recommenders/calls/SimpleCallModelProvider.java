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
package org.eclipse.recommenders.calls;

import static org.eclipse.recommenders.utils.Constants.CLASS_CALL_MODELS;

import java.util.zip.ZipFile;

import org.eclipse.recommenders.models.BasedTypeName;
import org.eclipse.recommenders.models.IModelProvider;
import org.eclipse.recommenders.models.IModelRepository;
import org.eclipse.recommenders.models.SimpleModelProvider;

import com.google.common.annotations.Beta;
import com.google.common.base.Optional;

/**
 * A non-thread-safe implementation of {@link IModelProvider} for call models that keeps references on the model
 * archives.
 * <p>
 * Note that models should not be shared between several recommenders.
 */
@Beta
public class SimpleCallModelProvider extends SimpleModelProvider<BasedTypeName, ICallModel> implements
        ICallModelProvider {

    public SimpleCallModelProvider(IModelRepository repo) {
        super(repo, CLASS_CALL_MODELS);
    }

    @Override
    protected Optional<ICallModel> loadModel(ZipFile zip, BasedTypeName key) throws Exception {
        return JayesCallModel.load(zip, key.getName());
    }
}
