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

import static org.eclipse.recommenders.utils.Constants.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.eclipse.recommenders.models.IInputStreamTransformer;
import org.eclipse.recommenders.models.IModelArchiveCoordinateAdvisor;
import org.eclipse.recommenders.models.IModelProvider;
import org.eclipse.recommenders.models.IModelRepository;
import org.eclipse.recommenders.models.SimpleModelProvider;
import org.eclipse.recommenders.models.UniqueTypeName;
import org.eclipse.recommenders.utils.Zips;

import com.google.common.annotations.Beta;

/**
 * A non-thread-safe implementation of {@link IModelProvider} for call models that keeps references on the model
 * archives.
 * <p>
 * Note that models should not be shared between several recommenders.
 */
@Beta
public class SimpleCallModelProvider extends SimpleModelProvider<UniqueTypeName, ICallModel> implements
        ICallModelProvider {

    public SimpleCallModelProvider(IModelRepository repo, IModelArchiveCoordinateAdvisor index,
            Map<String, IInputStreamTransformer> transformers) {
        super(repo, index, CLASS_CALL_MODELS, transformers);
    }

    @Override
    protected ICallModel loadModel(InputStream in, UniqueTypeName key) throws IOException {
        return JayesCallModel.load(in, key.getName());
    }

    @Override
    protected String getBasePath(UniqueTypeName key) {
        return Zips.path(key.getName(), DOT_JBIF);
    }
}
